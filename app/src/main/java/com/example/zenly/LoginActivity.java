package com.example.zenly;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEditText;
    private EditText passwordEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

    }

    public void onLogin(View view) {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Email and Password must not be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseHelper db = FirebaseHelper.getInstance();
        db.loginUser(email, password, new FirebaseHelper.AuthCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(LoginActivity.this, "Login Success!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, MapActivity.class);
                startActivity(intent);
                finish();
            }
            @Override
            public void onFailure(String errorMessage) {
                if (errorMessage.equals("An internal error has occurred. [ INVALID_LOGIN_CREDENTIALS ]")) {// invalid email or password
                    Toast.makeText(LoginActivity.this, "Invalid email or password, please try again.", Toast.LENGTH_SHORT).show();
                } else {// other errors
                    Toast.makeText(LoginActivity.this, "Login Failed：" + errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        });
    }


}