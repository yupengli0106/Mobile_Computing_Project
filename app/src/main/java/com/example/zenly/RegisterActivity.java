package com.example.zenly;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class RegisterActivity extends AppCompatActivity {
    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private DatabaseReference myRef;

    // Firebase Realtime Database URL
    private static final String URL = "https://mobile-computing-ef31f-default-rtdb.asia-southeast1.firebasedatabase.app/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        // Firebase Realtime Database initialization
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(URL);
        // Get a reference to the root node of the database (users)
        myRef = firebaseDatabase.getReference("users");

    }

    public void onSubmit(View view) {
        // TODO: check if the username, email, and password are valid
        final String username = usernameEditText.getText().toString();
        final String email = emailEditText.getText().toString();
        final String password = passwordEditText.getText().toString();

        // check if the email already exists
        Query query = myRef.orderByChild("email").equalTo(email);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // if the email already exists, show a toast message
                    Toast.makeText(getApplicationContext(), "email already exists", Toast.LENGTH_SHORT).show();
                } else {
                    // if the email does not exist, add the user to the database
                    Map<String, Object> myMap = new HashMap<>();
                    myMap.put("username", username);
                    myMap.put("email", email);
                    myMap.put("password", password);  // TODO: encrypt the password

                    // create a new child node with the email as the key
                    DatabaseReference newUserRef = myRef.child(email);

                    newUserRef.setValue(myMap).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {// if the user is successfully added to the database, show a toast message and go to the login page
                            Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getApplicationContext(), "Failed：" + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // if the database query fails, show a toast message
                Toast.makeText(getApplicationContext(), "database query failed：" + databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


}