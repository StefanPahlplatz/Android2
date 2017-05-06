package com.android.eu.proximitymap.Utils;

import com.google.android.gms.maps.model.LatLng;

import static java.lang.StrictMath.toIntExact;


/**
 * Class to calculate the distance between two points.
 */

public class DistanceCalculator {

    /**
     * This uses the ‘haversine’ formula to calculate the great-circle distance between two points
     * – that is, the shortest distance over the earth’s surface – giving an ‘as-the-crow-flies’
     * distance between the points
     * @param pos1 position one.
     * @param pos2 position two.
     * @return the distance between the points in meters.
     */
    public double getDistance(LatLng pos1, LatLng pos2) {
        double R = 6371E3;
        double φ1 = Math.toRadians(pos1.latitude);
        double φ2 = Math.toRadians(pos2.latitude);
        double Δφ = Math.toRadians(pos2.latitude - pos1.latitude);
        double Δλ = Math.toRadians(pos2.longitude - pos1.longitude);

        double a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
                Math.cos(φ1) * Math.cos(φ2) *
                        Math.sin(Δλ / 2) * Math.sin(Δλ / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return Math.round(R * c);
    }
}
