package com.example.psks.firephotoblog;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CommentActivity extends AppCompatActivity {

    private Toolbar commentToolbar;

    private String blog_post_id;
    private String currentUser;

    private EditText comment_field;
    private ImageView comment_post_btn;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        commentToolbar = findViewById(R.id.comment_toolbar);
        setSupportActionBar(commentToolbar);
        getSupportActionBar().setTitle("Comments");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        currentUser = firebaseAuth.getCurrentUser().getUid();
        blog_post_id = getIntent().getStringExtra("blog_post_id");

        comment_field = findViewById(R.id.comment_field);
        comment_post_btn = findViewById(R.id.comment_post_btn);

        comment_post_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String commentTxt = comment_field.getText().toString();

                if (!commentTxt.isEmpty()) {

                    Map<String, Object> commentsMap = new HashMap<>();
                    commentsMap.put("message", commentTxt);
                    commentsMap.put("user_id", currentUser);
                    commentsMap.put("timestamp", FieldValue.serverTimestamp());

                    firebaseFirestore.collection("posts/" + blog_post_id + "/comments")
                            .add(commentsMap)
                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if (!task.isSuccessful()) {
                                        String error = task.getException().getMessage();
                                        Toast.makeText(CommentActivity.this, "Error posting comment : " + error, Toast.LENGTH_SHORT).show();
                                    } else {
                                        comment_field.setText("");
                                    }
                                }
                            });
                }
            }
        });
    }
}
