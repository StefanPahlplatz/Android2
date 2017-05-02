package com.android2.eu.proximitymap.utils;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.android2.eu.proximitymap.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by stefan on 02/05/2017.
 *
 * FireBaseAuth helper class.
 * Call .dispose() in the calling onStop() method.
 */

public class FireBaseAuthHelper {
    private static final String TAG = FireBaseAuthHelper.class.getSimpleName();

    private Activity caller;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private final ResponseListener listener;

    /**
     * Initializes a FireBase Auth instance to handle the logging in and out of users.
     */
    public FireBaseAuthHelper(Activity caller) {
        this.caller = caller;

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                listener.onAuthStateChanged(user != null);
            }
        };

        // Add the auth state listener.
        mAuth.addAuthStateListener(mAuthListener);

        // Assign the caller for callbacks.
        try {
            listener = (ResponseListener) caller;
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling class must implement the interface ResponseListener");
        }
    }

    /**
     * Returns the current user as a user object.
     */
    public static void getCurrentUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName();
            // TODO: return a User object with the right information.
        }
    }

    public void createAccount(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(caller, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(caller, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(caller, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            Toast.makeText(caller, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Cleans up by removing the listener.
     */
    public void dispose() {
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    /**
     * Listener that handles the FireBase responses.
     */
    public interface ResponseListener {
        /**
         * Gets called when the auth state changes.
         * @param isLoggedIn whether the user is now logged in or not.
         */
        void onAuthStateChanged(Boolean isLoggedIn);
    }
}
