package com.example.olivi.maphap.utils;

import com.google.android.gms.maps.model.LatLng;

import junit.framework.TestCase;

/**
 * Created by olivi on 12/14/2015.
 */

public class MilesBetweenPointsTest extends TestCase{
    protected LatLng vanKleef;
    protected LatLng somar;
    protected LatLng home;
    protected LatLng cornerStore;


    protected void setUp() {
        vanKleef = new LatLng(37.806550, -122.270497);
        somar = new LatLng(37.807343, -122.270330);
        home = new LatLng(37.808237, -122.286235);
        cornerStore = new LatLng(37.809695, -122.285881);
    }
    public void testMethod() {

        double miles = LocationUtils.milesBetweenTwoPoints(vanKleef.latitude, vanKleef.longitude,
                somar.latitude, somar.longitude);
        assertTrue(miles <= 0.25);

        double dist = LocationUtils.milesBetweenTwoPoints(home.latitude, home.longitude,
                cornerStore.latitude, cornerStore.longitude);
        assertTrue(dist <= 0.25);

        double miles2 = LocationUtils.milesBetweenTwoPoints(vanKleef.latitude, vanKleef.longitude,
                home.latitude, home.longitude);
        assertFalse(miles2 <= 0.25);

        double dist2 = LocationUtils.milesBetweenTwoPoints(somar.latitude, somar.longitude,
                cornerStore.latitude, cornerStore.longitude);
        assertFalse(dist2 <= 0.25);
    }
}