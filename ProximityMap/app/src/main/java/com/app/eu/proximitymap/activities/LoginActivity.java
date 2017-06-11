package com.app.eu.proximitymap.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.app.eu.proximitymap.R;
import com.app.eu.proximitymap.models.User;
import com.app.eu.proximitymap.models.UserHelper;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

/**
 * Launch activity that handles the user login/registration by
 * creating a login intent with FireBaseUI. This takes care of
 * all the logging in and registration processes for us.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Response code for signing in.
     */
    private static final int RC_SIGN_IN = 0;
    /**
     * Firebase authentication instance that keeps track
     * of the current user status and more.
     */
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Assign the FirebaseAuth
        auth = FirebaseAuth.getInstance();

        // Check if the user is signed in yet.
        if (auth.getCurrentUser() != null) {
            // User is already signed in.
            Log.v("AUTH", "Logged in as " + auth.getCurrentUser().getEmail());
            final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(auth.getCurrentUser().getUid());
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    UserHelper.setUser(user);
                    startActivity(MapsActivity.class);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        } else {
            // User is not yet signed in, start the FirebaseUI intent by calling
            // createSignInIntentBuilder. The response of this intent is handled
            // in .onActivityResult() with the request code RC_SIGN_IN.
            startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setProviders(Arrays.asList(
                            new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                            new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()))
                    .setIsSmartLockEnabled(false)
                    .setLogo(R.drawable.icon)
                    .build(), RC_SIGN_IN);
        }
    }

    /**
     * Handles the response from the FirebaseUI intent. If the action succeeded
     * it calls .startMainActivity(), otherwise it does nothing.
     *
     * @throws NullPointerException when firebase can't get the user object.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // User logged in.
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    Log.d("AUTH", auth.getCurrentUser().getEmail());
                } else {
                    Log.wtf("ToPkEk", "CaN't HaPpEn! ImPoSsIbLe StAte!");
                    throw new NullPointerException("User logged in but something went wrong!");
                }
                startActivity(ExtraUserDetailsActivity.class);
            } else {
                // User not logged in.
                Log.d("AUTH", "Not authenticated");
            }
        }
    }

    /**
     * Start the maps intent and finish the current one.
     */
    private void startActivity(Class<?> activityToStart) {
        Intent intent = new Intent(this, activityToStart);
        startActivity(intent);
        finish();
    }
}
