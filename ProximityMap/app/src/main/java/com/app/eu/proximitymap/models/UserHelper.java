package com.app.eu.proximitymap.models;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Class to get the user object from.
 */

public class UserHelper {

    private static User user;

    private UserHelper() {
    }

    /**
     * @return current user object.
     * @throws NullPointerException when there is no user initialized.
     */
    public static User getInstance() {
        if (user == null) {
            throw new NullPointerException("No  user initialized");
        }
        return user;
    }

    public static void setUser(User u) {
        user = u;
    }

    /**
     * Uploads the user object to the real-time database.
     *
     * @param u      user object to upload.
     * @param caller activity that must implement OnCompleteListener.
     */
    public static void uploadUser(User u, OnCompleteListener caller) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("users");
        database.child(getUid())
                .setValue(u)
                .addOnCompleteListener(caller);
    }

    /**
     * Returns the uid of the current logged in user.
     *
     * @return uid.
     * @throws IllegalStateException when the user is not logged in.
     */
    @NonNull
    public static String getUid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("User not logged in.");
        }
        return user.getUid();
    }
}
