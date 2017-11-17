package com.salejung_android;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xotjr on 2017-11-15.
 */

public class LocationForFCMActivity extends FragmentActivity implements OnMapReadyCallback {

    final String TAG = "LocationForFCMActivity";
    // why double can't has null value?? So I choose.

    private double LOCATION_NULL = 999.999;
    final String FCM_DB_COLLECTION_NAME = "fcm_topic";
    final String FCM_DB_FIELD_NAME = "topic";

    private GoogleMap mMap;
    private static final int MY_LOCATION_REQUEST_CODE = 2;

    // Saved at SharedPreferences.
    private double mLat = LOCATION_NULL;
    private double mLng = LOCATION_NULL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_for_subscribe);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
        } else {
            // No user is signed in

            Intent intent = new Intent(LocationForFCMActivity.this, LoginActivity.class);
            intent.putExtra("returnActivity", "LocationForFCMActivity");
            startActivity(intent);
            finish();
            return;
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_setLoaction);
        mapFragment.getMapAsync(this);


        /*
        Save user's new FCM subscribe topic info to DB which is Firestore.
        Saved Topic Info used to unsubscribe previous topic when user change subscribe topic to new one.
        subscribe topic change scenario : when user change topic, FCM server should unsubscribe previous topic and then subscribe new topic.
        */

        final Button btn = findViewById(R.id.btn_regist_fcm_topic);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLng != LOCATION_NULL && mLat != LOCATION_NULL) {
                    /*
                    Make new topic to subscribe FCM.
                    Topic is coordinate value. Sum of string lng , lat.
                    FCM notification send massage to near place.
                     Topic is divided by 500m distnace which is coordinate 0.005.
                    */
                    FCMTopicMaker topicMaker = new FCMTopicMaker(mLng, mLat);
                    String newTopic = topicMaker.makeTopic();

                    if (newTopic == null) {
                        Log.e(TAG, "newTopic is null");
                    }

                    Log.d(TAG, "newTopic : " + newTopic);

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    String userId = null;
                    if (user != null) {
                        userId = user.getUid();
                    } else {
                        Log.e(TAG, "user is null");
                    }

                    // Access a Cloud Firestore instance from your Activity
                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    //set
                    Map<String, String> data = new HashMap<>();
                    data.put(FCM_DB_FIELD_NAME, newTopic);

                    if (userId != null){
                        // set userFCMTopicInfo
                        changeOrAddFCMTopic(db, FCM_DB_COLLECTION_NAME, userId, data);
                    } else {
                        Log.e(TAG, "userId is null");
                    }

                } else {
                    // TODO
                    Log.e("Coordinate error", "mLat mLng is null");
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
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
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

    /*
    subscribe topic change scenario : when user request add or change topic, first check topic info DB.
    If there is a previous subscribe topic info in DB, unsubscribe previous topic, update topic info DB and subscribe to new topic.
    If there is no previous subscribe topic info in DB, skip unsubscribtion, update topic info DB and subscribe to new topic.
    */
    private void changeOrAddFCMTopic(final FirebaseFirestore db, final String collectionName, final String documentName, final Map<String, String> data) {

        DocumentReference docRef = db.collection(collectionName).document(documentName);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + task.getResult().getData());
                        String userTopic = (String) task.getResult().getData().get(FCM_DB_FIELD_NAME);
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(userTopic);
                        subscribeFCMTopicAfterSaveTopicInfo(db, collectionName, documentName, data);
                    } else {
                        Log.d(TAG, "No such document");
                        subscribeFCMTopicAfterSaveTopicInfo(db, collectionName, documentName, data);
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }


    // Save user's new FCM subscribe topic info to DB which is Firestore.
    // And subscribe to new topic
    private void subscribeFCMTopicAfterSaveTopicInfo(final FirebaseFirestore db, final String collectionName, final String documentName, final Map<String, String> data) {
        db.collection(collectionName).document(documentName)
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        FirebaseMessaging.getInstance().subscribeToTopic(data.get(FCM_DB_FIELD_NAME));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }
}