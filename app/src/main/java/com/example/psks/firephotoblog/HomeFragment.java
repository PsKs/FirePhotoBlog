package com.example.psks.firephotoblog;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private List<BlogPost> blog_list;
    private List<User> user_list;

    private RecyclerView blog_list_view;
    private BlogRecyclerAdapter blogRecyclerAdapter;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        blog_list = new ArrayList<>();
        user_list = new ArrayList<>();
        blog_list_view = view.findViewById(R.id.blog_post_view);

        firebaseAuth = FirebaseAuth.getInstance();

        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list, user_list);
        blog_list_view.setLayoutManager(new LinearLayoutManager(container.getContext()));
        blog_list_view.setAdapter(blogRecyclerAdapter);

        if (firebaseAuth.getCurrentUser() != null) {

            firebaseFirestore = FirebaseFirestore.getInstance();

            blog_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                    if (reachedBottom) {

                        // String desc = lastVisible.getString("desc");
                        // Toast.makeText(container.getContext(), "Reached: " + desc, Toast.LENGTH_LONG).show();

                        nextPostLists();
                    }
                }
            });

            Query firstQuery = firebaseFirestore.collection("posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(3);

            firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                    if (!queryDocumentSnapshots.isEmpty()) {

                        if (isFirstPageFirstLoad) {
                            lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                            blog_list.clear();
                            user_list.clear();
                        }

                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String blogUserId = doc.getDocument().getString("user_id");
                                String blogPostId = doc.getDocument().getId();
                                final BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);

                                firebaseFirestore.collection("users").document(blogUserId).get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                                if (task.isSuccessful()) {

                                                    User user = task.getResult().toObject(User.class);

                                                    if (isFirstPageFirstLoad) {
                                                        // Load into object
                                                        user_list.add(user);
                                                        blog_list.add(blogPost);
                                                    } else {
                                                        // If new post after loaded object add in first lists
                                                        user_list.add(0, user);
                                                        blog_list.add(0, blogPost);
                                                    }

                                                    blogRecyclerAdapter.notifyDataSetChanged();

                                                } else {
                                                    String error = task.getException().getMessage();
                                                    Toast.makeText(container.getContext(), "Error : " + error, Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                            }
                        }
                        isFirstPageFirstLoad = false;
                    }
                }
            });
        }

        // Inflate the layout for this fragment
        return view;
    }

    public void nextPostLists() {

        Query nextQuery = firebaseFirestore.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(3);

        nextQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (!queryDocumentSnapshots.isEmpty()) {

                    lastVisible = queryDocumentSnapshots.getDocuments()
                            .get(queryDocumentSnapshots.size() - 1);

                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            String blogUserId = doc.getDocument().getString("user_id");
                            final String blogPostId = doc.getDocument().getId();
                            final BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);

                            firebaseFirestore.collection("users").document(blogUserId).get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                            if (task.isSuccessful()) {

                                                User user = task.getResult().toObject(User.class);

                                                user_list.add(user);
                                                blog_list.add(blogPost);

                                                blogRecyclerAdapter.notifyDataSetChanged();

                                            } else {
                                                String error = task.getException().getMessage();
                                                Toast.makeText(getActivity(), "Error : " + error, Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        lastVisible = null;
        isFirstPageFirstLoad = true;
    }
}
