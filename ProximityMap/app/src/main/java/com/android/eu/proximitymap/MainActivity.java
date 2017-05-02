package com.android.eu.proximitymap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_logout).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // Logout button.
        if (v.getId() == R.id.btn_logout) {
            // Sign out of firebase.
            FirebaseAuth.getInstance().signOut();
            Log.d("AUTH", "User logged out");

            // Start the login activity.
            Intent loginActivity = new Intent(this, LoginActivity.class);
            startActivity(loginActivity);

            finish();
        }
    }
}
