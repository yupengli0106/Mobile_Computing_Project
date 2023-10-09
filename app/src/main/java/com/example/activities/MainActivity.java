package com.example.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.fragments.MapFragment;
import com.example.zenly.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    // fragment manager and fragments
    private final FragmentManager fragmentManager = getSupportFragmentManager();
    // reuse mapFragment
    private final MapFragment mapFragment = new MapFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize bottom navigation view
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // set map fragment as default fragment
        fragmentManager.beginTransaction().replace(R.id.fragment_container, mapFragment).commit();

        //setOnNavigationItemSelectedListener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_map) {
//                selectedFragment = new MapFragment();
                selectedFragment = mapFragment;  // Reuse mapFragment
            }
//                else if (itemId == R.id.nav_chat) {
//                    TODO: navigate to chat fragment
//                } else if (itemId == R.id.nav_settings) {
//                    TODO: navigate to settings fragment
//                }

            if (selectedFragment != null) {
                fragmentManager.beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                Log.d("MainActivityLog", "onCreate:  selectedFragment set as default fragment !");
                return true;
            } else {
                // Handle null case
                Log.w("MainActivityLog", "No fragment selected");
                Toast.makeText(this, "No fragment selected", Toast.LENGTH_SHORT).show();
                return false;
            }

        });
    }
}

