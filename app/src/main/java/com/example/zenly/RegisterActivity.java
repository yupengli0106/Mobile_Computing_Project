package com.example.zenly;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;


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
        final String username = usernameEditText.getText().toString();
        final String email = emailEditText.getText().toString();
        final String password = passwordEditText.getText().toString();

        // get instance of FirebaseHelper singleton class
        FirebaseHelper firebaseHelper = FirebaseHelper.getInstance();
        // register user
        firebaseHelper.registerUser(username, email, password, this, LoginActivity.class);
    }

}