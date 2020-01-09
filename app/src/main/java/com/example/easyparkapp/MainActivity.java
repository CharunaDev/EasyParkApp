package com.example.easyparkapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.example.easyparkapp.Constens.ERROR_DIALOG_REQUEST;
import static com.example.easyparkapp.Constens.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.example.easyparkapp.Constens.PERMISSIONS_REQUEST_ENABLE_GPS;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {



    private GoogleMap mMap;
    private Button confrmbtn;
    Location lastLocationclnew;
    Location ParkLocation;
    LatLng temp;
    Marker marker;
    private String parkloc="";
    FirebaseUser user;

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION= Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE=1234;
    private static final String TAG ="Mapactivity";
    private Boolean mLocationpermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final float DEAFAULT_ZOOM =15f;

    FirebaseAuth auth;
    DatabaseReference db;

    private FirebaseDatabase firebaseDatabase;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth=FirebaseAuth.getInstance();
        db= FirebaseDatabase.getInstance().getReference().child("Park");



//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);

        getLocationPermission();


    }

    //method2

//    public void onLocationChanged(Location location) {
//        if (location != null) {
//            updateRiderPosition(new GeoLocation(location.getLatitude(), location.getLongitude()));
//        }
//    }
//
//    private ArrayList<Marker> markerList = new ArrayList<>();
//
//    private void updateRiderPosition(GeoLocation location) {
//        if (markerList != null) {
//            for (Marker marker : markerList) {
//                marker.remove();
//            }
//        }
//
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(parkloc).child("locations");
//        GeoFire geoFire = new GeoFire(ref);
//        GeoQuery geoQuery = geoFire.queryAtLocation(location, 1);
//        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
//            @Override
//            public void onKeyEntered(String key, GeoLocation location) {
//                markerList.add(mMap.addMarker(new MarkerOptions().position(new MLatLng(location.latitude, location.longitude)).icon(icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_motorbike)))));
//            }
//
//            @Override
//            public void onKeyExited(String key) {}
//
//            @Override
//            public void onKeyMoved(String key, GeoLocation location) {}
//
//            @Override
//            public void onGeoQueryReady() {}
//
//            @Override
//            public void onGeoQueryError(DatabaseError error) {}
//        });
//
//    }

    //Method to get location from database
    public void getAssignedVOPickupLocation(){
//method is here
DatabaseReference  reference=firebaseDatabase.getReference("ParkArea").child("locations");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){

                    List<Object> map=(List<Object>)dataSnapshot.getValue();

                    double locationLat=0;
                    double locationLong=0;

                    if (map.get(0)!=null){

                        locationLat=Double.parseDouble(map.get(0).toString());
                    }

                    if (map.get(1)!=null){

                        locationLong=Double.parseDouble(map.get(1).toString());
                    }

                    else{
                        System.out.println("FAILED TO GET THE REQUIRED LOCATION");
                    }


                    temp=new LatLng(locationLat,locationLong);

                    ParkLocation=new Location("");
                    ParkLocation.setLatitude(locationLat);
                    ParkLocation.setLongitude(locationLong);


                    System.out.println("LOCATION OF THE NEEDED CUSTOMER: "+temp);

                    if (marker!=null){
                        marker.remove();
                    }

                    marker=mMap.addMarker(new MarkerOptions().position(temp).title("Pickup Location"));

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }


    private void getDeviceLoctation(){
        Log.d(TAG,"getDeviceLocation:getting current Location");
        mFusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);
        try{
            if(mLocationpermissionGranted){
                Task location=mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener(){
                    public void onComplete(@NonNull Task task){
                        if(task.isSuccessful()){
                            Log.d(TAG,"onComplete:Found Location");
                            Location currentLocation=(Location)task.getResult();
                            lastLocationclnew=currentLocation;
                            assert currentLocation != null;
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEAFAULT_ZOOM);


                        }else{
                            Log.d(TAG,"onComplete:current location is null");
                            Toast.makeText(MainActivity.this,"unable to get current location",Toast.LENGTH_SHORT).show();
                        }
                    }


                });
            }

        }catch(SecurityException e){
            Log.e(TAG,"getDeviceLocation:SecurityException: " +e.getMessage());

        }

    }
    private  void moveCamera(LatLng latLng, float zoom){
        Log.d(TAG,"moveCamera:moving the camera to:lat:"+latLng.latitude+",lng:"+latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));
    }




    private void initMap(){
        Log.d(TAG,"initMap:initializingMap");
//        final SupportMapFragment mapFragment=(SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
//        assert mapFragment != null;
//        mapFragment.getMapAsync(EnterPark.this);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    private  void getLocationPermission(){
        Log.d(TAG,"getLocationPermission:getting Location Permission");
        String[]permission = {Manifest.permission.ACCESS_FINE_LOCATION};
        //     Manifest.permission.ACCESS_COARSE_LOCATION};
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            // if(ContextCompat.checkSelfPermission(this.getApplicationContext(),COURSE_LOCATION)== PackageManager.PERMISSION_GRANTED)
            mLocationpermissionGranted=true;
            initMap();
        }
        else{
            ActivityCompat.requestPermissions(this,permission,LOCATION_PERMISSION_REQUEST_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG,"OnRequestPermissionResult:called");
        //mLocationpermissionGranted=false;
        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0 ){
                    for(int i=0; i< grantResults.length;i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationpermissionGranted=false;
                            Log.d(TAG,"onRequestPermissionResult: permissionFailed");
                            return;
                        }
                    }
                    Log.d(TAG,"onRequestPermissionResult: permission granted");
                    mLocationpermissionGranted = true;
                    initMap();

                }



            }
        }
    }

    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this,"Select Your Park Location",Toast.LENGTH_SHORT).show();
        Log.d(TAG,"onMapReady:map is ready");
        mMap = googleMap;
        if(mLocationpermissionGranted){
            getDeviceLoctation();
            mMap.setMyLocationEnabled(true);

        }

     //   getAssignedVOPickupLocation();

    }


}

