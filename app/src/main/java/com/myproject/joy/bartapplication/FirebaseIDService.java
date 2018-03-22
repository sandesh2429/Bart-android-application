package com.myproject.joy.bartapplication;

import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Vamshik B D on 3/8/2018.
 */

public class FirebaseIDService extends FirebaseInstanceIdService {
    private static final String TAG="FirebaseIDService";
    private FirebaseUser currentUser;
    private DatabaseReference mUserDB;

    public void onTokenRefresh(){
        currentUser= FirebaseAuth.getInstance().getCurrentUser();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed Token: " + refreshedToken);
        sendRegistrationServer(refreshedToken);
    }

    private void sendRegistrationServer(String refreshedToken) {
        if(currentUser!=null) {
            mUserDB = FirebaseDatabase.getInstance().getReference().child("Users");
            mUserDB.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Toast.makeText(getApplicationContext(), "Token Added to Database", Toast.LENGTH_LONG).show();
                    dataSnapshot.getRef().child("authToken").setValue(refreshedToken);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}
