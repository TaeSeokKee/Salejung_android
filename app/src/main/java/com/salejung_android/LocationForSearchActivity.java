package com.salejung_android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.DecimalFormat;

/**
 * Created by xotjr on 2017-11-09.
 */

public class LocationForSearchActivity extends FragmentActivity implements OnMapReadyCallback {

    // why double can't has null value?? So I choose.
    private double LOCATION_NULL = 9999.9999;

    private GoogleMap mMap;
    private static final int MY_LOCATION_REQUEST_CODE = 2;

    // Saved at SharedPreferences.
    private double mLat = LOCATION_NULL;
    private double mLng = LOCATION_NULL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_for_search);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
        } else {
            // No user is signed in
            Intent intent = new Intent(LocationForSearchActivity.this, LoginActivity.class);
            intent.putExtra("returnActivity","LocationForSearchActivity");
            startActivity(intent);
            finish();
            return;
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_setLoaction);
        mapFragment.getMapAsync(this);

        // go to next step. (take photo)
        final Button btn = findViewById(R.id.btn_upload_stage_1);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLat != LOCATION_NULL && mLng != LOCATION_NULL) {
                    SharedPreferences sharedPreferences = getSharedPreferences("Coordinate", 0);
                    SharedPreferences.Editor spEditor = sharedPreferences.edit();
                    DecimalFormat form = new DecimalFormat("#.######");
                    spEditor.putString("lat", form.format(mLat));
                    spEditor.putString("lng", form.format(mLng));

                    spEditor.apply();

                    Intent intent = new Intent(LocationForSearchActivity.this, SearchActivity.class);
                    startActivity(intent);
                } else {
                    // TODO :
                }
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            if (permissions.length == 1 &&
                    permissions[0] == android.Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
                mMap.setMyLocationEnabled(true);
            } else {
                // Todo : Permission was denied. Display an error message.
            }
        }
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
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        // If there is no location permission, try to get location permission.
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_LOCATION_REQUEST_CODE);
        }

        // set location data. (lat, lng)
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
        {
            @Override
            public void onMapClick(LatLng point)
            {
                mMap.clear();
                mMap.addMarker(new MarkerOptions()
                        .position(point));

                mLat = point.latitude;
                mLng = point.longitude;
            }
        });

        // TODO : Get latest location from device and set to mLat, mLng variables.
        // There is 2 reason.
        // First, There is a case that User may touch "next button" at first time. In that case, mLat, mLng have trash values.
        // Second, For User can find present location more easily.
    }
}