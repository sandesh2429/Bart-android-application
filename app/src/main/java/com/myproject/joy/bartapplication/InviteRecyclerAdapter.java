package com.myproject.joy.bartapplication;

import android.*;
import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.myproject.joy.bartapplication.model.User;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.util.ArrayList;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class InviteRecyclerAdapter extends RecyclerView.Adapter<InviteRecyclerAdapter.InviteViewHolder>{

    private ArrayList<User> users;
    private FirebaseUser currentUser;
    private DatabaseReference mUserDB;
    private Context context;
    private static int REQUEST_SMS_PERMISSION =100;
    public InviteRecyclerAdapter(ArrayList<User> users,Context context){
        this.context=context;
        this.users=users;
    }

    public InviteViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
        View view= LayoutInflater.from(context).inflate(R.layout.invite_list_item,viewGroup,false);
        InviteViewHolder inviteViewHolder=new InviteViewHolder(view);
        return inviteViewHolder;
    }

    public void onBindViewHolder(InviteViewHolder inviteViewHolder,int position){
        currentUser= FirebaseAuth.getInstance().getCurrentUser();
        mUserDB= FirebaseDatabase.getInstance().getReference().child("Users");

        User user=users.get(position);
        inviteViewHolder.setDisplayName(user.getDisplayName());
        inviteViewHolder.setPhotoUri(user.getPhotoUri());
        String message = "Would you like to try the new Bart App?";
        inviteViewHolder.inviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    SmsManager sms = SmsManager.getDefault();
                Toast.makeText(context, "Phone number " + user.getPhoneNumber(), Toast.LENGTH_LONG).show();
                    sms.sendTextMessage(user.getPhoneNumber(), null, message, null, null);

            }
        });
    }

    public int getItemCount(){
        int arr=0;
        try {
            if (users.size() == 0) {
                return 0;
            } else {
                arr = users.size();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return arr;
    }

    public class InviteViewHolder extends RecyclerView.ViewHolder{
        View view;
        public Button inviteButton;

        public InviteViewHolder(View itemView){
            super(itemView);
            this.view=itemView;
            this.inviteButton=view.findViewById(R.id.inviteButton);
        }

        public void setDisplayName(String name){
            TextView textView=view.findViewById(R.id.inviteText);
            textView.setText(name);
        }

        public void setPhotoUri(String photoUri){
            if(photoUri!="") {
                de.hdodenhof.circleimageview.CircleImageView circleImageView = view.findViewById(R.id.inviteImage);
                Picasso.with(context).load(photoUri).placeholder(R.drawable.ic_person_profile).into(circleImageView);
            }
        }
    }
}
