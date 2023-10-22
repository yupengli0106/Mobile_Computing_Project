package com.example.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.fragments.ChatFragment;
import com.example.fragments.FriendsFragment;
import com.example.fragments.MapFragment;
import com.example.fragments.ProfileFragment;
import com.example.zenly.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    // fragment manager and fragments
    private final FragmentManager fragmentManager = getSupportFragmentManager();
    // reuse mapFragment
    private final MapFragment mapFragment = new MapFragment();
    private final ChatFragment chatFragment = new ChatFragment();
    private final FriendsFragment friendsFragment = new FriendsFragment();
    private final ProfileFragment profileFragment = new ProfileFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize bottom navigation view
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // set map fragment as default fragment
        fragmentManager.beginTransaction().replace(R.id.fragment_container, mapFragment).commit();

        bottomNavigationView.setSelectedItemId(R.id.nav_map);

        //setOnNavigationItemSelectedListener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_map) {
                selectedFragment = new MapFragment();
            } else if (itemId == R.id.nav_chat) {
                selectedFragment = chatFragment;
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = profileFragment;
            } else if (itemId == R.id.nav_friends) {
                selectedFragment = friendsFragment;
            }

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

