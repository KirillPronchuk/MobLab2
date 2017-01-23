package com.example.kirill.lab2.model;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Kirill on 18.01.2017.
 */
@IgnoreExtraProperties
public class User {
    public String firstname;
    public String lastname;
    public String email;
    public String position;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }


    public User(String firstname, String lastname, String email, String position) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.position = position;
        this.email = email;
    }
}
