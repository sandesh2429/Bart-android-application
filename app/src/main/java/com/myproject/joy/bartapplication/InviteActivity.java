package com.myproject.joy.bartapplication;

import android.*;
import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.myproject.joy.bartapplication.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class InviteActivity extends AppCompatActivity {

    private RecyclerAdapter recyclerAdapter;
    private static final int REQUEST_PERMISSION = 100;
    private static final int REQUEST_SMS_PERMISSION = 101;
    private DatabaseReference mUsersDB;
    private FirebaseUser currentUser;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private boolean permission=false;
    private boolean smspermission=false;
    private ArrayList<String> contactNames;
    private ArrayList<String> contactPhoneNumber;
    private ArrayList<User> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Invite Friends");

        getPermission();

        linearLayoutManager=new LinearLayoutManager(this);
        recyclerView=findViewById(R.id.inviteList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        mUsersDB= FirebaseDatabase.getInstance().getReference().child("Users");

    }

    public void onRequestPermissionsResult(int requestCode,String[] permissions,int []grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        switch (requestCode){
            case REQUEST_PERMISSION:{
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    permission=true;
                    startActivity(new Intent(this,InviteActivity.class));
                }else{
                    finish();
                    Toast.makeText(getApplicationContext(),"Please allow the permission",Toast.LENGTH_LONG).show();
                }
            }break;
            case REQUEST_SMS_PERMISSION:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    smspermission=true;
                    startActivity(new Intent(this,InviteActivity.class));
                }else{
                    finish();
                    Toast.makeText(getApplicationContext(),"Please allow the permission",Toast.LENGTH_LONG).show();
                }
                break;

        }
    }

    private void getPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
             ActivityCompat.requestPermissions(InviteActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_PERMISSION);
        }else{
            permission=true;
        }

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(InviteActivity.this, new String[]{Manifest.permission.SEND_SMS}, REQUEST_SMS_PERMISSION);
        }else{
            smspermission=true;
        }
    }

    public void onStart(){
        super.onStart();
        if(permission && smspermission){
            contactNames=new ArrayList<>();
            contactPhoneNumber = new ArrayList<>();
            users=new ArrayList<>();
            ContentResolver contentResolver = getContentResolver();
            Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC");

            if ((cursor != null ? cursor.getCount() : 0) > 0) {
                while (cursor != null && cursor.moveToNext()) {
                    User user=new User();
                    user.setDisplayName(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    Cursor phones = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID+" = " + id, null, null);

                    while (phones.moveToNext()) {
                        String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        user.setPhoneNumber(number);
                    }

                    users.add(user);
                }
            }

            mUsersDB.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(Iterator<User> iterator = users.iterator(); iterator.hasNext();){
                        User user=iterator.next();
                        for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                            if(snapshot.child("displayName").getValue(String.class) != null && snapshot.child("displayName").getValue(String.class).equals(user.getDisplayName())){
                                iterator.remove();
                            }
                        }
                    }


                    InviteRecyclerAdapter inviteRecyclerAdapter=new InviteRecyclerAdapter(users,getApplicationContext());
                    recyclerView.setAdapter(inviteRecyclerAdapter);
                    inviteRecyclerAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

        }else {
            getPermission();
            Toast.makeText(getApplicationContext(), "Please provide the Contact Permission", Toast.LENGTH_LONG).show();
        }
    }

}
