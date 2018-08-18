package com.example.psks.firephotoblog;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText mEmailField;
    private EditText mPasswordField;
    private EditText mConfirmPasswordField;
    private Button bRegis;
    private Button bRegisLogin;
    private ProgressBar pRegisBar;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        // Views
        mEmailField = (EditText) findViewById(R.id.reg_email);
        mPasswordField = (EditText) findViewById(R.id.reg_pass);
        mConfirmPasswordField = (EditText) findViewById(R.id.reg_confirm_pass);

        // Buttons
        bRegis = (Button) findViewById(R.id.reg_btn);
        bRegisLogin = (Button) findViewById(R.id.reg_login_btn);

        // ProgressBar
        pRegisBar = (ProgressBar) findViewById(R.id.regProgressBar);

        bRegis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = mEmailField.getText().toString();
                String password = mPasswordField.getText().toString();
                String confirm_password = mConfirmPasswordField.getText().toString();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(confirm_password)) {
                    if (password.equals(confirm_password)) {

                        pRegisBar.setVisibility(View.VISIBLE);

                        // [START create_user_with_email]
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            sendToMain();
                                        } else {
                                            String errorMsg = task.getException().getMessage();
                                            Toast.makeText(RegisterActivity.this, "Authentication failed : " + errorMsg, Toast.LENGTH_SHORT).show();
                                        }

                                        pRegisBar.setVisibility(View.INVISIBLE);

                                    }
                                });
                        // [END create_user_with_email]
                    } else {
                        Toast.makeText(RegisterActivity.this, "Password doesn't match.", Toast.LENGTH_LONG).show();
                    }
                }

            }
        });

        bRegisLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToLoginPage();
            }
        });
    }

    // [START on_start_check_user]
    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is signed in
            sendToMain();
        } else {
            // No user is signed in
        }
    }

    private void sendToMain() {
        Intent mainIntent = new Intent (RegisterActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    private void sendToLoginPage() {
        Intent loginIntent = new Intent (RegisterActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }
}
