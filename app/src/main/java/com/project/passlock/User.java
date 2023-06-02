package com.project.passlock;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public String key;
    public String uid;//user uid
    public String email,firstname, lastname;
    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public User(String uid, String email, String firstname,String lastname,String key) {
        this.uid = uid;
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        this.key = key;
    }
}
