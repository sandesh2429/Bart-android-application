package com.myproject.joy.bartapplication;

import android.*;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.PopupMenu;
import android.widget.Switch;
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
import com.squareup.picasso.Picasso;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;

import static java.net.Proxy.Type.HTTP;

public class LocationRequestResponse extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 100;
    private DatabaseReference mUsersDB;
    private FirebaseUser currentUser;
    private RecyclerView mRecyclerView;
    private Bundle mBundleRecyclerViewState;
    private LinearLayoutManager linearLayoutManager;
    private Switch mSwitch;
    private boolean requestResponse=false;
    ActionBar actionBar;
    private ArrayList<String> contactNames;
    private boolean permission=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_request);

        getPermission();
        Intent intent=getIntent();
        if(intent.getExtras()!=null)
            requestResponse=Boolean.valueOf(intent.getExtras().get("response").toString());

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mUsersDB = FirebaseDatabase.getInstance().getReference().child("Users");

        actionBar = getSupportActionBar();

        linearLayoutManager=new LinearLayoutManager(this);
        mRecyclerView=findViewById(R.id.locationRequestList);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.location_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        if (requestResponse == false) {
            actionBar.setTitle("Request Location");
            MenuItem menuItem = menu.findItem(R.id.requestLocationMenu);
            menuItem.setVisible(false);
        } else {
            actionBar.setTitle("Share Location");
            MenuItem menuItem = menu.findItem(R.id.shareLocationMenu);
            menuItem.setVisible(false);
        }
        return true;
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.shareLocationMenu:
                finish();
                Intent intent = new Intent(this, LocationRequestResponse.class);
                intent.putExtra("response",true);
                startActivity(intent);

                break;
            case R.id.requestLocationMenu:
                finish();
                startActivity(new Intent(this, LocationRequestResponse.class));
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }


    private void getPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(LocationRequestResponse.this, android.Manifest.permission.READ_CONTACTS)) {

            } else {
                ActivityCompat.requestPermissions(LocationRequestResponse.this, new String[]{android.Manifest.permission.READ_CONTACTS}, REQUEST_PERMISSION);
            }
        }else{
            permission=true;
        }
    }

    public void onRequestPermissionsResult(int requestCode,String[] permissions,int []grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        switch (requestCode){
            case REQUEST_PERMISSION:{
                if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    permission=true;

                    startActivity(new Intent(this,LocationRequestResponse.class));
                }else{
                    Toast.makeText(getApplicationContext(),"Please allow the permission",Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void onStart() {
        super.onStart();
        if (permission)
        {
            contactNames = new ArrayList<>();
            ContentResolver contentResolver = getContentResolver();
            Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

            if ((cursor != null ? cursor.getCount() : 0) > 0) {
                while (cursor != null && cursor.moveToNext()) {
                    contactNames.add(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));

                }
            }

            ArrayList<User> users = new ArrayList<>();
            mUsersDB.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (String name : contactNames) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            if (snapshot.hasChild("displayName")) {
                                if (snapshot.child("displayName").getValue(String.class).equals(name)) {
                                    User user = snapshot.getValue(User.class);
                                    user.setUserId(snapshot.getKey());
                                    users.add(user);
                                }
                            }
                        }
                    }
                    RecyclerAdapter recyclerAdapter = new RecyclerAdapter(users, getApplicationContext(), requestResponse);
                    mRecyclerView.setAdapter(recyclerAdapter);
                    recyclerAdapter.notifyDataSetChanged();
                    Log.e("UserArrayList", "User ArrayList Size " + users.size());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}
