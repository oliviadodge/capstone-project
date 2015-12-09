package com.example.olivi.maphap;

import com.example.olivi.maphap.data.EventDatabase;
import com.example.olivi.maphap.data.RegionsColumns;
import com.example.olivi.maphap.service.EventsDataJsonParser;

/**
 * Created by olivi on 12/8/2015.
 */
public class RegionProjections {

    public static final String[] REGION_COLUMNS = {
            EventDatabase.REGIONS + "." + RegionsColumns._ID,
            EventDatabase.REGIONS + "." + RegionsColumns.LATITUDE,
            EventDatabase.REGIONS + "." + RegionsColumns.LONGITUDE,
            EventDatabase.REGIONS + "." + RegionsColumns.RADIUS,


    };

    // These indices are tied to REGION_COLUMNS.  If REGION_COLUMNS changes, these
    // must change.
    static final int COL_ID = 0;
    static final int COL_LATITUDE = 1;
    static final int COL_LONGITUDE= 2;
    static final int COL_RADIUS = 3;

}
