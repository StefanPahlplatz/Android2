package com.android.eu.proximitymap;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * User location class that can be inserted directly into the database.
 */

@IgnoreExtraProperties
public class UserLocation {

    public Double lat;
    public Double lng;

    public UserLocation() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public UserLocation(Double lat, Double lng) {
        this.lat = lat;
        this.lng = lng;
    }
}
