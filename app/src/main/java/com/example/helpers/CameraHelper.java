package com.example.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CameraHelper {

    private final Context context;

    private final ActivityResultLauncher<String> requestPermissionLauncher;
    private final ActivityResultLauncher<Uri> takePictureLauncher;
    private final ActivityResultLauncher<String> pickImageLauncher;

    private String currentPhotoPath;

    private final FirebaseHelper firebaseHelper = FirebaseHelper.getInstance();

    public CameraHelper(Context context, ActivityResultLauncher<String> requestPermissionLauncher, ActivityResultLauncher<Uri> takePictureLauncher, ActivityResultLauncher<String> pickImageLauncher) {
        this.context = context;
        this.requestPermissionLauncher = requestPermissionLauncher;
        this.takePictureLauncher = takePictureLauncher;
        this.pickImageLauncher = pickImageLauncher;
    }

    public void openCamera() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            try {
                File photoFile = createImageFile();
                Uri photoUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", photoFile);
                takePictureLauncher.launch(photoUri);
            } catch (IOException e) {
                Toast.makeText(context, "Failed to create image file.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // If permission is not granted, request it
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    public void openGallery() {
        pickImageLauncher.launch("image/*");
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public String getCurrentPhotoPath() {
        return currentPhotoPath;
    }

    public void setCurrentPhotoPath(String path) {
        currentPhotoPath = path;
    }

    public void addPicToGallery(Context context, String imagePath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imagePath);
        Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    public void uploadImageToFirebaseStorage(Uri fileUri) {
        if (fileUri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            String currentUserId = firebaseHelper.getCurrentUserId();
            String fileName = currentUserId + ".jpg";

            StorageReference photoRef = storageRef.child("user_profile_pictures/" + fileName);
            // Upload file to Firebase Storage
            photoRef.putFile(fileUri).addOnSuccessListener(taskSnapshot -> {
                photoRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    updateUserProfileInfo(currentUserId, downloadUri.toString());
                }).addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to get download URL", Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show();
            });
        }
    }

    public void updateUserProfileInfo(String userId, String imageUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("profileImageUrl", imageUrl);

        firebaseHelper.usersRef.child(userId).updateChildren(updates).addOnSuccessListener(aVoid -> {
            Toast.makeText(context, "User profile updated", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(context, "Failed to update user profile", Toast.LENGTH_SHORT).show();
        });
    }


}
