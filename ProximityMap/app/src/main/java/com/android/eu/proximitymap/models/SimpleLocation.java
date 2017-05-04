package com.android.eu.proximitymap.models;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * User location class that can be inserted directly into the database.
 */

@IgnoreExtraProperties
public class SimpleLocation {

    public Double lat;
    public Double lng;
    public String name;

    public SimpleLocation() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public SimpleLocation(Double lat, Double lng, String name) {
        this.lat = lat;
        this.lng = lng;
        this.name = name;
    }
}
