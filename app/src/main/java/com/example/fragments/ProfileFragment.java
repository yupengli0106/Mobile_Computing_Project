package com.example.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.content.Intent;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.activities.WelcomeActivity;
import com.example.helpers.CameraHelper;
import com.example.helpers.FirebaseHelper;
import com.example.services.LocationService;
import com.example.util.StepUtil;
import com.example.zenly.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProfileFragment extends Fragment {
    private final FirebaseHelper firebaseHelper = FirebaseHelper.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final String TAG = "ProfileFragment";

    private CameraHelper cameraHelper;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        Button logoutButton = view.findViewById(R.id.logoutButton);
        TextView usernameTextView = view.findViewById(R.id.usernameTextView);
        TextView emailTextView = view.findViewById(R.id.emailTextView);
        TextView tvStep = view.findViewById(R.id.tvStep);
        ImageView imageView = view.findViewById(R.id.profileImageView);
        Button updateProfileButton = view.findViewById(R.id.updateProfileButton);
        TextView tvBirthdate = view.findViewById(R.id.tvBirthdateValue);
        Button buttonBirthdate = view.findViewById(R.id.buttonSelectBirthdate);
        Spinner spinnerGender = view.findViewById(R.id.spinnerGender);
        EditText etBio = view.findViewById(R.id.etBio);

        fetchAndSetUserDetails(usernameTextView, emailTextView, imageView, tvBirthdate, spinnerGender, etBio);

        buttonBirthdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                                tvBirthdate.setText(selectedDate);
                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.gender_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        spinnerGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String gender = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
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

        ActivityResultLauncher<Uri> takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
            if (result) {
                // Update ImageView with new photo
                Uri imageUri = Uri.parse(cameraHelper.getCurrentPhotoPath());
                imageView.setImageURI(imageUri);
                cameraHelper.addPicToGallery(getContext(), cameraHelper.getCurrentPhotoPath());
            }
        });

        ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
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
            showImagePickerDialog();
        });
        tvStep.append(StepUtil.getTodayStep(getContext()) + "");

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
            String birthdate = tvBirthdate.getText().toString();
            String gender = spinnerGender.getSelectedItem().toString();
            String bio = etBio.getText().toString();

            if (photoPath != null) {
                File photoFile = new File(photoPath);
                Uri photoUri = FileProvider.getUriForFile(
                        getContext(),
                        getContext().getPackageName() + ".provider",
                        photoFile
                );
                cameraHelper.uploadImageToFirebaseStorage(photoUri);
            }
            updateUserDetails(birthdate, bio, gender);
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

    public void fetchAndSetUserDetails(TextView usernameTextView, TextView emailTextView, ImageView imageView, TextView birthdateTextView, Spinner genderSpinner, EditText bioEditText) {
        String currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        firebaseHelper.usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("profileImageUrl")) {
                        String imageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            // Use Glide to load the image
                            Glide.with(getContext())
                                    .load(imageUrl)
                                    .into(imageView);
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
                        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                                R.array.gender_array, android.R.layout.simple_spinner_item);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        genderSpinner.setAdapter(adapter);
                        genderSpinner.setSelection(adapter.getPosition(gender));
                    }
                    if (dataSnapshot.hasChild("bio")) {
                        String bio = dataSnapshot.child("bio").getValue(String.class);
                        bioEditText.setText(bio);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "loadUserProfileImage:onCancelled", databaseError.toException());
            }
        });
    }

    private void updateUserDetails(String birthdate, String bio, String gender) {
        String currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        Map<String, Object> updates = new HashMap<>();
        if (birthdate != null && !birthdate.isEmpty()) {
            updates.put("birthdate", birthdate);
        }
        if (gender != null && !gender.isEmpty()) {
            updates.put("gender", gender);
        }
        if (bio != null && !bio.isEmpty()) {
            updates.put("bio", bio);
        }

        // Update the child with the new map
        firebaseHelper.usersRef.child(currentUserId).updateChildren(updates).addOnSuccessListener(aVoid -> {
            // Update UI or inform the user of success
            Toast.makeText(getContext(), "User profile updated", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            // Handle any errors
            Toast.makeText(getContext(), "Failed to update user profile", Toast.LENGTH_SHORT).show();
        });
    }

}