package com.example.zenly;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.util.Objects;


/**
 * FirebaseHelper is a singleton class that provides helper methods for Firebase Realtime Database.
 * It is used for CRUD operations on the database.
 */
public class FirebaseHelper implements Serializable {
    public interface AuthCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    // Singleton pattern
    private static volatile FirebaseHelper instance;
    // Firebase Authentication instance
    private final FirebaseAuth mAuth;

    // Firebase Realtime Database reference
    private final DatabaseReference myDatabase;
    // Firebase Realtime Database reference to the users node
    private final DatabaseReference usersRef;
    // Firebase Realtime Database URL
    private static final String URL = "https://mobile-computing-ef31f-default-rtdb.asia-southeast1.firebasedatabase.app/";


    private FirebaseHelper() {
        if (instance != null) {
            throw new RuntimeException("Please Use getInstance() method to get the single instance of this class.");
        }
        // Firebase Realtime Database initialization
        myDatabase = FirebaseDatabase.getInstance(URL).getReference();
        // Firebase Authentication initialization
        mAuth = FirebaseAuth.getInstance();
        // Get a reference to the root node of the database (users)
        usersRef = myDatabase.child("users");
    }

    /**
     * Get the singleton instance of this class
     * @return the singleton instance of this class
     */
    public static FirebaseHelper getInstance() {
        // Double check locking pattern
        if (instance == null) {
            synchronized (FirebaseHelper.class) {
                if (instance == null) {
                    instance = new FirebaseHelper();
                }
            }
        }
        return instance;
    }

    /**
     * This method is called immediately after an object of this class is deserialized.
     * @return the singleton instance of this class
     */
    protected Object readResolve() {
        return getInstance();
    }

    /**
     * use firebase authentication to register a user
     * @param email the email of the user
     * @param password the password of the user
     * @param user the user object
     * @param callback the callback function
     */
    public void registerUser(String email, String password, final User user, final AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                        usersRef.child(userId).setValue(user);
                        callback.onSuccess();
                    } else {
                        callback.onFailure(Objects.requireNonNull(task.getException()).getMessage());
                    }
                });
    }

}
