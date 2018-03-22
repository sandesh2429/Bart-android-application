package com.myproject.joy.bartapplication;

import android.support.v7.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {

    private EditText registerEmail;
    private EditText registerPassword;
    private EditText registerConfirmPassword;
    private Button registerButton;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private ProgressDialog mProgressDialog;
    private DatabaseReference mUserDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Sign up");

        registerEmail=findViewById(R.id.registerEmail);
        registerPassword=findViewById(R.id.registerPassword);
        registerConfirmPassword=findViewById(R.id.registerConfirm);
        registerButton=findViewById(R.id.registerButton);

        mAuth=FirebaseAuth.getInstance();

        mUserDB=FirebaseDatabase.getInstance().getReference().child("Users");

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName=registerEmail.getText().toString().trim();
                String userPassword=registerPassword.getText().toString().trim();
                String confirmPassword=registerConfirmPassword.getText().toString().trim();

                if(TextUtils.isEmpty(userName)){
                    showAlertDialog("Error","Email Address is Empty!");
                }else if(TextUtils.isEmpty(userPassword)){
                    showAlertDialog("Error","Please Enter Your Password!");
                }else if(TextUtils.isEmpty(confirmPassword))
                    showAlertDialog("Error","Please Confirm Your Password!");
                else{
                    registerUserToFireBase(userName,confirmPassword);
                }
            }
        });
    }

    private void registerUserToFireBase(String userName, String confirmPassword) {
        mAuth.createUserWithEmailAndPassword(userName,confirmPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(!task.isSuccessful()){
                    showAlertDialog("Error",task.getException().getMessage());
                }else{
                    //Update User to the Firebase DB
                    currentUser = mAuth.getCurrentUser();
                    mUserDB.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Toast.makeText(getApplicationContext(), "Added user to the database", Toast.LENGTH_LONG).show();
                            String authToken = FirebaseInstanceId.getInstance().getToken();
                            dataSnapshot.getRef().child("authToken").setValue(authToken);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                    finish();
                    startActivity(new Intent(RegisterActivity.this,MapsActivity.class));
                }
            }
        });
    }

    private void showAlertDialog(String title,String message){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }
}
