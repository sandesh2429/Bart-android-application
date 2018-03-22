package com.myproject.joy.bartapplication;


import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;
import android.security.keystore.KeyNotYetValidException;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.app.ActionBar;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class MapsActivity2 extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = "MapsActivity2";
    private GoogleMap mMap;
    private DatabaseReference mUserDB;
    private Double latitude;
    private Double longitude;
    private Double currentLatitude;
    private Double currentLongitude;
    private Location mLastKnownLocation;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PolylineOptions options;
    Bitmap bitmap;
    File localFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);

        mUserDB = FirebaseDatabase.getInstance().getReference().child("Users");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

            Bundle bundle = getIntent().getExtras();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);

        String Key = bundle.getString("Key");
        mUserDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                latitude = dataSnapshot.child(Key).child("Latitude").getValue(Double.class);
                longitude = dataSnapshot.child(Key).child("Longitude").getValue(Double.class);
                String name = dataSnapshot.child(Key).child("displayName").getValue(String.class);
                String photoUri = dataSnapshot.child(Key).child("photoUri").getValue(String.class);

                Double myLatitude=dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Latitude").getValue(Double.class);
                Double myLongitude=dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Longitude").getValue(Double.class);

                    /*FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference();
                    StorageReference gsReference  = storage.getReferenceFromUrl("gs://bartproject-67b93.appspot.com/User Image/FD2llQ6di7b80wcx57fLQYsgZfO2/22458");*/

                   /* if(photoUri!=null) {

                        //StorageReference islandRef = storageRef.child("User Image/FD2llQ6di7b80wcx57fLQYsgZfO2/22458");

                        final long ONE_MEGABYTE = 1024 * 1024;
                        //islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                           // @Override
                            *//*public void onSuccess(byte[] bytes) {
                            *//*    // Data for "images/island.jpg" is returns, use this as needed
                                Toast.makeText(MapsActivity2.this, "Recieved the image", Toast.LENGTH_LONG).show();
                                LatLng latLng = new LatLng(latitude, longitude);
                                MarkerOptions markerOptions = new MarkerOptions();
                                mMap.clear();

                               // Bitmap bmp = BitmapFactory.decodeStream(photoUri.openConnection().getInputStream());
                                mMap.addMarker(markerOptions.position(latLng).title(name).snippet("Failed Setting Photo").icon(BitmapDescriptorFactory.fromResource(R.drawable.if_bullet_blue_35773))).showInfoWindow();
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));*/
                       /*     }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle any errors
                            }
                        });
*/
                       // StorageReference pathReference = storageRef.child(photoUri);
                       /* StorageReference httpsReference = FirebaseStorage.getInstance().getReferenceFromUrl(photoUri);
                        try {
                            localFile = File.createTempFile("images", "jpg");
                        } catch (IOException exception) {
                            exception.printStackTrace();
                        }

                        gsReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                // Local temp file has been created
                                LatLng latLng = new LatLng(latitude, longitude);
                                MarkerOptions markerOptions = new MarkerOptions();
                                mMap.clear();
                                mMap.addMarker(markerOptions.position(latLng).title(name).snippet("Setting Photo").icon(BitmapDescriptorFactory.fromFile(localFile.getName()))).showInfoWindow();
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle any errors
                                Log.i("Vamshik", "Photo uri is " + photoUri);
                                LatLng latLng = new LatLng(latitude, longitude);
                                MarkerOptions markerOptions = new MarkerOptions();
                                mMap.clear();
                                mMap.addMarker(markerOptions.position(latLng).title(name).snippet("Failed Setting Photo").icon(BitmapDescriptorFactory.fromResource(R.drawable.if_bullet_blue_35773))).showInfoWindow();
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                            }
                        });*/
                    /*} else {*/
                        LatLng latLng = new LatLng(latitude, longitude);
                        MarkerOptions markerOptions = new MarkerOptions();
                        mMap.clear();
                        mMap.addMarker(markerOptions.position(latLng).title(name).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_walk))).showInfoWindow();
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    /*}*/

                options=new PolylineOptions();
                options.color(Color.parseColor("#CC0000FF"));
                options.width(5);
                options.visible(true);
                options.add(new LatLng(latitude,longitude));
                options.add(new LatLng(myLatitude,myLongitude));
                mMap.addPolyline(options);

            }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

    }

}



