package com.android.eu.proximitymap.models;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * User location class that can be inserted directly into the database.
 */

@IgnoreExtraProperties
public class User {

    public String profession;
    public String dob;
    public String gender;
    public Boolean student;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String profession, String dob, String gender, Boolean student) {
        this.profession = profession;
        this.dob = dob;
        this.gender = gender;
        this.student = student;

        if (!gender.equals("m") || !gender.equals("f")) {
            throw new IllegalArgumentException("Gender can only be 'm' or 'f'.");
        }
    }
}
