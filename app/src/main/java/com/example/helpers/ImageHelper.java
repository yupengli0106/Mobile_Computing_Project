package com.example.helpers;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class ImageHelper {

    private final FirebaseHelper firebaseHelper = FirebaseHelper.getInstance();

    public ImageHelper() {
    }

    public void fetchAndSetUserProfileImage(String userId, ImageView imageView, TextView textView) {
        firebaseHelper.usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("profileImageUrl")) {
                    String imageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        imageView.setVisibility(View.VISIBLE);
                        textView.setVisibility(View.GONE);
                        Glide.with(imageView.getContext()).load(imageUrl).into(imageView);
                    } else {
                        setImageToDefaultAvatar(imageView, textView, dataSnapshot);
                    }
                } else {
                    setImageToDefaultAvatar(imageView, textView, dataSnapshot);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
                Log.d("DatabaseError", "fetchAndSetUserProfileImage:onCancelled", databaseError.toException());
            }
        });
    }

    private void setImageToDefaultAvatar(ImageView imageView, TextView textView, DataSnapshot dataSnapshot) {
        imageView.setVisibility(View.GONE);
        textView.setVisibility(View.VISIBLE);
        if (dataSnapshot.hasChild("username")) {
            String username = dataSnapshot.child("username").getValue(String.class);
            textView.setText(username != null && !username.isEmpty() ? username.substring(0, 1).toUpperCase() : "");
        }
    }
}
