package com.example.zenly;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * FirebaseHelper is a singleton class that provides helper methods for Firebase Realtime Database.
 * It is used for CRUD operations on the database.
 */
public class FirebaseHelper implements Serializable {
    // Singleton pattern
    private static volatile FirebaseHelper instance;
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
     * Register a new user to the database
     * @param username the username of the user
     * @param email the email of the user
     * @param password the password of the user
     * @param context the context of the activity
     * @param nextActivityClass the class of the next activity
     */
    public void registerUser(final String username, final String email, final String password, final Context context, final Class<?> nextActivityClass) {
        Query query = usersRef.orderByChild("email").equalTo(email);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Toast.makeText(context, "Email already exists", Toast.LENGTH_SHORT).show();
                } else {
                    Map<String, Object> myMap = new HashMap<>();
                    myMap.put("username", username);
                    myMap.put("email", email);
                    myMap.put("password", password);  // TODO: Encrypt the password

                    DatabaseReference newUserRef = usersRef.child(email);
                    newUserRef.setValue(myMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(context, "Registered Success!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(context, nextActivityClass);
                                context.startActivity(intent);
                            } else {
                                Toast.makeText(context, "Registered Failed：" + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "Database query failed：" + databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * get the reference pointing to a specific user
     * @param username the username of the user
     * @return the reference pointing to the user
     */
    public DatabaseReference getUserReference(String username) {
        return usersRef.child(username);
    }
}
