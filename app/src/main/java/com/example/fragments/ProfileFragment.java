package com.example.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.activities.WelcomeActivity;
import com.example.helpers.CameraHelper;
import com.example.helpers.FirebaseHelper;
import com.example.model.User;
import com.example.services.LocationService;
import com.example.util.StepUtil;
import com.example.zenly.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private final FirebaseHelper firebaseHelper = FirebaseHelper.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final String TAG = "ProfileFragment";

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;

    private CameraHelper cameraHelper;

//    private CameraHelper cameraHelper;


    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        Button logoutButton = view.findViewById(R.id.logoutButton);
        // Initialize TextViews
        TextView usernameTextView = view.findViewById(R.id.usernameTextView);
        TextView emailTextView = view.findViewById(R.id.emailTextView);
        TextView tvStep = view.findViewById(R.id.tvStep);
        ImageView imageView = view.findViewById(R.id.profileImageView);
        Button updateProfileButton = view.findViewById(R.id.updateProfileButton);

        fetchAndSetUserProfileImage(imageView);

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your app.
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
            }
        });

        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
            if (result) {
                // Update ImageView with new photo
                Uri imageUri = Uri.parse(cameraHelper.getCurrentPhotoPath());
                imageView.setImageURI(imageUri);
                cameraHelper.addPicToGallery(getContext(), cameraHelper.getCurrentPhotoPath());
            }
        });

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                imageView.setImageURI(uri);
                cameraHelper.setCurrentPhotoPath(uri.toString());
            }
        });

        cameraHelper = new CameraHelper(
                getContext(),
                requestPermissionLauncher,
                takePictureLauncher,
                pickImageLauncher
        );


        Button btnAddImage = view.findViewById(R.id.button_change_picture);
        btnAddImage.setOnClickListener(v -> {
            // Show a dialog or a popup menu to choose between camera and gallery
            // Or simply call openCamera() or openGallery() directly if there's a dedicated button
//            openCamera();
            showImagePickerDialog();
        });
        tvStep.append(StepUtil.getTodayStep(getContext()) + "");

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        // get current user id
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    String username = user.getUsername();
                    String email = user.getEmail();
                    // set context to TextView
                    usernameTextView.setText("Username: " + username);
                    emailTextView.setText("Email: " + email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "Get username failed: " + databaseError.getMessage());
            }
        });


        logoutButton.setOnClickListener(v -> {
            // Stop location service
            Intent serviceIntent = new Intent(getActivity(), LocationService.class);
            requireActivity().stopService(serviceIntent);

            // Sign out from Firebase Auth
            mAuth.signOut();
            Log.d(TAG, "User signed out successfully.");

            // Redirect to login activity
            Intent intent = new Intent(getActivity(), WelcomeActivity.class);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        updateProfileButton.setOnClickListener(v -> {
            String photoPath = cameraHelper.getCurrentPhotoPath();
            if (photoPath != null) {
                File photoFile = new File(photoPath);
                Uri photoUri = FileProvider.getUriForFile(
                        getContext(),
                        getContext().getPackageName() + ".provider",
                        photoFile
                );

                cameraHelper.uploadImageToFirebaseStorage(photoUri);
            } else {
                Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
            }
        });


        return view;
    }

    private void showImagePickerDialog() {
        // Options to show in dialog
        String[] items = {"Select from Gallery", "Take Photo"};

        new AlertDialog.Builder(getContext())
                .setTitle("Add Image")
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            // Select from gallery
                            cameraHelper.openGallery();
                        } else {
                            // Take a photo with the camera
                            cameraHelper.openCamera();
                        }
                    }
                })
                .show();
    }

    public void fetchAndSetUserProfileImage(ImageView imageView) {
        String currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        firebaseHelper.usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("profileImageUrl")) {
                    String imageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        // Use Glide to load the image
                        Glide.with(getContext())
                                .load(imageUrl)
                                .into(imageView);
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