package com.example.olivi.maphap.utils;

import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    public static final boolean RETURN_POPULAR = true;
    public static final int MAX_EVENTS_PER_REQUEST = 100;


}
