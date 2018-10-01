package com.example.psks.firephotoblog;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    private String currentUserId;

    private FirebaseFirestore firebaseFirestore;

    private Toolbar mainToolbar;
    private FloatingActionButton addPostBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        firebaseFirestore = FirebaseFirestore.getInstance();

        mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("FirePhotoBlog ( ﾟдﾟ)");

        addPostBtn = findViewById(R.id.add_post_btn);
        addPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postBlog();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            // User is signed in
            currentUserId = user.getUid();
            firebaseFirestore.collection("users").document(currentUserId).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                if (!task.getResult().exists()) {
                                    accountSetting();
                                }
                            } else {
                                String error = task.getException().getMessage();
                                Toast.makeText(MainActivity.this, "Error : " + error, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } else {
            // No user is signed in
            sendToSignIn();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout_btn:

                signOut();

                return true;
            case R.id.action_settings_btn:

                accountSetting();

                return true;
            default:
                return false;
        }
    }

    private void sendToSignIn() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    private void signOut() {
        mAuth.signOut();
        sendToSignIn();
    }

    private void accountSetting() {
        Intent settingsIntent = new Intent(MainActivity.this, SetupActivity.class);
        startActivity(settingsIntent);
        finish();
    }

    private void postBlog() {
        Intent newPostIntent = new Intent(MainActivity.this, NewPostActivity.class);
        startActivity(newPostIntent);
        finish();
    }
}
