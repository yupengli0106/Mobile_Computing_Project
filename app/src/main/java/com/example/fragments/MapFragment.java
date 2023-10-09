package com.example.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.activities.LoginActivity;
import com.example.model.LocationData;
import com.example.zenly.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.android.gms.maps.model.Marker;


import java.util.HashMap;

public class MapFragment extends Fragment {
    private static final String TAG = "MapFragmentLog";
    // GoogleMap object
    private GoogleMap myMap;
    // ValueEventListener to listen for changes in the database
    private ValueEventListener myValueEventListener;
    // use a HashMap to store the markers of all users
    private final HashMap<String, Marker> userMarkers = new HashMap<>();
    // default zoom level of the map
    private static final float DEFAULT_ZOOM_LEVEL = 16.0f;
    // Firebase Realtime Database reference to the locations node
    private DatabaseReference locationsRef;
    // path to the locations node in Firebase Realtime Database
    private static final String LOCATIONS_PATH = "locations";


    // callback method for when the map is ready
    private final OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
            myMap = googleMap;
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.getUiSettings().setAllGesturesEnabled(true);

            LatLng initialLocation = new LatLng(-34, 151);
            myMap.addMarker(new MarkerOptions().position(initialLocation).title("Initial Position"));
            myMap.moveCamera(CameraUpdateFactory.newLatLng(initialLocation));
            startRepeatingTask();
        }
    };

    public MapFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        DatabaseReference myDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth myAuth = FirebaseAuth.getInstance();
        FirebaseUser user = myAuth.getCurrentUser();

        if (user != null) {  // check if the user is logged in
            String uid = user.getUid();
            locationsRef = myDatabase.child(LOCATIONS_PATH).child(uid);
        } else {
            // user is not logged in redirect to login page
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            // go to login page
            Intent intent = new Intent(getContext(), LoginActivity.class);
            startActivity(intent);
            return;
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    private void startRepeatingTask() {
        Log.d(TAG, "startRepeatingTask: start repeating task");
        myValueEventListener = new ValueEventListener() {
            /**
             * Called when data at a location changes.
             * @param dataSnapshot The current data at the location
             */
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) { // traverse all users

                    // get the location data of the user
                    Double latitudeValue = dataSnapshot.child("latitude").getValue(Double.class);
                    double latitude = (latitudeValue != null) ? latitudeValue : 0.0;
                    Double longitudeValue = dataSnapshot.child("longitude").getValue(Double.class);
                    double longitude = (longitudeValue != null) ? longitudeValue : 0.0;
                    Float speedValue = dataSnapshot.child("speed").getValue(Float.class);
                    float speed = (speedValue != null) ? speedValue : 0.0f;
                    Long timestampValue = dataSnapshot.child("timestamp").getValue(Long.class);
                    long timestamp = (timestampValue != null) ? timestampValue : 0L;
                    Integer batteryLevelValue = dataSnapshot.child("batteryLevel").getValue(Integer.class);
                    int batteryLevel = (batteryLevelValue != null) ? batteryLevelValue : 0;

                    // create a LocationData object
                    LocationData locationData = new LocationData(latitude, longitude, speed, timestamp, batteryLevel);

                    String userId = userSnapshot.getKey();
                    if (userId != null) {
                        // update the marker on the map if the user exists
                        LatLng newLocation = new LatLng(locationData.getLatitude(), locationData.getLongitude());
                        updateMapMarker(userId, newLocation, DEFAULT_ZOOM_LEVEL);
                    }
                }
            }

            /**
             * Called when the read is cancelled.
             * @param databaseError A description of the error that occurred
             */
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Log error message
                Log.d(TAG, "onCancelled: " + databaseError.getMessage());
                Toast.makeText(getContext(), "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        };
        // Add the ValueEventListener
        if (locationsRef != null) {
            locationsRef.addValueEventListener(myValueEventListener);
        }
    }

    /**
     * Update the marker on the map
     * @param userId the user id
     * @param newLocation the new location of the user
     * @param zoomLevel the zoom level of the map
     */
    private void updateMapMarker(String userId, LatLng newLocation, float zoomLevel) {
        if (myMap != null) {
            Marker existingMarker = userMarkers.get(userId); // get the existing marker
            if (existingMarker != null) {
                existingMarker.setPosition(newLocation);
            } else {
                Marker newMarker = myMap.addMarker(new MarkerOptions().position(newLocation).title("User " + userId));
                userMarkers.put(userId, newMarker); // add the new marker to the HashMap
            }
            myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLocation, zoomLevel));
        }
    }

    /**
     * Stop the repeating task when the activity is destroyed
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove the listener using the member variable
        if (myValueEventListener != null && locationsRef != null) {
            locationsRef.removeEventListener(myValueEventListener);
        }
    }

}