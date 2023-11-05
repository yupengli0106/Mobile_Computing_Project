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
import android.widget.TextView;
import android.widget.Toast;

import com.example.activities.LoginActivity;
import com.example.managers.FriendManager;
import com.example.model.Friend;
import com.example.zenly.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MapFragment extends Fragment {
    private final String TAG = "MapFragmentLog";
    // GoogleMap object
    private GoogleMap myMap;
    // ValueEventListener to listen for changes in the database
    private ValueEventListener myValueEventListener;
    // use a HashMap to store the markers of all users
    private final HashMap<String, Marker> userMarkers = new HashMap<>();
    // use a HashMap to store the usernames of all users to avoid querying the database multiple times
    private final HashMap<String, String> usernameCache = new HashMap<>();
    // default zoom level of the map when the app is first loaded (street level)
    private final float DEFAULT_ZOOM_LEVEL = 15.0f;
    // Firebase Realtime Database reference to the locations node
    private DatabaseReference userRef;
    private DatabaseReference locationsRef;
    // path to the locations node in Firebase Realtime Database
    private static final String LOCATIONS_PATH = "locations";
    // path to the users node in Firebase Realtime Database
    private static final String USERS_PATH = "users";
    // boolean to check if it is the first load
    private boolean isFirstLoad = true;
    // Firebase user
    private FirebaseUser currentUser = null;
    private FriendManager friendManager;

    // callback method for when the map is ready
    private final OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be
         * prompted to
         * install it inside the SupportMapFragment. This method will only be triggered
         * once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
            myMap = googleMap;
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.getUiSettings().setAllGesturesEnabled(true);

            myMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(@NonNull Marker marker) {
                    return null; // Use default window frame
                }

                @Override
                public View getInfoContents(@NonNull Marker marker) {
                    // Inflate custom layout
                    View view = getLayoutInflater().inflate(R.layout.marker_info_window, null);

                    // Find TextViews in custom layout
                    TextView title = view.findViewById(R.id.title);
                    TextView snippet = view.findViewById(R.id.snippet);

                    // Set content
                    title.setText(marker.getTitle());
                    snippet.setText(marker.getSnippet());

                    return view;
                }
            });

            startRepeatingTask();
        }
    };

    public MapFragment() {
    }

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
        currentUser = myAuth.getCurrentUser();
        friendManager = FriendManager.getInstance();

        if (currentUser != null) {  // check if the user is logged in
            // get the reference to the locations node
            locationsRef = myDatabase.child(LOCATIONS_PATH);
            // get the reference to the user node
            userRef = myDatabase.child(USERS_PATH);
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

    /**
     * Start the repeating task to listen for changes in the database
     */
    private void startRepeatingTask() {
        Log.d(TAG, "startRepeatingTask: start repeating task");
        myValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // get the list of friends of the current user
                List<Friend> currentFriends = friendManager.getFriendsList().getValue();
                // use a HashSet to store the user IDs of all friends
                Set<String> friendIds = new HashSet<>();

                if (currentFriends != null) {
                    for (Friend friend : currentFriends) {
                        // add the user ID of each friend to the HashSet
                        friendIds.add(friend.getUserId());
                    }
                }

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    if (userId != null) {
                        if (currentUser != null && userId.equals(currentUser.getUid())) {
                            handleNewLocation(userSnapshot);  // update the current user's location
                        } else if (friendIds.contains(userId)) {
                            handleNewLocation(userSnapshot);  // update the location of friends
                        }
                    }
                }
            }

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
     * Handle new location of a user
     * @param userSnapshot snapshot of the user
     */
    private void handleNewLocation(@NonNull DataSnapshot userSnapshot) {
        // get the user ID
        String userId = userSnapshot.getKey();

        // get the user's location
        Double latitudeValue = userSnapshot.child("latitude").getValue(Double.class);
        double latitude = (latitudeValue != null) ? latitudeValue : 0.0;
        Double longitudeValue = userSnapshot.child("longitude").getValue(Double.class);
        double longitude = (longitudeValue != null) ? longitudeValue : 0.0;
        Float speedValue = userSnapshot.child("speed").getValue(Float.class);
        float speed = (speedValue != null) ? speedValue : 0.0f;
        Long timestampValue = userSnapshot.child("timestamp").getValue(Long.class);
        long timestamp = (timestampValue != null) ? timestampValue : 0L;// TODO: last update time
        Integer batteryLevelValue = userSnapshot.child("batteryLevel").getValue(Integer.class);
        int batteryLevel = (batteryLevelValue != null) ? batteryLevelValue : 0;

        // create a LatLng object from the latitude and longitude values
        LatLng newLocation = new LatLng(latitude, longitude);

        // get the username of the user
        DatabaseReference specificUserRef = userRef.child(userId);
        specificUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //TODO: TBD if the username can be changed and need to update the usernameCache
                String username = usernameCache.get(userId);
                if (username == null) {
                    username = dataSnapshot.child("username").getValue(String.class);
                    usernameCache.put(userId, username);
                }
                // update the marker on the map after getting the username
                updateMapMarker(userId, newLocation, DEFAULT_ZOOM_LEVEL, username, speed, batteryLevel);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "get username onCancelled: " + databaseError.getMessage());
            }
        });
    }


    /**
     * Handle new location of a user
     * 
     * @param userSnapshot snapshot of the user
     */
    private void handleNewLocation(@NonNull DataSnapshot userSnapshot) {
        // get the user ID
        String userId = userSnapshot.getKey();

        // get the user's location
        Double latitudeValue = userSnapshot.child("latitude").getValue(Double.class);
        double latitude = (latitudeValue != null) ? latitudeValue : 0.0;
        Double longitudeValue = userSnapshot.child("longitude").getValue(Double.class);
        double longitude = (longitudeValue != null) ? longitudeValue : 0.0;
        Float speedValue = userSnapshot.child("speed").getValue(Float.class);
        float speed = (speedValue != null) ? speedValue : 0.0f;
        Long timestampValue = userSnapshot.child("timestamp").getValue(Long.class);
        long timestamp = (timestampValue != null) ? timestampValue : 0L;// TODO: last update time
        Integer batteryLevelValue = userSnapshot.child("batteryLevel").getValue(Integer.class);
        int batteryLevel = (batteryLevelValue != null) ? batteryLevelValue : 0;

        // create a LatLng object from the latitude and longitude values
        LatLng newLocation = new LatLng(latitude, longitude);

        // get the username of the user
        DatabaseReference specificUserRef = userRef.child(userId);
        specificUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // TODO: TBD if the username can be changed and need to update the usernameCache
                String username = usernameCache.get(userId);
                if (username == null) {
                    username = dataSnapshot.child("username").getValue(String.class);
                    usernameCache.put(userId, username);
                }
                // update the marker on the map after getting the username
                updateMapMarker(userId, newLocation, DEFAULT_ZOOM_LEVEL, username, speed, batteryLevel);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "get username onCancelled: " + databaseError.getMessage());
            }
        });
    }

    /**
     * Update the marker on the map
     * @param userId the user ID
     * @param newLocation new location of the user
     * @param zoomLevel zoom level of the map
     * @param username username of the user
     * @param speed speed of the user
     * @param batteryLevel battery level of the user's device
     */
    private void updateMapMarker(String userId, LatLng newLocation, float zoomLevel, String username, float speed, int batteryLevel) {
        if (myMap != null) {
            Marker existingMarker = userMarkers.get(userId); // get the existing marker
            if (existingMarker != null) {
                existingMarker.setPosition(newLocation); // update the existing marker's position
                existingMarker.setTitle("User: " + username);
                existingMarker.setSnippet("Speed: " + speed + "\n" + "Battery: " + batteryLevel + "%");
                existingMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            } else {
                Marker newMarker = myMap.addMarker(new MarkerOptions()
                        .position(newLocation)
                        .title("User: " + username)
                        .snippet("Speed: " + speed + "\n" + "Battery: " + batteryLevel + "%"));
                userMarkers.put(userId, newMarker);
            }
            if (currentUser != null && userId.equals(currentUser.getUid()) && isFirstLoad) {
                myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLocation, zoomLevel));
                isFirstLoad = false;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // start the repeating task again when the app is resumed
        // user switches back to the app
        isFirstLoad = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove the listener using the member variable to avoid memory leaks
        // foreground service will keep running even if the app is closed
        //TODO: foreground service will be stopped when the user logs out
//        if (myValueEventListener != null && locationsRef != null) {
//            locationsRef.removeEventListener(myValueEventListener);
//        }
    }

}