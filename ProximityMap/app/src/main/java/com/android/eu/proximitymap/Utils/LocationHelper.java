package com.android.eu.proximitymap.Utils;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.android.eu.proximitymap.models.SimpleLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Helper class that handles location updates. It sync your own location to
 * the firebase database and calls the interface method updateLocation() when the location
 * changes.
 * <p>
 * Calling class must implement LocationHelper.LocationListener
 */

public class LocationHelper implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener {

    private static final int UPDATE_LOCATION_INTERVAL = 2000;

    private GoogleApiClient mGoogleApiClient;
    private Context ctx;
    private String mUid;
    private String mDisplayName;
    private LocationListener locationListener;

    /**
     * Initializes the helper
     *
     * @param ctx         context.
     * @param uid         user's uid.
     * @param displayName user's display name.
     */
    public LocationHelper(Context ctx, String uid, String displayName) {
        this.ctx = ctx;
        this.locationListener = (LocationListener) ctx;
        this.mUid = uid;
        this.mDisplayName = displayName;
    }

    /**
     * Updates the location of the current mUser in the database and calls the
     * updateLocation method to notify listeners.
     *
     * @param location new location.
     */
    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        SimpleLocation loc = new SimpleLocation(latLng.latitude, latLng.longitude, mDisplayName);

        // Set the location
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("locations");
        mDatabase.child(mUid).setValue(loc);

        // Call the interface to update the location in listening classes.
        locationListener.updateLocation(latLng);
    }

    /**
     * Connected to location services.
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest mLocationRequest = new LocationRequest();
        // TODO: Make this a setting.
        mLocationRequest.setInterval(UPDATE_LOCATION_INTERVAL);
        mLocationRequest.setFastestInterval(UPDATE_LOCATION_INTERVAL);
        // TODO: Make this a setting.
        mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);

        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        } else {
            noLocationPermissions();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(ctx, "Couldn't connect to the servers.", Toast.LENGTH_SHORT).show();
    }

    public synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(ctx)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        }
    }

    /**
     * User denied location permissions.
     */
    private void noLocationPermissions() {
        Log.e("PERMISSIONS", "USER DENIED LOCATION PERMISSION");
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        // TODO: handle user denying location permission.
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setMessage("You can't use this application without GPS permissions")
                .setPositiveButton("I understand", dialogClickListener).show();
    }

    /**
     * Removes the location listener.
     */
    public void dispose() {
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    /**
     * Callback interface to update the location.
     */
    public interface LocationListener {
        void updateLocation(LatLng latLng);
    }
}
