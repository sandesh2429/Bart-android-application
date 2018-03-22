package com.myproject.joy.bartapplication;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.myproject.joy.bartapplication.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class TrackRecyclerAdapter extends RecyclerView.Adapter<TrackRecyclerAdapter.TrackViewHolder> {
    private Context context;
    private ArrayList<User> users;
    private FirebaseUser currentUser;
    private DatabaseReference mUsersDB;

    public TrackRecyclerAdapter(ArrayList<User> users,Context context){
        this.context=context;
        this.users=users;
    }

    public TrackViewHolder onCreateViewHolder(ViewGroup parent,int viewType){
        View view= LayoutInflater.from(context).inflate(R.layout.track_data,parent,false);
        TrackViewHolder trackViewHolder=new TrackViewHolder(view);
        return trackViewHolder;
    }

    public void onBindViewHolder(TrackViewHolder trackViewHolder,int position){
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mUsersDB= FirebaseDatabase.getInstance().getReference().child("Users");

        User user=users.get(position);
        trackViewHolder.setDisplayName(user.getDisplayName());
        String ImageURI = user.getPhotoUri();
        trackViewHolder.setPhotoUri(ImageURI);

        trackViewHolder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MapsActivity2.class);
                intent.putExtra("Key",user.getUserId());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    public int getItemCount(){
        int arrSize=0;
        if(users.size()==0){
            return arrSize;
        }else{
            arrSize=users.size();
        }
        return arrSize;
    }

    public class TrackViewHolder extends RecyclerView.ViewHolder{
        View view;
        public ImageView button;

        public TrackViewHolder(View itemView){
            super(itemView);
            this.view=itemView;

            this.button=view.findViewById(R.id.arrow);
        }

        public void setDisplayName(String name){
            TextView textView=view.findViewById(R.id.textView);
            textView.setText(name);
        }

        public void setPhotoUri(String photoUri){
            de.hdodenhof.circleimageview.CircleImageView circleImageView=view.findViewById(R.id.requestPicTrack);
            Picasso.with(context).load(photoUri).placeholder(R.drawable.ic_person_profile).into(circleImageView);
        }
    }
}
