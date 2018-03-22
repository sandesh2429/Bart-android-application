package com.myproject.joy.bartapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.myproject.joy.bartapplication.model.User;

import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private TextView signupEditText;
    private Button loginButton;
    private EditText loginEmail;
    private EditText loginPassword;
    private DatabaseReference mUserDB;

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Sign in");

        signupEditText=findViewById(R.id.registerText);
        loginEmail=findViewById(R.id.emailEditText);
        loginPassword=findViewById(R.id.editPassword);
        loginButton=findViewById(R.id.loginButton);
        mAuth=FirebaseAuth.getInstance();
        mUserDB= FirebaseDatabase.getInstance().getReference().child("Users");

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail=loginEmail.getText().toString().trim();
                String userPassword=loginPassword.getText().toString().trim();

                if(TextUtils.isEmpty(userEmail))
                    showAlertDialog("Error","Please Enter Your Email!");
                else if(TextUtils.isEmpty(userPassword))
                    showAlertDialog("Error","Cannot Sign In Without Your Password");
                else
                    loginUserToFireBase(userEmail,userPassword);
            }
        });

        signupEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(LoginActivity.this
                        ,RegisterActivity.class);
                startActivity(intent);

            }
        });
    }

    private void loginUserToFireBase(String userEmail, String userPassword) {
        mAuth.signInWithEmailAndPassword(userEmail,userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(!task.isSuccessful()){
                    showAlertDialog("Error",task.getException().getMessage());
                }
                else{
                    FirebaseUser currentUser=task.getResult().getUser();
                    if(currentUser!=null){
                        String authToken = FirebaseInstanceId.getInstance().getToken();
                        mUserDB.child(currentUser.getUid()).child("authToken").setValue(authToken);
                        Toast.makeText(LoginActivity.this,"Logged in",Toast.LENGTH_LONG).show();
                    }
                    finish();
                    startActivity(new Intent(LoginActivity.this, MapsActivity.class));
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
