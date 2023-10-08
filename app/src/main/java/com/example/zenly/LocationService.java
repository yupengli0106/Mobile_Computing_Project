package com.example.zenly;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.BatteryManager;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.*;

public class LocationService extends Service {

    private FusedLocationProviderClient myFusedLocationClient;
    private LocationCallback myLocationCallback;

    private Location lastLocation;  // last location of the user
    private long lastUpdateTime;  // last time the location is updated (in milliseconds)
    private static final float MIN_DISTANCE = 10;  // minimum distance threshold (in meters)
    private static final long MIN_TIME = 5000;  // minimum time threshold (in milliseconds)
    private static final FirebaseHelper db = FirebaseHelper.getInstance();

    @Override
    public void onCreate() {
        super.onCreate();

        myFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // create location callback to get location updates
        myLocationCallback = new LocationCallback() {

            // get new location when location is updated
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // get new location
                Location newLocation = locationResult.getLastLocation();
                // get battery level
                BatteryManager batteryManager = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);
                int batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);


                if (newLocation != null) {
                    if (lastLocation == null) {
                        lastLocation = newLocation;
                        lastUpdateTime = System.currentTimeMillis();
                    } else {
                        float distance = newLocation.distanceTo(lastLocation);  // 计算新旧位置之间的距离
                        long currentTime = System.currentTimeMillis();
                        long timeDifference = currentTime - lastUpdateTime;  // 计算时间差

                        // satisfy both distance and time threshold to upload location data to firebase
                        if (distance >= MIN_DISTANCE || timeDifference >= MIN_TIME) {
                            LocationData locationData = new LocationData(
                                    newLocation.getLatitude(),
                                    newLocation.getLongitude(),
                                    newLocation.getSpeed(),
                                    currentTime,
                                    batteryLevel
                            );
                            // upload location data to firebase
                            db.uploadLocation(locationData);

                            // update last location and last update time
                            lastLocation = newLocation;
                            lastUpdateTime = currentTime;
                        }
                    }
                }

            }

            // check if location is available when location availability changes
            @Override
            public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
                if (!locationAvailability.isLocationAvailable()) {
                    // location is not available, stop location updates
                    myFusedLocationClient.removeLocationUpdates(myLocationCallback);
                }
            }
        };

        // start location updates when service is created (app is started)
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        LocationRequest mLocationRequest = new LocationRequest();
        // Set the interval for location updates (in milliseconds)
        // This means the app will receive updates at most every 10 seconds
        mLocationRequest.setInterval(10000);
        // Set the fastest interval for location updates (in milliseconds)
        // If other apps request more frequent updates, your app will also receive them, but at most every 5 seconds
        mLocationRequest.setFastestInterval(5000);
        // Set the priority of the location request
        // PRIORITY_HIGH_ACCURACY will try to use GPS for higher accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // location permission is denied, do not update user location
            return;
        } else {
            // location permission is granted, start location service
            myFusedLocationClient.requestLocationUpdates(mLocationRequest, myLocationCallback, Looper.myLooper());
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myFusedLocationClient.removeLocationUpdates(myLocationCallback);
    }
}

