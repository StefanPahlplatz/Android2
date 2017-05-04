package com.android.eu.proximitymap.models;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by stefa on 04/05/2017.
 */

public class UserLocation extends SimpleLocation {
    public String uid;

    public UserLocation(Double lat, Double lng, String name, String uid) {
        super(lat, lng, name);
        this.uid = uid;
    }

    public UserLocation(SimpleLocation simpleLocation, String uid) {
        super(simpleLocation.lat, simpleLocation.lng, simpleLocation.name);
        this.uid = uid;
    }

    public LatLng getLatLng() {
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
