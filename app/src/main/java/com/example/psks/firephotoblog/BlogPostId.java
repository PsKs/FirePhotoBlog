package com.example.psks.firephotoblog;

import android.support.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

@SuppressWarnings("WeakerAccess")
public class BlogPostId {

    @Exclude
    public String BlogPostId;

    @SuppressWarnings("unchecked")
    public   <T extends BlogPostId> T withId(@NonNull final String id) {
        this.BlogPostId = id;

        return (T) this;
    }
}
