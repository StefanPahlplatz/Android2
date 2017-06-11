package com.app.eu.proximitymap;

import com.app.eu.proximitymap.Utils.DistanceCalculator;
import com.google.android.gms.maps.model.LatLng;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Container for all unit tests.
 */
public class UnitTests {
    @Test
    public void distanceBetweenTwoPoints() throws Exception {
        DistanceCalculator calculator = new DistanceCalculator();
        LatLng point1 = new LatLng(51.8, 4.8);
        LatLng point2 = new LatLng(51.7, 4.9);

        double distance = calculator.getDistance(point1, point2);

        assertEquals(13080, distance, 5);
    }
}
