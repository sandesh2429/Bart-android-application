package com.myproject.joy.bartapplication;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.support.v7.app.ActionBar;
import android.content.DialogInterface;
import android.content.Intent;
import android.Manifest;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;


public class ProfileActivity extends AppCompatActivity {

    private String passedUserId;
    private static String TAG = "ProfileActivity";
    private EditText profileNameEditText;
    private EditText phoneEditText;
    private EditText emailEditText;
    private ImageView profileImage;
    private Button saveButton;

    private FloatingActionButton editButton;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private StorageReference mStorage;
    private DatabaseReference mUserDB;
    private DatabaseReference mPhoneNumberDB;

    private static final int REQUEST_PHOTO_CAPTURE=1;
    private static final int REQUEST_PHOTO_PICK=1;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 101;
    private static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 102;

    private Uri mPhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_layout);

        profileNameEditText=findViewById(R.id.nameEditText);
        phoneEditText=findViewById(R.id.profileNumber);
        emailEditText=findViewById(R.id.profileEmail);

        profileImage = findViewById(R.id.profilePic);

        mAuth = FirebaseAuth.getInstance();
        passedUserId = getIntent().getStringExtra("userId");
        currentUser = mAuth.getCurrentUser();
        mUserDB = FirebaseDatabase.getInstance().getReference().child("Users");
        mPhoneNumberDB = FirebaseDatabase.getInstance().getReference().child("PhoneNumber");
        mStorage = FirebaseStorage.getInstance().getReference();
        saveButton = findViewById(R.id.profileSaveButton);
        editButton = findViewById(R.id.profilePicEdit);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");



        mUserDB.child(passedUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userName=dataSnapshot.child("displayName").getValue(String.class);
                String phoneNumber=dataSnapshot.child("PhoneNumber").getValue(String.class);
                String photoUrl=dataSnapshot.child("photoUri").getValue(String.class);

                profileNameEditText.setText(userName);
                phoneEditText.setText(phoneNumber);
                String userEmail=currentUser.getEmail().toString();
                emailEditText.setText(userEmail);
                try{
                    Picasso.with(ProfileActivity.this).load(photoUrl).placeholder(R.drawable.ic_person_profile).into(profileImage);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                builder.setMessage("How Would You Like To Take Your Picture?");
                builder.setPositiveButton("Choose Photo Intent", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dispatchChoosePhotoIntent();
                    }
                });
                builder.setNegativeButton("Take new photo", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        dispatchTakePhotoIntent();

                    }
                });
                builder.setNeutralButton("cc", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create();
                AlertDialog alertDialog=builder.show();

                Button cameraButton=alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                cameraButton.setCompoundDrawablesWithIntrinsicBounds(ProfileActivity.this.getResources().getDrawable(R.drawable.ic_camera_button),null,null,null);
                cameraButton.setText("");

                Button photoButton=alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                photoButton.setCompoundDrawablesWithIntrinsicBounds(ProfileActivity.this.getResources().getDrawable(R.drawable.ic_gallery_button),null,null,null);
                photoButton.setText("");

                Button cancelButton=alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                cancelButton.setCompoundDrawablesWithIntrinsicBounds(ProfileActivity.this.getResources().getDrawable(R.drawable.ic_cancel_button),null,null,null);
                cancelButton.setText("");


            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newUserName=profileNameEditText.getText().toString().trim();
                String newPhoneNumber=phoneEditText.getText().toString().trim();
                String newEmail=emailEditText.getText().toString().trim();
                if(!TextUtils.isEmpty(newUserName)){
                    updateUserName(newUserName);
                }
                if(!TextUtils.isEmpty(newPhoneNumber)){
                    updateUserPhoneNumber(newPhoneNumber);
                }
                if (!TextUtils.isEmpty(newEmail)){
                   updateUserEmail(newEmail);
                }
                if(mPhotoUri!=null)
                    updateUserPhoto(mPhotoUri);
            }
        });
    }

    private void updateUserPhoto(Uri mPhotoUri) {
        StorageReference userImageReference = mStorage.child("User Image").child(currentUser.getUid()).child(mPhotoUri.getLastPathSegment());
        userImageReference.putFile(mPhotoUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                Uri uploadImageUri = task.getResult().getDownloadUrl();
                Map<String, Object> updatePhotoMap = new HashMap<>();
                updatePhotoMap.put("photoUri", uploadImageUri.toString());
                mUserDB.child(currentUser.getUid()).updateChildren(updatePhotoMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "Success Photo Update", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void updateUserEmail(String newEmail) {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Password");
        builder.setMessage("Enter Your Password");
        final EditText password=new EditText(ProfileActivity.this);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        password.setLayoutParams(lp);
        builder.setView(password);
        builder.setIcon(R.drawable.ic_register_password);

        builder.setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AuthCredential credential=EmailAuthProvider.getCredential(currentUser.getEmail(),password.getText().toString().trim());

                currentUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            currentUser.updateEmail(newEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(ProfileActivity.this,"Email Successfully Updated!",Toast.LENGTH_LONG).show();
                                    }else{
                                        Toast.makeText(ProfileActivity.this,"Email Could Not Be Updated!",Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }else{
                            Toast.makeText(ProfileActivity.this,"Authentication Unsuccessful",Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();


    }

    private void updateUserPhoneNumber(String newPhoneNumber) {
        mUserDB.child(passedUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataSnapshot.getRef().child("PhoneNumber").setValue(newPhoneNumber);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void updateUserName(String newUserName) {

        UserProfileChangeRequest profileUpdate=new UserProfileChangeRequest.Builder().setDisplayName(newUserName).build();
        currentUser.updateProfile(profileUpdate).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Map<String, Object> updateUserNameMap=new HashMap<>();
                updateUserNameMap.put("displayName",newUserName);
                mUserDB.child(passedUserId).updateChildren(updateUserNameMap);
            }
        });
    }

    private void dispatchChoosePhotoIntent() {
        Intent choosePhoto=new Intent(Intent.ACTION_PICK);
        choosePhoto.setType("image/*");
        startActivityForResult(choosePhoto,REQUEST_PHOTO_PICK);
    }

    private void dispatchTakePhotoIntent() {
        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Requesting Camera Permission");
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    MY_CAMERA_REQUEST_CODE);
        } else if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Requesting Write External Storage Permission");
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        } else if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Requesting  Read External Storage Permission");
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_EXTERNAL_STORAGE_REQUEST_CODE);
        } else {
            Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePhotoIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePhotoIntent, REQUEST_PHOTO_CAPTURE);
            }
        }
    }

    public void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);

        if(requestCode==REQUEST_PHOTO_CAPTURE && resultCode==RESULT_OK){
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Requesting  Read External Storage Permission");
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        READ_EXTERNAL_STORAGE_REQUEST_CODE);
            } else {
                if(data.getData() == null) {
                    Bitmap bitmap = (Bitmap)data.getExtras().get("data");
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    String path = MediaStore.Images.Media.insertImage(this.getContentResolver(), bitmap, "Title", null);

                    mPhotoUri = Uri.parse(path);
                    Log.i(TAG, "the image uri: " + data.getExtras().get("data"));
                } else {
                    mPhotoUri = data.getData();
                }
                Log.i(TAG, "Setting the image uri: " + mPhotoUri);
                profileImage.setImageURI(mPhotoUri);
            }
        }else if(requestCode==REQUEST_PHOTO_PICK && resultCode==RESULT_OK){
            mPhotoUri=data.getData();
            profileImage.setImageURI(null);
            profileImage.setImageURI(mPhotoUri);
            profileImage.postInvalidate();
        }
    }
}
