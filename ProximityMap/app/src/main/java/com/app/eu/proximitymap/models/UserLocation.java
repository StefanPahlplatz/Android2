package com.app.eu.proximitymap.models;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Wrapper for SimpleLocation.
 */

public class UserLocation extends SimpleLocation {

    private static final float MARKER_COLOUR = BitmapDescriptorFactory.HUE_BLUE;

    public final String uid;

    public UserLocation(Double lat, Double lng, String name, String uid) {
        this(new SimpleLocation(lat, lng, name), uid);
    }

    /**
     * Initializes a userlocation.
     * @param simpleLocation existing location object.
     * @param uid of the user that the position belongs to.
     */
    public UserLocation(SimpleLocation simpleLocation, String uid) {
        super(simpleLocation.lat, simpleLocation.lng, simpleLocation.name);
        this.uid = uid;
    }

    public LatLng getLatLng() {
        return new LatLng(this.lat, this.lng);
    }

    public MarkerOptions getMarkerOptions() {
        // Create marker
        MarkerOptions newMarker = new MarkerOptions();
        newMarker.position(getLatLng());
        newMarker.title(name);
        newMarker.icon(BitmapDescriptorFactory.defaultMarker(MARKER_COLOUR));

        return newMarker;
    }

    @Override
    public String toString() {
        return "name: " + name +
                "\nlat: " + lat +
                "\nlng: " + lng;
    }
}
