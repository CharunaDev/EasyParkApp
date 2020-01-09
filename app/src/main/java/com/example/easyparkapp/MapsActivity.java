package com.example.easyparkapp;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;

import com.example.easyparkapp.Interface.IOnLoadLocationListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GeoQueryEventListener, IOnLoadLocationListener {

    private GoogleMap mMap;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker currentUser;
    private DatabaseReference myLocationRef;
    private GeoFire geoFire;
    private List<LatLng> parkArea;
    private IOnLoadLocationListener listener;
    private DatabaseReference locations;
    private DatabaseReference athukorala;
    private Location lastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                        buildLocationRequest();
                        buildLocationCallback();
                        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapsActivity.this);

                       initArea(); 
                       settingGeoFire();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MapsActivity.this, "You must enable permission", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();

    }

   

    private void initArea() {



        locations =  FirebaseDatabase.getInstance()
                .getReference("ParkArea")
                .child("locations");
        listener = this;


       //Load locations from firebase


                locations.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<MLatLng> latLngList = new ArrayList<>();
                        for(DataSnapshot locationSnapShot: dataSnapshot.getChildren()){
                            MLatLng latLng = locationSnapShot.getValue(MLatLng.class);
                            latLngList.add(latLng);
                        }
                        listener.onLoadLocationSuccess(latLngList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                      listener.onLoadLocationFailed(databaseError.getMessage());
                    }
                });
                locations.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //update parking areas

                        List<MLatLng> latLngList = new ArrayList<>();
                        for(DataSnapshot locationSnapShot: dataSnapshot.getChildren()){
                            MLatLng latLng = locationSnapShot.getValue(MLatLng.class);
                            latLngList.add(latLng);
                        }
                        listener.onLoadLocationSuccess(latLngList);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

      /* FirebaseDatabase.getInstance()
                .getReference("ParkArea")
                .child("kegalle")
                .child("athukorala")
                .setValue(parkArea)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(MapsActivity.this, "Updated!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MapsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }); */
    }

    private void addUserMarker() {
        geoFire.setLocation("You", new GeoLocation(lastLocation.getLatitude(),
                lastLocation.getLongitude()), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if (currentUser != null) currentUser.remove();
                currentUser = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lastLocation.getLatitude(),
                                lastLocation.getLongitude()))
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        .title("You"));
                //move camera
                mMap.animateCamera(CameraUpdateFactory
                        .newLatLngZoom(currentUser.getPosition(),12.0f));
            }
        });
    }

    private void settingGeoFire() {

        myLocationRef = FirebaseDatabase.getInstance().getReference("MyLocation");
        geoFire = new GeoFire(myLocationRef);
    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback(){
         public void onLocationResult(final LocationResult locationResult){
             if (mMap != null){

                 lastLocation = locationResult.getLastLocation();

                //add our location
               addUserMarker();
             }
         }
        };
    }

    private void buildLocationRequest(){
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);

}

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

     mMap.getUiSettings().setZoomControlsEnabled(true);

     if (fusedLocationProviderClient != null)
         fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

     //Add marks for parks
       addParkArea();
    }

    private void addParkArea() {
       for (LatLng latLng : parkArea){
        /*   mMap.addCircle(new CircleOptions().center(latLng)
                    .clickable(true)
                   .radius(200) //200m
                    .strokeColor(Color.BLUE)
                    .fillColor(0x220000FF) //22 is transparent code
                    .strokeWidth(5.0f)
            ); */

    if (!(parkArea.get(0) == null)) {
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Parking")
                .snippet("Rs.100/per Hour")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker2)));


    }

           else if (!(parkArea.get(1) == null)) {
               mMap.addMarker(new MarkerOptions()
                       .position(latLng)
                       .title("Parking")
                       .snippet("Rs.200/per Hour")
                       .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker2)));


           }

    else if (!(parkArea.get(2) == null)) {
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Parking")
                .snippet("Rs.400/per Hour")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker2)));


    }

            /*Bitmap.Config conf = Bitmap.Config.ARGB_8888;
            Bitmap bmp = Bitmap.createBitmap(80, 80, conf);
            Canvas canvas1 = new Canvas(bmp);

// paint defines the text color, stroke width and size
            Paint color = new Paint();
            color.setTextSize(35);
            color.setColor(Color.BLACK);

// modify canvas
            canvas1.drawBitmap(BitmapFactory.decodeResource(getResources(),
                    R.drawable.marker1), 0,0, color);
            canvas1.drawText("User Name!", 30, 40, color);

         /*   mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromBitmap(bmp))
                    // Specifies the anchor to be at a particular point in the marker image.
                    .anchor(0.5f, 1)); */

        //Create GeoQuery when user in parking location
     /*   GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(latLng.latitude, latLng.longitude), 0.5f); //500m
        geoQuery.addGeoQueryEventListener(MapsActivity.this); */

        }
    }

    @Override
    protected void onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onStop();
    }

    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        sendNotification("EDMTDev",String.format("%s nearby parking available",key));
    }

    @Override
    public void onKeyExited(String key) {
        sendNotification("EDMTDev",String.format("%s going away from nearby parking available",key));
    }



    @Override
    public void onKeyMoved(String key, GeoLocation location) {
        sendNotification("EDMTDev",String.format("%s nearby parking available",key));
    }

    @Override
    public void onGeoQueryReady() {

    }

    @Override
    public void onGeoQueryError(DatabaseError error) {
       Toast.makeText(this,""+error.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private void sendNotification(String title, String content) {
        String NOTIFICATION_CHANNEL_ID = "edmt_multiple_location";
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,"EasyPark Notification",
                    NotificationManager.IMPORTANCE_DEFAULT);

            //Config
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(false)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));

        Notification notification = builder.build();
        notificationManager.notify(new Random() .nextInt(),notification);
    }

    @Override
    public void onLoadLocationSuccess(List<MLatLng> latLngs) {
        parkArea = new ArrayList<>();
        for (MLatLng mLatLng : latLngs){
            LatLng convert = new LatLng(mLatLng.getLatitude(),mLatLng.getLongitude());
            parkArea.add(convert);

        }

   //Load map after get the all parking spots
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.parking);
        mapFragment.getMapAsync(MapsActivity.this);
        //clear the map
        if (mMap != null){
            mMap.clear();
            //Add user marker
            addUserMarker();

            //Add Parking Areas
            addParkArea();
        }

    }

    @Override
    public void onLoadLocationFailed(String message) {
        Toast.makeText(this, ""+message, Toast.LENGTH_SHORT).show();
    }
}
