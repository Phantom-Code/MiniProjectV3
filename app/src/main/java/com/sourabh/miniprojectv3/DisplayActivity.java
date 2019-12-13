package com.sourabh.miniprojectv3;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ThrowOnExtraProperties;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class DisplayActivity extends FragmentActivity implements OnMapReadyCallback{

    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";
    private static final String TAG = DisplayActivity.class.getSimpleName();

    private HashMap<String, Marker> geofenceMarkers = new HashMap<>();
    private GoogleMap mMap;
    double end_latitude,end_longitude;
    double deviceLat, deviceLong;
    double geofenceLat,geofenceLong;
    Button addButton;
    float x= (float) 100.00;
    float[] results =new float[10];

    String message="";
    String key="";
    static int enteredCnt=0;
    static int exitCnt=0;
    private HashMap<String, Marker> userMarkers=new HashMap();
    private PendingIntent pendingIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
        addButton =findViewById(R.id.addGeofence);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // Toast.makeText(getApplicationContext(),"Clicked Add Button",Toast.LENGTH_LONG).show();
                startActivity(new Intent(DisplayActivity.this,JobActivity.class));
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMaxZoomPreference(18);

        getDeviceLocations();
        getUpdateFromGeofences();
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        mMap.getUiSettings().setZoomControlsEnabled(true);



    }


    private void getUpdateFromGeofences() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Accounts/username/Geo_Locations");
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                setGeofenceMarkers(dataSnapshot);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                setGeofenceMarkers(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.d(TAG, "Failed to read value.", error.toException());
            }
        });
    }


    private void getDeviceLocations() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Accounts/username");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot != null) {
                    setUserDeviceMarkers(dataSnapshot.getValue(UserNode.class));
                } else {
                    Toast.makeText(getApplicationContext(), "Username/Password Invalid", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Something fishy happened! Sorry for the inconvenience!", Toast.LENGTH_SHORT).show();
            }
        });
    }




    private void setUserDeviceMarkers(Object dataSnapshot) {
        UserNode userNode=(UserNode)dataSnapshot;
        for (Device device:userNode.getDevices()){
            deviceLat = device.getLatitude();
            deviceLong = device.getLongitude();
            key=device.getDeviceId();


        //Toast.makeText(this,"Co-ordinates: "+ deviceLat +" , "+ deviceLong,Toast.LENGTH_SHORT).show();
        LatLng location = new LatLng(deviceLat, deviceLong);

        if (!userMarkers.containsKey(key)) {
            userMarkers.put(key, mMap.addMarker(new MarkerOptions().title("Device:"+key).position(location)));
        } else {
            userMarkers.get(key).setPosition(location);
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : userMarkers.values()) {
            builder.include(marker.getPosition());
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 300));

        }
        calculate(key);

    }
    private void setGeofenceMarkers(DataSnapshot dataSnapshot) {

        String key = dataSnapshot.getKey();
        HashMap<String, Object> value = (HashMap<String, Object>) dataSnapshot.getValue();
        geofenceLat = Double.parseDouble(value.get("latitude").toString());
        geofenceLong = Double.parseDouble(value.get("longitude").toString());

        LatLng location = new LatLng(geofenceLat, geofenceLong);
        if (!geofenceMarkers.containsKey(key)) {
            geofenceMarkers.put(key, mMap.addMarker(new MarkerOptions().title("Job Area: "+key+"").position(location).
                    icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))));
        } else {
            geofenceMarkers.get(key).setPosition(location);
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : geofenceMarkers.values()) {
            builder.include(marker.getPosition());
            CircleOptions circleOptions=new CircleOptions().center(marker.getPosition())
                    .radius(99).fillColor(0x220000ff)
                    .strokeWidth(5.0f);
            mMap.addCircle(circleOptions);
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 300));

        calculate(key);

    }

    public void calculate(String key)
    {

        Location.distanceBetween(deviceLat, deviceLong,geofenceLat,geofenceLong,results);
        double y=0.0;
        y=results[0];



        if(results[0]>=0 && results[0]<100)
        {
            exitCnt=0;

            if(enteredCnt<1){
            message="Entered in Geofence";
            createNotificationChannel(message,key);
                enteredCnt++;
            }

        }
        if(results[0]>100){
            enteredCnt=0;

            if(exitCnt<1){
            message="Out of Geofence";
            createNotificationChannel(message,key);
                exitCnt++;}
        }



    }




    private void createNotificationChannel(String message,String key) {

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notification";
            String description = "First Notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("Geofence", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,"Geofence")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(message)
                .setContentTitle(key)
                .setAutoCancel(true)

                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if(notificationManager!=null)
            notificationManager.notify(001, notificationBuilder.build());
    }


}
