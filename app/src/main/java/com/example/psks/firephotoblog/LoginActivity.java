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

public class LoginActivity extends AppCompatActivity {

    private EditText mEmailField;
    private EditText mPasswordField;
    private Button bLogin;
    private Button bRegLogin;
    private ProgressBar pLoginProgrssBar;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Views
        mEmailField = (EditText) findViewById(R.id.login_email);
        mPasswordField = (EditText) findViewById(R.id.login_password);

        // Buttons
        bLogin = (Button) findViewById(R.id.loging_btn);
        bRegLogin = (Button) findViewById(R.id.login_reg_btn);

        // ProgressBar
        pLoginProgrssBar = (ProgressBar) findViewById(R.id.loginProgressBar);

        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = mEmailField.getText().toString();
                String password = mPasswordField.getText().toString();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {

                    pLoginProgrssBar.setVisibility(View.VISIBLE);

                    // [START sign_in_with_email]
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        sendToMain();
                                    } else {
                                        String errorMsg = task.getException().getMessage();
                                        Toast.makeText(LoginActivity.this, "Error : " + errorMsg, Toast.LENGTH_LONG).show();
                                    }

                                pLoginProgrssBar.setVisibility(View.INVISIBLE);

                                }
                    });
                    // [END sign_in_with_email]
                }

            }
        });

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]
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
        Intent mainIntent = new Intent (LoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
