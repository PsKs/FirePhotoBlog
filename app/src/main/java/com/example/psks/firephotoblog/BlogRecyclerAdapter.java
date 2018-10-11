package com.example.psks.firephotoblog;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    public List<User> user_list;
    public List<BlogPost> blog_list;
    public Context context;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    public BlogRecyclerAdapter (List<BlogPost> blog_list, List<User> user_list) {
        this.blog_list = blog_list;
        this.user_list = user_list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item, parent, false);

        context = parent.getContext();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final String blogPostId = blog_list.get(position).BlogPostId;
        final String currentUserId = firebaseAuth.getCurrentUser().getUid();

        String userName = user_list.get(position).getName();
        String userImage = user_list.get(position).getImage();

        holder.setUserData(userName, userImage);

        String image_url = blog_list.get(position).getImage_url();
        String thumb_url = blog_list.get(position).getThumb_url();
        holder.setBlogImage(image_url, thumb_url);

        String desc_data = blog_list.get(position).getDesc();
        holder.setDescText(desc_data);

        long millisec = blog_list.get(position).getTimestamp().getTime();
        String dateString = DateFormat.format("dd/MM/yyyy HH:ss", new Date(millisec)).toString();
        holder.setTime(dateString);

        // Fetch user liked
        firebaseFirestore
                .collection("posts/" + blogPostId + "/likes")
                .document(currentUserId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                        if (documentSnapshot.exists()) {
                            holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.mipmap.action_like_accent));

                        } else {
                            holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.mipmap.action_like_gray));

                        }
                    }
                });

        // Likes count
        firebaseFirestore
                .collection("posts/" + blogPostId + "/likes")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            int count = queryDocumentSnapshots.size();
                            holder.updateLikesCount(count);

                        } else {
                            holder.updateLikesCount(0);
                        }

                    }
                });

        // Like or Dislike
        holder.blogLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseFirestore
                        .collection("posts/" + blogPostId + "/likes")
                        .document(currentUserId).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                if (!task.getResult().exists()) {

                                    Map<String, Object> likesMap = new HashMap<>();
                                    likesMap.put("timestamp", FieldValue.serverTimestamp());

                                    firebaseFirestore
                                            .collection("posts/" + blogPostId + "/likes")
                                            .document(currentUserId).set(likesMap);
                                } else {
                                    firebaseFirestore
                                            .collection("posts/" + blogPostId + "/likes")
                                            .document(currentUserId).delete();
                                }

                            }
                        });
            }
        });

        // Comment
        holder.blogCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent commentIntent = new Intent(context, CommentActivity.class);
                commentIntent.putExtra("blog_post_id", blogPostId);
                context.startActivity(commentIntent);
            }
        });

        // Comments count
        firebaseFirestore
                .collection("posts/" + blogPostId + "/comments")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            int count = queryDocumentSnapshots.size();
                            holder.updateCommentsCount(count);

                        } else {
                            holder.updateCommentsCount(0);
                        }
                    }
                });

    }

    @Override
    public int getItemCount() {
        return blog_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private TextView blogDate;
        private TextView descView;
        private TextView blogUserName;
        private TextView blogLikeCount;
        private TextView blogCommentCount;
        private ImageView blogLikeBtn;
        private ImageView blogImageView;
        private ImageView blogCommentBtn;
        private CircleImageView blogUserImage;

        public ViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            blogLikeBtn = mView.findViewById(R.id.blog_like_btn);
            blogCommentBtn = mView.findViewById(R.id.blog_comment_btn);
        }

        public void setUserData(String name, String image) {
            blogUserName = mView.findViewById(R.id.blog_user_name);
            blogUserName.setText(name);

            blogUserImage = mView.findViewById(R.id.blog_user_image);

            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.profile_placeholder);
            Glide.with(context)
                    .applyDefaultRequestOptions(placeholderOption)
                    .load(image)
                    .into(blogUserImage);
        }

        public void setBlogImage(String downloadUri, String thumbUri) {
            blogImageView = mView.findViewById(R.id.blog_image);

            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.image_placeholder);
            Glide.with(context)
                    .applyDefaultRequestOptions(placeholderOption)
                    .load(downloadUri)
                    .thumbnail(Glide.with(context).load(thumbUri))
                    .into(blogImageView);
        }

        public void setDescText(String descText) {
            descView = mView.findViewById(R.id.blog_desc);
            descView.setText(descText);
        }

        public void setTime(String date) {
            blogDate = mView.findViewById(R.id.blog_date);
            blogDate.setText(date);
        }

        public void updateLikesCount(int count) {
            blogLikeCount = mView.findViewById(R.id.blog_like_count);
            blogLikeCount.setText(count + " Likes");
        }

        public void updateCommentsCount(int count) {
            blogCommentCount = mView.findViewById(R.id.blog_comment_count);
            blogCommentCount.setText(count + " Comments");
        }
    }

}
