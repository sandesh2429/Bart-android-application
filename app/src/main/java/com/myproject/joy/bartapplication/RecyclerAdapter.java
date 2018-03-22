package com.myproject.joy.bartapplication;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.myproject.joy.bartapplication.model.User;
import com.squareup.picasso.Picasso;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.UserViewHolder> {


    private DatabaseReference mUsersDB;
    private FirebaseUser currentUser;
    ArrayList<User> userList;
    Context context;
    private boolean requestResponse;

    public RecyclerAdapter(ArrayList<User> userList,Context context,boolean requestResponse){
        this.context=context;
        this.userList=userList;
        this.requestResponse=requestResponse;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup viewGroup,int viewType){
        View view= LayoutInflater.from(context).inflate(R.layout.request_list_item,viewGroup,false);
        UserViewHolder userViewHolder=new UserViewHolder(view);
        return userViewHolder;
    }

    @Override
    public void onBindViewHolder(UserViewHolder userViewHolder,int position){
        currentUser= FirebaseAuth.getInstance().getCurrentUser();
        mUsersDB= FirebaseDatabase.getInstance().getReference().child("Users");


        User user=userList.get(position);
        mUsersDB.child(user.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("locationSharing")) {
                    String locSharing = dataSnapshot.child("locationSharing").getValue(String.class);
                    if (locSharing.contains(currentUser.getUid())) {
                        userViewHolder.setCheckedState(true);
                        //userViewHolder.setButtonState(false);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        userViewHolder.setDisplayName(user.getDisplayName());
        userViewHolder.setDisplayPic(user.getPhotoUri());

        userViewHolder.switchButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                userViewHolder.switchButton.setTag("Tag");
                return false;
            }
        });

        userViewHolder.switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (userViewHolder.switchButton.getTag() != null) {
                    if (isChecked) {
                        mUsersDB.child(user.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild("locationSharing")) {
                                    String locationSharing = dataSnapshot.child("locationSharing").getValue(String.class);
                                    locationSharing = locationSharing +currentUser.getUid().toString() + ",";
                                    dataSnapshot.getRef().child("locationSharing").setValue(locationSharing);
                                } else {
                                    dataSnapshot.getRef().child("locationSharing").setValue(currentUser.getUid() + ",");
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        new RequestServerNotification(user.getAuthToken(), "has shared his/her location with You. You can track him/her now! :)").execute();

                    } else {
                        mUsersDB.child(user.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String locationSharing = dataSnapshot.child("locationSharing").getValue(String.class);
                                locationSharing = locationSharing.replace(currentUser.getUid() + ",", "");
                                dataSnapshot.getRef().child("locationSharing").setValue(locationSharing);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        new RequestServerNotification(user.getAuthToken(), "has stopped sharing location with you!").execute();

                    }
                }
            }
        });

        userViewHolder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, FirebaseIDService.class);
                context.startService(intent);
                new RecyclerAdapter.RequestServerNotification(user.getAuthToken()).execute();
            }
        });
    }

    public int getItemCount(){
        int arr=0;
        try{
            if(userList.size()==0)
                arr=0;
            else
                arr=userList.size();
        }catch (Exception e){
            e.printStackTrace();
        }
        return arr;
    }




    public class UserViewHolder extends RecyclerView.ViewHolder{
        View view;
        public Switch switchButton;
        public Button button;

        public UserViewHolder(View itemView){
            super(itemView);
            this.view=itemView;

            switchButton=view.findViewById(R.id.requestSwitchButton);
            button=view.findViewById(R.id.sendNotifBtn);
            if(requestResponse==false){
                switchButton.setVisibility(View.GONE);
                button.setVisibility(View.VISIBLE);
            }else{
                switchButton.setVisibility(View.VISIBLE);
                button.setVisibility(View.GONE);
            }

        }

        public void setButtonState(boolean buttonState){
            button.setEnabled(buttonState);
        }
        public void setDisplayName(String name){
            TextView textView=view.findViewById(R.id.requestName);
            textView.setText(name);

        }

        public void setDisplayPic(String photoUrl){
            de.hdodenhof.circleimageview.CircleImageView circleImageView=view.findViewById(R.id.requestPic);
            Picasso.with(context).load(photoUrl).placeholder(R.drawable.ic_person_profile).into(circleImageView);
        }

        public void setCheckedState(boolean checked){
            switchButton.setTag(null);
            switchButton.setChecked(checked);
        }
    }
    private class RequestServerNotification extends AsyncTask<Void, Void, Void> {
        String authToken;
        String message;

        public RequestServerNotification(String authToken) {
            this.authToken = authToken;
        }

        public RequestServerNotification(String authToken,String message){
            this.authToken=authToken;
            this.message=message;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            HttpHandler httpHandler = new HttpHandler();
            HttpPost httpost = new HttpPost("https://fcm.googleapis.com/fcm/send");
            httpost.addHeader("Content-Type", "application/json");
            httpost.addHeader("Authorization", "key=AAAAqFOrfcI:APA91bE4O7LlQB2P_vPQTYPsYv70tZ0RxIo5WtwXIXgnwSNi7cV6caIE7ak7Z5UwB5ThnzRQHyRL0aQQ3qzN3-TkQJJgs1jwWX6f4asFBz6EHndZ7Pl_0rSJUH4riVzgyx4xLsntjOZr");
            JSONObject object = new JSONObject();
            Log.i("RequestServerNotif", "authToken is " + authToken);
            try {
                if(requestResponse==false) {
                    String presentUser = currentUser.getDisplayName();
                    StringEntity paramsEntity = new StringEntity("{\"to\":\"" + authToken + "\",\"notification\":{\"body\":\"Please share your Location to " + presentUser + "\",\"title\":\"Bart App\"}}");
                    httpost.setEntity(paramsEntity);
                }else{
                    String presentUser = currentUser.getDisplayName();
                    StringEntity paramsEntity = new StringEntity("{\"to\":\"" + authToken + "\",\"notification\":{\"body\":\"" + presentUser + " "+message+"\",\"title\":\"Bart App\"}}");
                    httpost.setEntity(paramsEntity);
                }
            }catch (UnsupportedEncodingException exception) {
                exception.printStackTrace();
            }
            try {
                int resp = httpHandler.makeServerCall(httpost);
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Status code is " + resp, Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception exception) {
                exception.printStackTrace();
                Log.i("Vamshik", " Some exception");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Log.i("RequestServerNoti", "onPostExecute RequestServerNotification");
        }
    }
}
