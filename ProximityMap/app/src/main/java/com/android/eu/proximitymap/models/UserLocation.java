package com.android.eu.proximitymap.models;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Wrapper for SimpleLocation.
 */

public class UserLocation extends SimpleLocation {
    public String uid;

    public UserLocation(Double lat, Double lng, String name, String uid) {
        this(new SimpleLocation(lat, lng, name), uid);
    }

    public UserLocation(SimpleLocation simpleLocation, String uid) {
        super(simpleLocation.lat, simpleLocation.lng, simpleLocation.name);
        this.uid = uid;
    }

    private LatLng getLatLng() {
        return new LatLng(this.lat, this.lng);
    }

    public MarkerOptions getMarkerOptions(float markerColour) {
        // Create marker
        MarkerOptions newMarker = new MarkerOptions();
        newMarker.position(getLatLng());
        newMarker.title(name);
        newMarker.icon(BitmapDescriptorFactory.defaultMarker(markerColour));

        return newMarker;
    }
}
