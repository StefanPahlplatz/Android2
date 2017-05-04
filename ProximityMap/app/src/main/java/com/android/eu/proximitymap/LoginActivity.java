package com.android.eu.proximitymap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;

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
            Log.d("AUTH", auth.getCurrentUser().getEmail());
            startMainActivity();
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
                    .build(), RC_SIGN_IN);
        }
    }

    /**
     * Handles the response from the FirebaseUI intent. If the action succeeded
     * it calls .startMainActivity(), otherwise it does nothing.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // User logged in.
                Log.d("AUTH", auth.getCurrentUser().getEmail());
                startMainActivity();
            } else {
                // User not logged in.
                Log.d("AUTH", "Not authenticated");
            }
        }
    }

    /**
     * Start the main intent and finish the current one.
     */
    private void startMainActivity() {
        Intent mainIntent = new Intent(this, MapsActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
