package com.myproject.joy.bartapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.myproject.joy.bartapplication.model.User;
import com.myproject.joy.bartapplication.model.UserLocation;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class TrackActivity extends AppCompatActivity {

    private static final String TAG = TrackActivity.class.getSimpleName();
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference mUserDB;
    private RecyclerView mRecyclerView;
    private Bundle mBundleRecyclerViewState;
    private ArrayList<User> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Track Location");

        mAuth = FirebaseAuth.getInstance();
        mUserDB = FirebaseDatabase.getInstance().getReference().child("Users");
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView = findViewById(R.id.homeRecyclerView);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        if(mAuth.getCurrentUser() == null){
            goToLogin();
        }
    }

    private void goToLogin() {
        startActivity(new Intent(TrackActivity.this, LoginActivity.class));
    }

    public void onPause(){
        super.onPause();
        mBundleRecyclerViewState=new Bundle();
        Parcelable listState=mRecyclerView.getLayoutManager().onSaveInstanceState();
        mBundleRecyclerViewState.putParcelable("recycler_state",listState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mAuth.getCurrentUser() == null){
            goToLogin();
        }

        if(mBundleRecyclerViewState!=null){
            Parcelable listState = mBundleRecyclerViewState.getParcelable("recycler_state");
            mRecyclerView.getLayoutManager().onRestoreInstanceState(listState);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        currentUser=FirebaseAuth.getInstance().getCurrentUser();

        users=new ArrayList<>();
        mUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User myUser=dataSnapshot.child(currentUser.getUid()).getValue(User.class);
                for(DataSnapshot snapshot:dataSnapshot.getChildren()) {
                    if (myUser.getLocationSharing() != null) {
                        String locSharing = myUser.getLocationSharing();
                        if (locSharing.contains(snapshot.getKey())) {
                            User user = snapshot.getValue(User.class);
                            user.setUserId(snapshot.getKey());
                            users.add(user);
                        }
                    }
                }
                TrackRecyclerAdapter trackRecyclerAdapter=new TrackRecyclerAdapter(users,getApplicationContext());
                mRecyclerView.setAdapter(trackRecyclerAdapter);
                trackRecyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
