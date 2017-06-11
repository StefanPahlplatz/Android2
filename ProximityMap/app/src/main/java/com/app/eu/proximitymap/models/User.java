package com.app.eu.proximitymap.models;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * User location class that can be inserted directly into the database.
 */

@IgnoreExtraProperties
public class User {

    public String name;
    public String profession;
    public String dob;
    public String gender;
    public Boolean student;
    public String picture;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    /**
     * @param gender has to be 'm' or 'f'.
     * @throws IllegalArgumentException when the gender is not 'm' or 'f'.
     */
    public User(String name, String profession, String dob, String gender, Boolean student) {
        this.name = name;
        this.profession = profession;
        this.dob = dob;
        this.gender = gender;
        this.student = student;
        this.picture = "";

        if (!(gender.equals("m") || gender.equals("f"))) {
            throw new IllegalArgumentException("Gender can only be 'm' or 'f'.");
        }
    }
}
