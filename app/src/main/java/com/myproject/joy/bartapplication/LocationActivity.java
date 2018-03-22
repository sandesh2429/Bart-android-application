package com.myproject.joy.bartapplication;

import android.*;
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;

public class LocationActivity extends AppCompatActivity implements OnMapReadyCallback, android.location.LocationListener {

    private static final String TAG = LocationActivity.class.getSimpleName();
    private GoogleMap mGoogleMap;
    private CameraPosition mCameraPosition;
    private GeoDataClient mGeoDataClient;

    private PlaceDetectionClient mPlaceDetectionClient;

    private FusedLocationProviderClient mFusedLocationProviderClient;

    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 2;
    private boolean mLocationPermissionGranted;

    private Location mLastKnownLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    // Used for selecting the current place.
    private static final int M_MAX_ENTRIES = 5;
    private String[] mLikelyPlaceNames;
    private String[] mLikelyPlaceAddresses;
    private String[] mLikelyPlaceAttributions;
    private LatLng[] mLikelyPlaceLatLngs;
    private String directoryName;
    private File logFile;
    private FileOutputStream openFileOutput;
    private OutputStream outputStream;
    private SupportMapFragment mapFragment;
    private DatabaseReference mUsersDB;
    private FirebaseUser currentUser;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mSharedPreferencesEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        mUsersDB = FirebaseDatabase.getInstance().getReference().child("Users");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        setContentView(R.layout.activity_location);

        mGeoDataClient = Places.getGeoDataClient(this, null);

        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
        mSharedPreferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mSharedPreferencesEditor=mSharedPreferences.edit();

        if(mSharedPreferences.getString("service","").matches("")){
            mSharedPreferencesEditor.putString("service","service").commit();

            Intent intent=new Intent(this,LocationStorageService.class);
            startService(intent);
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        if (mGoogleMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mGoogleMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents, (FrameLayout) findViewById(R.id.map), false);

                TextView title = ((TextView) infoWindow.findViewById(R.id.title));
                title.setText(marker.getTitle());

                TextView snippet = ((TextView) infoWindow.findViewById(R.id.snippet));
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });

        getLocationPermission();

        updateLocationUI();

        getDeviceLocation();


    }

    private void getDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        try {
                            if (task.isSuccessful()) {
                                mLastKnownLocation = task.getResult();
                                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                if(currentUser != null) {
                                    mUsersDB.child(currentUser.getUid()).child("Latitude").setValue(mLastKnownLocation.getLatitude());
                                    mUsersDB.child(currentUser.getUid()).child("Longitude").setValue(mLastKnownLocation.getLongitude());
                                }
                            } else {
                                Log.d(TAG, "Current Location is null. Using Defaults.");
                                Log.e(TAG, "Exception: %s", task.getException());
                                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
                            }
                        } catch (Exception e) {
                            Log.i(TAG, "Exception is getDeviceLocation");
                        }
                    }
                });
            } else {
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception is %s", e.getMessage());
        } catch (NullPointerException e) {
            Log.e("Exception is %s", e.getMessage());
        } catch (Exception e) {
            Log.e("Exception is %s", e.getMessage());
        }
    }

    private void updateLocationUI() {
        try {
            if (mGoogleMap == null) {
                return;
            }
            if (mLocationPermissionGranted) {
                Toast.makeText(getApplicationContext(), "Updating UI Location", Toast.LENGTH_LONG).show();
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    return;
                }
                mGoogleMap.setMyLocationEnabled(true);
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mGoogleMap.setMyLocationEnabled(false);
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume called");
        getLocationPermission();
    }


    private void getLocationPermission() {

        Log.i(TAG, "getLocationPermission called");
        if ((ActivityCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            Log.i(TAG, "Location Permission already granted");
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            Log.i(TAG, "Location Permission Requested");
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void showCurrentPlace() {
        if (mGoogleMap == null) {
            return;
        }
        if (mLocationPermissionGranted) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            final Task<PlaceLikelihoodBufferResponse> placeResult = mPlaceDetectionClient.getCurrentPlace(null);
            placeResult.addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                @Override
                public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                    if(task.isSuccessful() && task.getResult()!=null){
                        PlaceLikelihoodBufferResponse likelyPlaces=task.getResult();

                        int count;
                        if(likelyPlaces.getCount()<M_MAX_ENTRIES){
                            count=likelyPlaces.getCount();
                        }else{
                            count=M_MAX_ENTRIES;
                        }

                        int i=0;
                        mLikelyPlaceNames=new String[count];
                        mLikelyPlaceAddresses=new String[count];
                        mLikelyPlaceAttributions=new String[count];
                        mLikelyPlaceLatLngs=new LatLng[count];

                        for(PlaceLikelihood placeLikelihood:likelyPlaces){
                            mLikelyPlaceNames[i]=(String)placeLikelihood.getPlace().getName();
                            mLikelyPlaceAddresses[i]=(String)placeLikelihood.getPlace().getAddress();
                            mLikelyPlaceAttributions[i]=(String)placeLikelihood.getPlace().getAttributions();
                            mLikelyPlaceLatLngs[i]=(LatLng)placeLikelihood.getPlace().getLatLng();

                            i++;
                            if(i>(count-1)){
                                break;
                            }
                        }

                        likelyPlaces.release();

                        openPlacesDialog();
                    }else{
                        Log.e(TAG,"Exception is %s",task.getException());
                    }
                }
            });

        }else{
            Log.i(TAG,"The user did not grant permission");

            mGoogleMap.addMarker(new MarkerOptions().title(getString(R.string.default_info_title)).position(mDefaultLocation).snippet(getString(R.id.snippet)));
        }
    }

    private void openPlacesDialog() {
        DialogInterface.OnClickListener listener=new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LatLng markerLatLng=mLikelyPlaceLatLngs[which];
                String markerSnippet=mLikelyPlaceAddresses[which];
                if(mLikelyPlaceAttributions[which]!=null){
                    markerSnippet=markerSnippet+"\n"+mLikelyPlaceAttributions[which];
                }

                mGoogleMap.addMarker(new MarkerOptions().title(mLikelyPlaceNames[which]).position(markerLatLng).snippet(markerSnippet));

                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,DEFAULT_ZOOM));
            }
        };
    }

    public void onLocationChanged(Location location){
        Log.e(TAG,"onLocationChanged: "+location);
        getDeviceLocation();
        mLastKnownLocation.set(location);
        Toast.makeText(getApplicationContext(),"On Location Changed Called",Toast.LENGTH_LONG).show();

    }

    public void onProviderDisabled(String provider){
        Log.e(TAG, "onProviderDisabled: " + provider);
    }

    public void onProviderEnabled(String provider)
    {
        Log.e(TAG, "onProviderEnabled: " + provider);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
        Log.e(TAG, "onStatusChanged: " + provider);
    }
}
