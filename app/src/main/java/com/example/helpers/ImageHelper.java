package com.example.helpers;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class ImageHelper {

    private final FirebaseHelper firebaseHelper = FirebaseHelper.getInstance();

    public ImageHelper() {
    }

    /**
     * Fetches the profile image URL for the user and applies it to the ImageView.
     * If the URL is null, a default avatar will be shown.
     *
     * @param userId    The ID of the user whose image is to be fetched.
     * @param imageView The ImageView where the profile image should be displayed.
     * @param textView  The TextView that should display the user's initial if there is no profile image.
     */
    public void fetchAndSetUserProfileImage(String userId, ImageView imageView, TextView textView) {
        firebaseHelper.usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
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
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
                Log.d("DatabaseError", "fetchAndSetUserProfileImage:onCancelled", databaseError.toException());
            }
        });
    }

    /**
     * Sets the ImageView to a default avatar and makes the TextView visible with the initial of the username.
     *
     * @param imageView    The ImageView that would have shown the profile image.
     * @param textView     The TextView to display the user's initial.
     * @param dataSnapshot The DataSnapshot containing user data.
     */
    private void setImageToDefaultAvatar(ImageView imageView, TextView textView, DataSnapshot dataSnapshot) {
        imageView.setVisibility(View.GONE);
        textView.setVisibility(View.VISIBLE);
        if (dataSnapshot.hasChild("username")) {
            String username = dataSnapshot.child("username").getValue(String.class);
            textView.setText(username != null && !username.isEmpty() ? username.substring(0, 1).toUpperCase() : "");
        }
    }


}
