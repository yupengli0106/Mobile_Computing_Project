package com.example.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.helpers.FirebaseHelper;
import com.example.model.LocationData;
import com.google.android.gms.location.*;

public class LocationService extends Service {
    private final String TAG = "LocationServiceLog";
    private FusedLocationProviderClient myFusedLocationClient; // location client to get location updates
    private LocationCallback myLocationCallback; // location callback to get location updates
    private Location lastLocation;  // last location of the user
    private long lastUpdateTime;  // last time the location is updated (in milliseconds)
    private static final float MIN_DISTANCE = 10;  // minimum distance threshold (in meters)
    private static final long MIN_TIME = 5000;  // minimum time threshold (in milliseconds)
    private static final FirebaseHelper db = FirebaseHelper.getInstance(); // get instance of FirebaseHelper singleton class
    private static final int NOTIFICATION_ID = 1; // notification id for foreground service
    private static final String CHANNEL_ID = "ForegroundServiceChannel"; // notification channel id for foreground service

    @Override
    public void onCreate() {
        super.onCreate();

        // create notification channel for foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            // create notification channel for foreground service if it does not exist
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        // initialize location client
        myFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // create location callback to get location updates
        myLocationCallback = new LocationCallback() {
            /**
             * Called when the location has changed.
             * @param locationResult The new location, as a Location object.
             */
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // get new location
                Location newLocation = locationResult.getLastLocation();
                // get battery level
                BatteryManager batteryManager = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);
                int batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

                // check if new location is available
                if (newLocation != null) {
                    if (lastLocation == null) {
                        lastLocation = newLocation;
                        lastUpdateTime = System.currentTimeMillis();
                    } else {
                        // calculate distance between two locations
                        float distance = newLocation.distanceTo(lastLocation);
                        long currentTime = System.currentTimeMillis();
                        // calculate time difference between two locations
                        long timeDifference = currentTime - lastUpdateTime;

                        // satisfy both distance and time threshold to upload location data to firebase
                        if (distance >= MIN_DISTANCE || timeDifference >= MIN_TIME) {
                            LocationData locationData = new LocationData(
                                    newLocation.getLatitude(),
                                    newLocation.getLongitude(),
                                    newLocation.getSpeed(),
                                    currentTime,
                                    batteryLevel
                            );

                            try {
                                // upload location data to firebase
                                db.uploadLocation(locationData);
                            }catch (Exception e){
                                Log.d(TAG, "Location updated Failed: " + e.getMessage());
                            }

                            // update last location and last update time
                            lastLocation = newLocation;
                            lastUpdateTime = currentTime;
                        }
                    }
                }

            }

            /**
             * Called when the availability of the location information changes.
             * @param locationAvailability the availability of the location information
             */
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

    /**
     * Start location updates
     */
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
            Log.d(TAG, "startLocationUpdates: location permission is denied");
            return;
        } else {
            // location permission is granted, start location service
            myFusedLocationClient.requestLocationUpdates(mLocationRequest, myLocationCallback, Looper.myLooper());
            Log.d(TAG, "startLocationUpdates: location permission is granted, initialize location service");
        }

    }

    /**
     * Called by the system every time a client explicitly starts the service by calling
     * @param intent The Intent that was used to bind to this service,
     * as given to {@link android.content.Context#bindService
     * Context.bindService}.  Note that any extras that were included with
     * the Intent at that point will <em>not</em> be seen here.
     *
     * @return The communication channel to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        myFusedLocationClient.removeLocationUpdates(myLocationCallback);
    }

    /**
     * Called by the system every time a client explicitly starts the service by calling
     * @param intent The Intent supplied to {@link android.content.Context#startService},
     * as given.  This may be null if the service is being restarted after
     * its process has gone away, and it had previously returned anything
     * except {@link #START_STICKY_COMPATIBILITY}.
     * @param flags Additional data about this start request.
     * @param startId A unique integer representing this specific request to
     * start.  Use with {@link #stopSelfResult(int)}.
     *
     * @return The return value indicates what semantics the system should use for the service's
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText("Your service is running in the foreground.")
                .build();

        startForeground(NOTIFICATION_ID, notification);

        return START_NOT_STICKY;
    }

}

