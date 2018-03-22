package com.myproject.joy.bartapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import com.google.maps.android.ui.IconGenerator;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.myproject.joy.bartapplication.model.UserLocation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, PopupMenu.OnMenuItemClickListener {

    Geocoder geocoder;
    private GoogleMap mMap;
    private boolean sourceSelected = false;
    private boolean destinationSelected = false;
    private boolean stationInfoSelected = false;
    private String sourceStation;
    private String desttinationStation;
    private ImageButton goToBartStationBtn;
    private DatabaseReference mUserLocationDB;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private static final int REQUEST_PERMISSION=100;
    private boolean boolean_permission;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mSharedPreferencesEditor;
    private Double latitude, longitude;
    private AutoCompleteTextView searchText;
    private ArrayList<Marker> markerList;
	private ArrayList<Marker> displayMarkerList;
    private Context context;
    private String station;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            latitude = Double.valueOf(bundle.get("latitude").toString());
            longitude = Double.valueOf(bundle.get("longitude").toString());

            if (latitude != null && longitude != null) {
                postUserLocationToFireBase(latitude, longitude);
            }
        }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mAuth=FirebaseAuth.getInstance();

        geocoder=new Geocoder(this, Locale.getDefault());
        mSharedPreferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mSharedPreferencesEditor=mSharedPreferences.edit();

        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
        markerList = new ArrayList<>();

        mUserLocationDB= FirebaseDatabase.getInstance().getReference().child("Users");
        goToBartStationBtn = (ImageButton) findViewById(R.id.google_go_button);

        fn_permission();

        if(boolean_permission){
            if(mSharedPreferences.getString("service","").matches("")){
                mSharedPreferencesEditor.putString("service","service").commit();

                Intent intent=new Intent(this,LocationStorageService.class);
                startService(intent);
            }
        }

        goToBartStationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng westOakland = new LatLng(37.804872, -122.295140);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(westOakland));
                mMap.animateCamera( CameraUpdateFactory.zoomTo( 9.0f ) );
            }
        });

        Intent intent=new Intent(this,LocationStorageService.class);
        startService(intent);
    }

    private void postUserLocationToFireBase(Double latitude, Double longitude) {
        UserLocation userLocation=new UserLocation(longitude,latitude);
        mUserLocationDB.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataSnapshot.getRef().child("Longitude").setValue(longitude);
                dataSnapshot.getRef().child("Latitude").setValue(latitude);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int []grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        switch (requestCode){
            case REQUEST_PERMISSION:{
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    boolean_permission=true;
                }else{
                    Toast.makeText(getApplicationContext(),"Please allow the permission",Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void fn_permission() {
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)){

            }else{
                ActivityCompat.requestPermissions(MapsActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_PERMISSION);
            }
        }else{
            boolean_permission=true;
        }
    }

    protected void onResume() {

        for (Marker marker : markerList) {
            if (marker.getTag() == "true") {
                marker.setIcon(BitmapDescriptorFactory.defaultMarker());
                marker.hideInfoWindow();
                LatLng westOakland = new LatLng(37.804872, -122.295140);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(westOakland));
                mMap.animateCamera( CameraUpdateFactory.zoomTo( 9.0f ) );
            }
            //registerReceiver(broadcastReceiver,new IntentFilter(LocationStorageService.str_receiver));
        }
        super.onResume();
    }

    protected void onPause(){
        super.onPause();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        try {
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));
            if(!success)
                Log.e("Error", "Style parsing failed.");
        }catch (Resources.NotFoundException e){
            Log.e("Error","Can't find style",e);
        }
        mMap = googleMap;
        mMap.getUiSettings().setCompassEnabled(true);

        // Add a marker in Sydney and move the camera
        LatLng twelvethStreet = new LatLng(37.803768, -122.271450);
        LatLng sixteenthStreet = new LatLng(37.765062, -122.419694);
        LatLng nineteenthStreet = new LatLng(37.808350, -122.268602);
        LatLng twentyFourthStreet = new LatLng(37.752470, -122.418143);
        LatLng ashby = new LatLng(37.852803, -122.270062);
        LatLng balboaPark = new LatLng(37.721585, -122.447506);
        LatLng bayFair = new LatLng(37.696924, -122.126514);
        LatLng castroValley = new LatLng(37.690746, -122.075602);
        LatLng civicCenterUNPlaza = new LatLng(37.779732, -122.414123);
        LatLng coliseum = new LatLng(37.753661, -122.196869);
        LatLng colma = new LatLng(37.684638, -122.466233);
        LatLng concord = new LatLng(37.973737, -122.029095);
        LatLng dalyCity = new LatLng(37.706121, -122.469081);
        LatLng downtownBerkley = new LatLng(37.870104, -122.268133);
        LatLng dublinPleasanton = new LatLng(37.701687, -121.899179);
        LatLng elCerritoDelNorte = new LatLng(37.925086, -122.316794);
        LatLng elCerritoPlaza = new LatLng(37.902632, -122.298904);
        LatLng embarcadero = new LatLng(37.792874, -122.397020);
        LatLng fremont = new LatLng(37.557465, -121.976608);
        LatLng fruitvale = new LatLng(37.774836, -122.224175);
        LatLng glenPark = new LatLng(37.733064, -122.433817);
        LatLng hayward = new LatLng(37.669723, -122.087018);
        LatLng lafayette = new LatLng(37.893176, -122.124630);
        LatLng lakeMerritt = new LatLng(37.797027, -122.265180);
        LatLng macArthur = new LatLng(37.829065, -122.267040);
        LatLng millbrae = new LatLng(37.600271, -122.386702);
        LatLng montogommeryStreet = new LatLng(37.789405, -122.401066);
        LatLng northBerkley = new LatLng(37.873967, -122.283440);
        LatLng northConcord = new LatLng(38.003193, -122.024653);
        LatLng oaklandinternationalAirport = new LatLng(37.713238, -122.212191);
        LatLng orinda = new LatLng(37.878361, -122.183791);
        LatLng pittsburghBayPoint = new LatLng(38.018914, -121.945154);
        LatLng pleasantHill = new LatLng(37.928468, -122.056012);
        LatLng powellStreet = new LatLng(37.784471, -122.407974);
        LatLng richmond = new LatLng(37.936853, -122.353099);
        LatLng rockbridge = new LatLng(37.844702, -122.251371);
        LatLng sanBruno = new LatLng(37.637761, -122.416287);
        LatLng sanFranciscoInternationalAirport = new LatLng(37.615966, -122.392409);
        LatLng sanLeandro = new LatLng(37.721947, -122.160844);
        LatLng southSanFrancisco = new LatLng(37.721947, -122.160844);
        LatLng unionCity = new LatLng(37.590630, -122.017388);
        LatLng walnutCreek = new LatLng(37.905522, -122.067527);
        LatLng warmSprings = new LatLng(37.502171, -121.939313);
        LatLng westDublin = new LatLng(37.699756, -121.928240);
        LatLng westOakland = new LatLng(37.804872, -122.295140);

        Marker m1 = mMap.addMarker(new MarkerOptions().position(twelvethStreet).title("12th St. Oakland City Center"));
        markerList.add(m1);
        Marker m2 = mMap.addMarker(new MarkerOptions().position(sixteenthStreet).title("16th St. Mission"));
        markerList.add(m2);
        Marker m3 = mMap.addMarker(new MarkerOptions().position(nineteenthStreet).title("19th St. Oakland"));
        markerList.add(m3);
        Marker m4 = mMap.addMarker(new MarkerOptions().position(twentyFourthStreet).title("24th St. Mission"));
        markerList.add(m4);
        Marker m5 = mMap.addMarker(new MarkerOptions().position(ashby).title("Ashby"));
        markerList.add(m5);
        Marker m6 = mMap.addMarker(new MarkerOptions().position(balboaPark).title("Balboa Park"));
        markerList.add(m6);
        Marker m7 = mMap.addMarker(new MarkerOptions().position(bayFair).title("Bay Fair"));
        markerList.add(m7);
        Marker m8 = mMap.addMarker(new MarkerOptions().position(castroValley).title("Castro Valley"));
        markerList.add(m8);
        Marker m9 = mMap.addMarker(new MarkerOptions().position(civicCenterUNPlaza).title("Civic Center/UN Plaza"));
        markerList.add(m9);
        Marker m10 = mMap.addMarker(new MarkerOptions().position(coliseum).title("Coliseum"));
        markerList.add(m10);
        Marker m11 = mMap.addMarker(new MarkerOptions().position(colma).title("Colma"));
        markerList.add(m11);
        Marker m12 = mMap.addMarker(new MarkerOptions().position(concord).title("Concord"));
        markerList.add(m12);
        Marker m13 = mMap.addMarker(new MarkerOptions().position(dalyCity).title("Daly City"));
        markerList.add(m13);
        Marker m14 = mMap.addMarker(new MarkerOptions().position(dublinPleasanton).title("Dublin/Pleasanton"));
        markerList.add(m14);
        Marker m15 = mMap.addMarker(new MarkerOptions().position(elCerritoDelNorte).title("El Cerrito Plaza"));
        markerList.add(m15);
        Marker m16 = mMap.addMarker(new MarkerOptions().position(embarcadero).title("Embarcadero"));
        markerList.add(m16);
        Marker m17 = mMap.addMarker(new MarkerOptions().position(fremont).title("Fremont"));
        markerList.add(m17);
        Marker m18 = mMap.addMarker(new MarkerOptions().position(fruitvale).title("Fruitvale"));
        markerList.add(m18);
        Marker m19 = mMap.addMarker(new MarkerOptions().position(glenPark).title("Glen Park"));
        markerList.add(m19);
        Marker m20 = mMap.addMarker(new MarkerOptions().position(hayward).title("Hayward"));
        markerList.add(m20);
        Marker m21 = mMap.addMarker(new MarkerOptions().position(lafayette).title("Lafayette"));
        markerList.add(m21);
        Marker m22 = mMap.addMarker(new MarkerOptions().position(lakeMerritt).title("Lake Merritt"));
        markerList.add(m22);
        Marker m23 = mMap.addMarker(new MarkerOptions().position(macArthur).title("MacArthur"));
        markerList.add(m23);
        Marker m24 = mMap.addMarker(new MarkerOptions().position(millbrae).title("Millbrae"));
        markerList.add(m24);
        Marker m25 = mMap.addMarker(new MarkerOptions().position(montogommeryStreet).title("Montgomery St."));
        markerList.add(m25);
        Marker m26 = mMap.addMarker(new MarkerOptions().position(northBerkley).title("North Berkeley"));
        markerList.add(m26);
        Marker m27 = mMap.addMarker(new MarkerOptions().position(northConcord).title("North Concord/Martinez"));
        markerList.add(m27);
        Marker m28 = mMap.addMarker(new MarkerOptions().position(oaklandinternationalAirport).title("Oakland International Airport"));
        markerList.add(m28);
        Marker m29 = mMap.addMarker(new MarkerOptions().position(orinda).title("Orinda"));
        markerList.add(m29);
        Marker m30 = mMap.addMarker(new MarkerOptions().position(pittsburghBayPoint).title("Pittsburg/Bay Point"));
        markerList.add(m30);
        Marker m31 = mMap.addMarker(new MarkerOptions().position(pleasantHill).title("Pleasant Hill/Contra Costa Centre"));
        markerList.add(m31);
        Marker m32 = mMap.addMarker(new MarkerOptions().position(powellStreet).title("Powell St."));
        markerList.add(m32);
        Marker m33 = mMap.addMarker(new MarkerOptions().position(richmond).title("Richmond"));
        markerList.add(m33);
        Marker m34 = mMap.addMarker(new MarkerOptions().position(rockbridge).title("Rockridge"));
        markerList.add(m34);
        Marker m35 = mMap.addMarker(new MarkerOptions().position(sanBruno).title("San Bruno"));
        markerList.add(m35);
        Marker m36 = mMap.addMarker(new MarkerOptions().position(sanFranciscoInternationalAirport).title("San Francisco International Airport"));
        markerList.add(m36);
        Marker m37 = mMap.addMarker(new MarkerOptions().position(sanLeandro).title("San Leandro"));
        markerList.add(m37);
        Marker m38 = mMap.addMarker(new MarkerOptions().position(southSanFrancisco).title("South San Francisco"));
        markerList.add(m38);
        Marker m39 = mMap.addMarker(new MarkerOptions().position(unionCity).title("Union City"));
        markerList.add(m39);
        Marker m40 = mMap.addMarker(new MarkerOptions().position(walnutCreek).title("Walnut Creek"));
        markerList.add(m40);
        Marker m41 = mMap.addMarker(new MarkerOptions().position(warmSprings).title("Warm Springs/South Fremont"));
        markerList.add(m41);
        Marker m42 = mMap.addMarker(new MarkerOptions().position(westDublin).title("West Dublin/Pleasanton"));
        markerList.add(m42);
        Marker m43 = mMap.addMarker(new MarkerOptions().position(westOakland).title("West Oakland"));
        markerList.add(m43);


        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Toast.makeText(MapsActivity.this,marker.getTitle().toString(),Toast.LENGTH_LONG).show();
                marker.showInfoWindow();
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                marker.setTag("true");
                showRadioButtonDialog(marker.getTitle().toString());
                return true;
            }
        });
        searchText = (AutoCompleteTextView)findViewById(R.id.input_search);
        ArrayList<String> stationList = new ArrayList<>();
        for (Marker marker : markerList) {
            stationList.add(marker.getTitle());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, stationList);
        searchText.setAdapter(adapter);
        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER
                        || keyEvent.getAction()== KeyEvent.KEYCODE_SEARCH){

                    String s = searchText.getText().toString();


                    for (Marker marker : markerList) {
                        if ((marker.getTitle()).equalsIgnoreCase(s)) {
                            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                            marker.showInfoWindow();
                            LatLng latLng= marker.getPosition();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
                            searchText.setText("");
                            marker.setTag("true");
                        }
                        else {
                            marker.setIcon(BitmapDescriptorFactory.defaultMarker());
                        }
                    }
                    if(!searchText.getText().toString().isEmpty()){
                        Toast.makeText(MapsActivity.this, "No Station Found", Toast.LENGTH_LONG).show();
                    }
                    //execute our method for searching
                   /* if(flag = true){
                        Toast.makeText(MapsActivity.this, "No Station Found", Toast.LENGTH_LONG).show();
                    }*/
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
                }

                searchText.setText("");
                return false;
            }
        });
        context = this;
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                for(Marker marker : markerList){
                    if(marker.getTag()=="true"){
                        marker.setIcon(BitmapDescriptorFactory.defaultMarker());
                        marker.hideInfoWindow();
                    }
                }
            }
        });

        mMap.moveCamera(CameraUpdateFactory.newLatLng(westDublin));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(twelvethStreet));
        mMap.animateCamera( CameraUpdateFactory.zoomTo( 9.0f ) );
        mMap.setMinZoomPreference(10.0f);
      displayMarkerList = new ArrayList<Marker>();
		for(Marker marker:markerList) {
            Double Lat =marker.getPosition().latitude + 0.004914;
            Double Long = marker.getPosition().longitude + 0.000633;
            LatLng temp =  new LatLng(Lat, Long);

            IconGenerator iconFactory = new IconGenerator(context);
            Marker mMarkerA = mMap.addMarker(new MarkerOptions().position(temp));
            mMarkerA.setIcon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(marker.getTitle())));


            displayMarkerList.add(mMarkerA);

        }
        for(Marker marker:displayMarkerList) {
            marker.setVisible(false);

        }

        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int reason) {
                if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                    Log.d("", "onCameraMoveStarted");

                    float zoom = mMap.getCameraPosition().zoom;
                    Log.e("Zoom", "Zoom : " + zoom);
                    if((int)zoom == 12 ||(int)zoom == 13){
                        //
                        for(Marker marker:displayMarkerList) {
                            marker.setVisible(true);
                        }
                    }
                    if((int)zoom == 11 || (int)zoom== 10){
                        for(Marker marker:displayMarkerList) {
                            marker.setVisible(false);
                        }

                    }



                }
            }
        });
    }

    @Override
    public void onBackPressed() {
		super.onBackPressed();
            for(Marker marker : markerList){
                if(marker.getTag()=="true"){
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker());
                    marker.hideInfoWindow();
                }
            }

    }

    public void showPopUp(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        MenuInflater inflater = popupMenu.getMenuInflater();
        popupMenu.setOnMenuItemClickListener(this);
        inflater.inflate(R.menu.menu_options, popupMenu.getMenu());
        popupMenu.show();
        if(mAuth.getCurrentUser()!=null){
            popupMenu.getMenu().findItem(R.id.login_page).setVisible(false);
        }else{
            popupMenu.getMenu().findItem(R.id.profile_page).setVisible(false);
            popupMenu.getMenu().findItem(R.id.logout_dialog).setVisible(false);
            popupMenu.getMenu().findItem(R.id.requestLocation).setVisible(false);
            popupMenu.getMenu().findItem(R.id.track).setVisible(false);
            popupMenu.getMenu().findItem(R.id.inviteActivity).setVisible(false);
        }
    }

    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.login_page:
                startActivity(new Intent(MapsActivity.this, LoginActivity.class));
                return true;

            case R.id.logout_dialog:
                AlertDialog.Builder builder=new AlertDialog.Builder(MapsActivity.this);
                builder.setMessage("Are You Sure You Want To Sign Out?");
                builder.setPositiveButton("Yes! I'm Sure!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAuth.signOut();
                        finish();
                        startActivity(getIntent());
                    }
                });
                builder.setNegativeButton("No! I Want To Stay Logged In!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                return true;

            case R.id.profile_page:
                Intent intent=new Intent(this,ProfileActivity.class);
                intent.putExtra("userId",mAuth.getCurrentUser().getUid());
                startActivity(intent);
                return true;

            case R.id.requestLocation:
                startActivity(new Intent(this,LocationRequestResponse.class));
                return true;

            case R.id.clipperRecharge:
                Intent intentClipper = new Intent(this, ClipperAndParking.class);
                intentClipper.putExtra("URL", "https://m.clippercard.com/ClipperCard/needLogin.jsf");
                startActivity(intentClipper);
                return true;

            case R.id.bookParking:
                Intent intentParking = new Intent(this, ClipperAndParking.class);
                intentParking.putExtra("URL", "https://www.select-a-spot.com/bart/users/login");
                startActivity(intentParking);
                return true;

            case R.id.inviteActivity:
                startActivity(new Intent(this,InviteActivity.class));
                return true;

			case R.id.track:
                startActivity(new Intent(this,TrackActivity.class));
                return true;
				
            default:
                return false;
        }
    }

    private void showRadioButtonDialog(String bart) {
        final String bartStation = bart;
        AlertDialog levelDialog;
        String[] stationList;
        String[] stationAbbrevatedList;

        stationList = getResources().getStringArray(R.array.stationList);
        stationAbbrevatedList = getResources().getStringArray(R.array.stationAbbrevatedList);

        // custom dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(bartStation + " as : ");
        final CharSequence[] items = {" Source "," Destination ", " Station Info: "};
        builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {

                switch(item)
                {
                    case 0:
                        sourceSelected = true;
                        //destinationSelected = false;
                        break;
                    case 1:
                        destinationSelected = true;
                        break;
                    case 2:
                        stationInfoSelected = true;
                        break;
                }
            }
        });

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if(sourceSelected == true) {
                    sourceSelected = false;
                    sourceStation = bartStation;
                    stationInfoSelected = false;
                } else if(destinationSelected == true) {
                    destinationSelected = false;
                    desttinationStation = bartStation;
                    stationInfoSelected = false;
                }
                if(sourceStation != null && desttinationStation != null) {
                    Intent newIntent = new Intent(MapsActivity.this, ScheduleActivity.class);
                    newIntent.putExtra("sourceStation", sourceStation);
                    newIntent.putExtra("destinationStation", desttinationStation);
                    dialog.dismiss();
                    startActivity(newIntent);

                    sourceStation = null;
                    desttinationStation = null;
                    stationInfoSelected = false;
                }
                if(stationInfoSelected){
                    Toast.makeText(MapsActivity.this, "Station Info to be shown", Toast.LENGTH_LONG).show();
                    for(int k = 0; k < stationList.length; k++) {
                        if(stationList[k].equalsIgnoreCase(bart)) {
                            station = stationAbbrevatedList[k];
                            new getStationInfo(bart, station).execute();
                            break;
                        }
                    }

                    stationInfoSelected = false;
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                for(Marker marker : markerList){
                    if(marker.getTag()=="true"){
                        marker.setIcon(BitmapDescriptorFactory.defaultMarker());
                        marker.hideInfoWindow();
                    }
                }
                dialog.dismiss();
            }
        });
        levelDialog = builder.create();
        levelDialog.show();

    }

    public void createAlertDialog(String title, String message) {
        AlertDialog.Builder alert= new AlertDialog.Builder(this);
        alert.setTitle(title);
        alert.setMessage(message);
        alert.create().show();
    }

    private class getStationInfo extends AsyncTask<Void, Void, Void> {
        String message;
        String stationName;
        String stationNameAbbr;
        String elevatorDescription;

        boolean bartParking = false;
        boolean bikeRacks = false;
        boolean bikeStation = false;
        boolean locker = false;

        public getStationInfo(String stationName, String stationNameAbbr) {
            this.stationName = stationName;
            this.stationNameAbbr = stationNameAbbr;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params){
            HttpHandler httpHandlerForStationInfo = new HttpHandler();
            HttpHandler httpHandlerForElevatorInfo = new HttpHandler();
            String url = "http://api.bart.gov/api/stn.aspx?cmd=stnaccess&orig=" + stationNameAbbr + "&l=1&key=MW9S-E7SL-26DU-VV8V&json=y";
            String elevatorUrl = "https://api.bart.gov/api/bsa.aspx?cmd=elev&orig=" + stationNameAbbr +"&key=MW9S-E7SL-26DU-VV8V&json=y";
            final String json = httpHandlerForStationInfo.makeServiceCall(url);
            final String jsonElevator = httpHandlerForElevatorInfo.makeServiceCall(elevatorUrl);
            try {
                JSONObject jsonObject = new JSONObject(json);
                JSONObject rootObject = jsonObject.getJSONObject("root");
                JSONObject stationsObject = rootObject.getJSONObject("stations");
                JSONObject stationObject = stationsObject.getJSONObject("station");

                JSONObject elevatorJsonObject = new JSONObject(jsonElevator);
                JSONObject elevatorRootObject = elevatorJsonObject.getJSONObject("root");
                JSONArray elevatorBSAArray = elevatorRootObject.getJSONArray("bsa");
                JSONObject elevatorBSAObject = elevatorBSAArray.getJSONObject(0);
                JSONObject elevatorDescriptionObject = elevatorBSAObject.getJSONObject("description");
                elevatorDescription = elevatorDescriptionObject.getString("#cdata-section");
                stationName = stationObject.getString("name");
                message = "";
                if(stationObject.getString("@parking_flag").equals("1")) {
                    message += "Bart Parking is available at " + stationName + "\n";
                    bartParking = true;
                } else {
                    message += "No Bart Parking is available at " + stationName + "\n";
                    bartParking = false;
                }

                if(stationObject.getString("@bike_flag").equals("1")) {
                    message += "Bike Racks are available at " + stationName + "\n";
                    bikeRacks = true;
                } else {
                    message += "Bike Racks are not available at " + stationName + "\n";
                    bikeRacks = false;
                }

                if(stationObject.getString("@bike_station_flag").equals("1")) {
                    message += stationName + " is a Bike Station"  + "\n";
                    bikeStation = true;
                } else {
                    message += stationName + " is not a Bike Station"  + "\n";
                    bikeStation = false;
                }

                if (stationObject.getString("@locker_flag").equals("1")) {
                    message += "Lockers are available at " + stationName + "\n";
                    locker = true;
                } else {
                    message += "Lockers are not available at " + stationName + "\n";
                    locker = false;
                }
                message += "Elevator Info: \n" + elevatorDescription;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //createAlertDialog(stationName + " info:", message);
                        LayoutInflater inflater = getLayoutInflater();
                        View dialoglayout = inflater.inflate(R.layout.station_info_layout, null);
                        TextView stationInfotextView = (TextView) dialoglayout.findViewById(R.id.stationNameInfo);

                        TextView bartParkingTextView = (TextView) dialoglayout.findViewById(R.id.bartParking);
                        TextView bikeRacksTextView = (TextView) dialoglayout.findViewById(R.id.bikeParking);
                        TextView bikeStationTextView = (TextView) dialoglayout.findViewById(R.id.bikeStation);
                        TextView lockerTextView = (TextView) dialoglayout.findViewById(R.id.locker);
                        TextView elevatorTextView = (TextView) dialoglayout.findViewById(R.id.elevator);

                        stationInfotextView.setText(stationName + " Info: ");
                        if(bartParking) {
                            bartParkingTextView.setText("Bart Parking: Available");
                        } else{
                            bartParkingTextView.setText("Bart Parking: Not Available");
                        }

                        if(bikeRacks) {
                            bikeRacksTextView.setText("Bike Racks: Available");
                        } else{
                            bikeRacksTextView.setText("Bike Racks: Not Available");
                        }

                        if(bikeStation) {
                            lockerTextView.setText("Locker Facility: Available");
                        } else{
                            lockerTextView.setText("Locker Facility: Not Available");
                        }

                        if(locker) {
                            bikeStationTextView.setText("Bike Station: Available");
                        } else{
                            bikeStationTextView.setText("Bike Station: Not Available");
                        }

                        elevatorTextView.setText(elevatorDescription);

                        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                        builder.setView(dialoglayout);
                        builder.show();
                        for(Marker marker : markerList){
                            if(marker.getTag()=="true"){
                                marker.setIcon(BitmapDescriptorFactory.defaultMarker());
                                marker.hideInfoWindow();
                            }
                        }
                    }
                });
            } catch (Exception exception) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Unable to get Station Info", Toast.LENGTH_LONG).show();
                    }
                });
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }
}