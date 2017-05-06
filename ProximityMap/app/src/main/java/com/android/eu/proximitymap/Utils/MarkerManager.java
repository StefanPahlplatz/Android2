package com.android.eu.proximitymap.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.android.eu.proximitymap.models.SimpleLocation;
import com.android.eu.proximitymap.models.User;
import com.android.eu.proximitymap.models.UserLocation;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

/**
 * Marker manager for all markers on the map.
 */

public class MarkerManager {

    private String mUid;
    private HashMap<UserLocation, Marker> mMarkers;
    private GoogleMap mMap;


    /**
     * Initializes the class.
     *
     * @param map google map instance.
     * @param uid the current logged in user's uid.
     */
    public MarkerManager(GoogleMap map, String uid) {
        if (uid.isEmpty()) {
            throw new IllegalArgumentException("UID can't be empty");
        }
        if (map == null) {
            throw new IllegalArgumentException("Map can't be null");
        }

        this.mMap = map;
        this.mUid = uid;

        mMarkers = new HashMap<>();
    }

    /**
     * Removes the key from the map.
     *
     * @param key to remove.
     */
    public void remove(String key) {
        for (UserLocation loc : mMarkers.keySet()) {
            if (loc.uid.equals(key)) {
                mMarkers.remove(loc).remove();
            }
        }
    }

    /**
     * Adds a marker to the map.
     *
     * @param dataSnapshot of the marker.
     */
    public void addMarker(DataSnapshot dataSnapshot) {
        final String uid = dataSnapshot.getKey();
        final UserLocation newLocation =
                new UserLocation(dataSnapshot.getValue(SimpleLocation.class), uid);

        // Don't place markers for yourself.
        if (uid.equals(mUid)) {
            return;
        }

        // Remove a marker the user previously placed.
        remove(uid);

        // Get marker options.
        MarkerOptions markerOptions = newLocation.getMarkerOptions();

        // Add the marker to the map.
        Marker marker = mMap.addMarker(markerOptions);
        marker.setVisible(false);

        // Save the marker.
        mMarkers.put(newLocation, marker);

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(uid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                // Load the user's picture.
                new MarkerManager.MarkerIconTask(uid).execute(user.picture);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    /**
     * Returns the user location that corresponds to the passed uid.
     *
     * @param uid search.
     * @return location of the uid.
     */
    private UserLocation getUserLocationByUid(String uid) {
        for (UserLocation loc : mMarkers.keySet()) {
            if (loc.uid.equals(uid)) {
                return loc;
            }
        }
        return null;
    }

    /**
     * Load a user's profile picture and set it as their marker icon.
     */
    private class MarkerIconTask extends AsyncTask<String, Void, Bitmap> {

        /**
         * Uid of the user that belongs to the icon that is being loaded.
         */
        final private String uid;

        MarkerIconTask(String uid) {
            this.uid = uid;
        }

        /**
         * @param params at index 0 is the download link for the bitmap.
         * @return bitmap or null.
         */
        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bmp = null;
            try {
                URL url = new URL(params[0]);
                bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            BitmapActions bitmapActions = new BitmapActions();
            return bitmapActions.adjust(bmp, true, true, true);
        }

        /**
         * Set the bitmap as marker image for the corresponding user.
         *
         * @param bitmap user image.
         */
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            setIcon(uid, bitmap);
        }

        /**
         * Sets the icon for the user corresponding to the uid.
         *
         * @param uid    user.
         * @param bitmap profile picture.
         */
        private void setIcon(String uid, Bitmap bitmap) {
            try {
                Marker marker = mMarkers.get(getUserLocationByUid(uid));

                if (!marker.isVisible()) {
                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
                    marker.setVisible(true);
                }
            } catch (NullPointerException e) {
                Log.d("ICON", "Couldn't find corresponding marker");
            }
        }
    }
}
