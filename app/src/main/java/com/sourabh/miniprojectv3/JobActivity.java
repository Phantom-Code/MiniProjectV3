package com.sourabh.miniprojectv3;



import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;


public class JobActivity extends AppCompatActivity implements OnMapReadyCallback {
    private  GoogleMap myMap;
    EditText mEditText;
    double latitude,longitude;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_job);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        mEditText = findViewById(R.id.editText);

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        myMap=googleMap;

    }
    public void gotoLocation(double latitude,double longitude,int zoom){
        LatLng latLng =new LatLng(latitude,longitude);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng,zoom);
        myMap.moveCamera(update);
        myMap.addMarker(new MarkerOptions().title("Yes").position(latLng));
    }

    public void findOnMap(View view) {
        Geocoder geocoder =new Geocoder(this);
        try {
            List<Address> myList = geocoder.getFromLocationName(mEditText.getText().toString(),1);
            Address address = myList.get(0);
            String locality = address.getLocality();
            Toast.makeText(getApplicationContext(),locality,Toast.LENGTH_SHORT).show();
            latitude=address.getLatitude();
            longitude=address.getLongitude();
            gotoLocation(latitude,longitude,15);
        }catch (IOException e){

        }
    }

    public void addGeofence(View view) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Accounts/username/Geo_Locations/0");
        ref.child("/longitude").setValue(longitude);
        ref.child("/latitude").setValue(latitude);
    }

    public void backToDisplay(View view) {
        startActivity(new Intent(JobActivity.this,DisplayActivity.class));
    }
}
