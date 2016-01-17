package com.example.olivi.maphap.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.olivi.maphap.R;

/**
 * Created by olivi on 12/8/2015.
 */
public class LocationUtils {


    private static final String TAG = LocationUtils.class.getSimpleName();

    //Taken from https://www.geodatasource.com/developers/java

    public static double milesBetweenTwoPoints(double lat1, double lon1, double lat2,
                                               double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) +
                Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;

        return dist;
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }


    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    public static int getPreferredRadius(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(context.getString(R.string.pref_radius_key),
                50);
    }

    public static void savePreferredRadius(Context context, int radius) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt(context.getString(R.string.pref_radius_key), radius);
        editor.commit();
    }

    public static double getPreferredLatitude(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        long lat = prefs.getLong(context.getString(R.string.pref_latitude_key), 0);

        return Double.longBitsToDouble(lat);
    }
    public static double getPreferredLongitude(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        long lon = prefs.getLong(context.getString(R.string.pref_longitude_key), 0);

        return Double.longBitsToDouble(lon);
    }

    public static long getPreferredRegionId(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getLong(context.getString(R.string.pref_region_id_key), -1);
    }

    public static void saveRegionIdToSharedPref(Context context, long regionId) {
        Log.i(TAG, "saving region ID to shared prefs " + regionId);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(context.getString(R.string.pref_region_id_key), regionId);
        editor.commit();
    }


    public static void saveLocationToSharedPref(Context context, double latitude, double
            longitude) {
        Log.i(TAG, "saving latitude and longitude to shared prefs " + latitude + " " + longitude);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(context.getString(R.string.pref_latitude_key), Double.doubleToLongBits(latitude));
        editor.putLong(context.getString(R.string.pref_longitude_key), Double.doubleToLongBits
                (longitude));
        editor.commit();
    }

    public static int getMapZoomLevel(Context context) {
        int radius = getPreferredRadius(context);
        double x = (0.9375 * 24901) / radius;
        return ((int) (Math.log(x) / Math.log(2))) - 1;
    }


}
