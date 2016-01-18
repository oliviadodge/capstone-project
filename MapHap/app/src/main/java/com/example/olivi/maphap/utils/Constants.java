package com.example.olivi.maphap.utils;

/**
 * Created by olivi on 12/8/2015.
 */
public class Constants {
//Constant that allows some wiggle room for the search regions' epicenters.
//ie. if a person moves less than this distance from where they originally
//did a search, this new location will not be recorded because the previous
//one will suffice for the purposes of this app.

    public static final double TOLERANCE_DIST_IN_MILES = 1.0;
    public static final int MAP_ZOOM_LEVEL = 10;
    public static final int MAP_ZOOM_LEVEL_CLOSE = 14;


    //Networking constants
    public static final boolean RETURN_POPULAR_ARG = true;
    public static final String SORT_BY_ARG = "best";
    public static final int MAX_EVENTS_PER_REQUEST = 200;


    public static final long MILLIS_IN_A_DAY = 86400000;

    //Preference Keys
    public static final String PREF_RADIUS_KEY = "radius_key";
    public static final String PREF_LATITUDE_KEY = "latitude_key";
    public static final String PREF_LONGITUDE_KEY = "longitude_key";
    public static final String PREF_REGION_ID_KEY = "region_id_key";
    public static final String PREF_START_DATE_KEY = "start_date_key";
    public static final String PREF_END_DATE_KEY = "end_date_key";
    public static final String PREF_CATEGORY_KEY = "category_key";



}
