package com.sourabh.miniprojectv3;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class DisplayActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMarkerDragListener,
        LocationListener {
    LocationManager locationManager;
    private static final String TAG = DisplayActivity.class.getSimpleName();
    private HashMap<String, Marker> geofenceMarkers = new HashMap<>();
    private LocationRequest locationRequest;
    private GoogleMap mMap;
    TextView locationText;
    public static final int REQUEST_LOCATION_CODE=99;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;
    private Location lastLocation;
    private Marker currentLocationMarker;
    double end_latitude,end_longitude;
    double deviceLat, deviceLong;
    double geofenceLat,geofenceLong;
    float x= (float) 100.00;
    float[] results =new float[10];
    int flg=0;
    private HashMap<String, Marker> userMarkers=new HashMap();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

    }




    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Authenticate with Firebase when the Google map is loaded
        mMap = googleMap;
        mMap.setMaxZoomPreference(16);
        /*  loginToFirebase();
         */
        calculate();
        getDeviceLocations();
        getUpdateFromGeofences();
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
        mMap.setOnMarkerDragListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.getUiSettings().setZoomControlsEnabled(true);

    }


   /* private void loginToFirebase() {
        String email = getString(R.string.firebase_email);
        String password = getString(R.string.firebase_password);
        // Authenticate with Firebase and subscribe to updates
        FirebaseAuth.getInstance().signInWithEmailAndPassword(
                email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    getDeviceLocations();
                    Log.d(TAG, "firebase auth success");
                } else {
                    Log.d(TAG, "firebase auth failed");
                }
            }
        });
    }
*/

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
        String key="";
        for (Device device:userNode.getDevices()){
            deviceLat = device.getLatitude();
            deviceLong = device.getLongitude();
            key=device.getDeviceName();
        }

        Toast.makeText(this,"Co-ordinates: "+ deviceLat +" , "+ deviceLong,Toast.LENGTH_SHORT).show();
        LatLng location = new LatLng(deviceLat, deviceLong);
        /*  listpoints.add(location);*/
        if (!userMarkers.containsKey(key)) {
            userMarkers.put(key, mMap.addMarker(new MarkerOptions().title(key).position(location)));
        } else {
            userMarkers.get(key).setPosition(location);
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : userMarkers.values()) {
            builder.include(marker.getPosition());
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 300));
        int flg=0;
        calculate();

    }
    private void setGeofenceMarkers(DataSnapshot dataSnapshot) {
        // When a location update is received, put or update
        // its value in mMarkers, which contains all the markers
        // for locations received, so that we can build the
        // boundaries required to show them all on the map at once
        String key = dataSnapshot.getKey();
        HashMap<String, Object> value = (HashMap<String, Object>) dataSnapshot.getValue();
        geofenceLat = Double.parseDouble(value.get("latitude").toString());
        geofenceLong = Double.parseDouble(value.get("longitude").toString());
        Toast.makeText(this,"Co-ordinates: "+ geofenceLat +" , "+ geofenceLong,Toast.LENGTH_SHORT).show();
        LatLng location = new LatLng(geofenceLat, geofenceLong);
        /*  listpoints.add(location);*/
        if (!geofenceMarkers.containsKey(key)) {
            geofenceMarkers.put(key, mMap.addMarker(new MarkerOptions().title("Geofence: "+key+"").position(location).
                    icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))));
        } else {
            geofenceMarkers.get(key).setPosition(location);
        }
        mMap.clear();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : geofenceMarkers.values()) {
            builder.include(marker.getPosition());
            CircleOptions circleOptions=new CircleOptions().center(marker.getPosition())
                    .radius(199).fillColor(0x220000ff)
                    .strokeWidth(5.0f);
            mMap.addCircle(circleOptions);
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 300));
        int flg=0;
        calculate();

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.setDraggable(true);
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        end_latitude = marker.getPosition().latitude;
        end_longitude = marker.getPosition().longitude;
        Toast.makeText(this,"Marker drag finished",Toast.LENGTH_SHORT).show();
        calculate();

    }
    public void calculate()
    {
        Location.distanceBetween(deviceLat, deviceLong,geofenceLat,geofenceLong,results);
        double y=0.0;
        y=results[0];
        Toast.makeText(this,"Results: "+y,Toast.LENGTH_SHORT).show();


        if(results[0]>100 & flg<1 && results[0]<200)
        {
            flg=1;
            sendmessage(results[0]);
        }

    }


    public  void sendmessage(float result){
        //Toast.makeText(this,"message send successfully",Toast.LENGTH_LONG).show();
        String number= "8669173297";
        String mess="device is out of zone";
        if(number==null || number.equals("")||mess==null|| mess.equals("")){
            Toast.makeText(this,"field can't be empty",Toast.LENGTH_LONG).show();

        }
        else        {
            if(TextUtils.isDigitsOnly(number)){

                Toast.makeText(this,"message send successfully" +number,Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(this,"please enter integer only",Toast.LENGTH_LONG).show();
            }
        }

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
/*
    public void getDirection() {
        if (listpoints.size() > 2)
            listpoints.clear();
        if (listpoints.size() == 2) {
            String url = getRequestURL(listpoints.get(0),listpoints.get(1));
        }
    }

    private String getRequestURL(LatLng origin, LatLng dest) {
        String str_org="origin"+origin.latitude+origin.longitude;
        String str_dest="Destination"+dest.latitude+origin.longitude;
        String sensor="sensor=false";
        String mode="mode=driving";
        String param=str_org+"&"+str_dest+"&"+sensor+"&"+mode;
        String output="json";
        String url="https://maps.googleapis.com/maps/directions/"+output+"?"+param;
        return url;

    }
*/

}
