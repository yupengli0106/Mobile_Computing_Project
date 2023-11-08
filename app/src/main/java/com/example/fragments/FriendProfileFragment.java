package com.example.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.helpers.FirebaseHelper;
import com.example.zenly.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;


public class FriendProfileFragment extends Fragment {
    private String friendId;
    private final FirebaseHelper firebaseHelper = FirebaseHelper.getInstance();
    private final String TAG = "FriendProfileFragment";

    public FriendProfileFragment() {
        // Required empty public constructor
    }

    public static FriendProfileFragment newInstance(String friendId) {
        FriendProfileFragment fragment = new FriendProfileFragment();
        Bundle args = new Bundle();
        args.putSerializable("selected_friend", friendId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            friendId = (String) getArguments().getSerializable("selected_friend");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_friend_profile, container, false);

        TextView usernameTextView = view.findViewById(R.id.usernameTextView);
        TextView emailTextView = view.findViewById(R.id.emailTextView);
        TextView tvStep = view.findViewById(R.id.tvStep);
        ImageView imageView = view.findViewById(R.id.profileImageView);
        TextView tvGender = view.findViewById(R.id.tvGenderValue);
        TextView tvBirthdate = view.findViewById(R.id.tvBirthdateValue);
        TextView tvBio = view.findViewById(R.id.tvBio);

        fetchAndSetUserDetails(usernameTextView, emailTextView, imageView, tvBirthdate, tvGender, tvBio, tvStep);

        return view;
    }

    ;

    public void fetchAndSetUserDetails(TextView usernameTextView, TextView emailTextView, ImageView imageView, TextView birthdateTextView, TextView genderTextView, TextView bioTextView, TextView stepTextView) {
        firebaseHelper.usersRef.child(friendId).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("profileImageUrl")) {
                        String imageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            // Use Glide to load the image
                            Glide.with(requireContext()).load(imageUrl).into(imageView);
                        }
                    }
                    if (dataSnapshot.hasChild("username")) {
                        String username = dataSnapshot.child("username").getValue(String.class);
                        usernameTextView.setText(username);
                    }
                    if (dataSnapshot.hasChild("email")) {
                        String email = dataSnapshot.child("email").getValue(String.class);
                        emailTextView.setText(email);
                    }
                    if (dataSnapshot.hasChild("birthdate")) {
                        String birthdate = dataSnapshot.child("birthdate").getValue(String.class);
                        birthdateTextView.setText(birthdate);
                    }
                    if (dataSnapshot.hasChild("gender")) {
                        String gender = dataSnapshot.child("gender").getValue(String.class);
                        genderTextView.setText(gender);
                    }
                    if (dataSnapshot.hasChild("bio")) {
                        String bio = dataSnapshot.child("bio").getValue(String.class);
                        bioTextView.setText(bio);
                    }
                    if (dataSnapshot.hasChild("step")) {
                        String step = dataSnapshot.child("step").getValue(String.class);
                        bioTextView.setText(step);
                    } else {
                        stepTextView.setText("No step yet");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "loadUserProfileImage:onCancelled", databaseError.toException());
            }
        });
    }
}