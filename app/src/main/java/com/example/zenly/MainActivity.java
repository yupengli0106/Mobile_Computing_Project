package com.example.zenly;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    private static final String ACCESS_FINE_LOCATION = "android.permission.ACCESS_FINE_LOCATION";
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // request location permission from user when app starts
        // first check if location permission is granted
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // permission is not granted yet so request it from user and wait for user's response
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
        }

    }

    /**
     * callback method for requestPermissions() in onCreate()
     * @param requestCode The request code passed in requestPermissions()
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission granted
                Toast.makeText(this, "location permission granted", Toast.LENGTH_SHORT).show();
                //TODO: start location service
            } else {
                // permission denied
                Toast.makeText(this, "location permission denied", Toast.LENGTH_SHORT).show();
                // show alert dialog to user
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Location permission is required to use this app. Please grant the permission in the settings. " +
                                "\n\nIf you have already granted the permission, please restart the app. ")
                        .setTitle("Location Permission Denied")
                        .setPositiveButton("Exit", (dialog, id) -> {
                            finish();  // exit the app if user denied location permission
                        })
                        .show();

            }
        }
    }

    /**
     * Go to register activity when user clicks on register button
     * @param view the view of the activity
     */
    public void goToRegister(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    /**
     * Go to login activity when user clicks on login button
     * @param view the view of the activity
     */
    public void goToLogin(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}