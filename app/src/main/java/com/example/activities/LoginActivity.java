package com.example.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.helpers.FirebaseHelper;
import com.example.services.LocationService;
import com.example.zenly.R;

public class LoginActivity extends AppCompatActivity {
    private final String TAG = "LoginActivityLog";
    private EditText emailEditText; // email input field
    private EditText passwordEditText; // password input field
    private TextView forgotPasswordTextView; // forgot password text view
    private final FirebaseHelper db = FirebaseHelper.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);

        // set the forgot password text view to show the reset password dialog
        forgotPasswordTextView.setOnClickListener(v -> showResetPasswordDialog());
    }


    /**
     * callback method for login button
     * @param view the login button
     */
    public void onLogin(View view) {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Email and Password must not be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseHelper db = FirebaseHelper.getInstance();
        db.loginUser(email, password, new FirebaseHelper.AuthCallback() {
            /**
             * Called when the login is successful.
             */
            @Override
            public void onSuccess() {
                Toast.makeText(LoginActivity.this, "Login Success!", Toast.LENGTH_SHORT).show();
                // go to map page
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);

                // start the location service
                Intent serviceIntent = new Intent(LoginActivity.this, LocationService.class);
                // start the service in the foreground if the android version is Oreo or above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent);
                } else {
                    startService(serviceIntent);
                }
                Log.d(TAG, "onSuccess: start location service");

                // finish the login activity
                finish();
            }

            /**
             * Called when the login is failed.
             * @param errorMessage The error message returned from Firebase Authentication.
             */
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


    private void showResetPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");

        final EditText input = new EditText(this);
        input.setHint("Please enter your email");
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(input);

        builder.setPositiveButton("Send", (dialog, which) -> {
            String email = input.getText().toString();
            db.resetPassword(email, new FirebaseHelper.AuthCallback(){
                @Override
                public void onSuccess() {
                    Toast.makeText(LoginActivity.this, "Reset password email sent.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(LoginActivity.this, "Reset password failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });

        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }



}