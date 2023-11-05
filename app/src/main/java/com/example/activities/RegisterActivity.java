package com.example.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.helpers.FirebaseHelper;
import com.example.model.User;
import com.example.zenly.R;

import java.util.ArrayList;
import java.util.HashMap;


public class RegisterActivity extends AppCompatActivity {
    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

    }

    public void onSubmit(View view) {
        final String username = usernameEditText.getText().toString().trim();
        final String email = emailEditText.getText().toString().trim();
        final String password = passwordEditText.getText().toString().trim();

        // validate input fields before registering user to Firebase Authentication and Realtime Database
        if (!validation(username, email, password)){
            return;
        }

        User newUser = new User(username, email);
        // get instance of FirebaseHelper singleton class
        FirebaseHelper db = FirebaseHelper.getInstance();
        db.registerUser(email, password, newUser, new FirebaseHelper.AuthCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(RegisterActivity.this, "Registered Success!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegisterActivity.this, WelcomeActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(RegisterActivity.this, "Registered Failed：" + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    public boolean validation(String username, String email, String password){
        if (username.isEmpty()) {
            usernameEditText.setError("Username is required");
            usernameEditText.requestFocus();
            return false;
        }else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || email.isEmpty()){
            emailEditText.setError("Please enter a valid email");
            emailEditText.requestFocus();
            return false;
        }else if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            passwordEditText.requestFocus();
            return false;
        }else {
            return true;
        }
    }

}