package com.example.olivi.maphap;

import com.example.olivi.maphap.data.EventDatabase;
import com.example.olivi.maphap.data.EventsColumns;
import com.example.olivi.maphap.data.RegionsColumns;
import com.example.olivi.maphap.data.VenuesColumns;
import com.example.olivi.maphap.service.EventsDataJsonParser;

/**
 * Created by olivia on 12/8/2015.
 * This class lists the columns we seek from the database.
 */
public class Projections {

    public static final String[] REGION_COLUMNS = {
            EventDatabase.REGIONS + "." + RegionsColumns._ID,
            EventDatabase.REGIONS + "." + RegionsColumns.LATITUDE,
            EventDatabase.REGIONS + "." + RegionsColumns.LONGITUDE,
            EventDatabase.REGIONS + "." + RegionsColumns.RADIUS,
            EventDatabase.REGIONS + "." + RegionsColumns.ADDED_DATE_TIME


    };

    public static final String[] EVENT_COLUMNS = {
            EventsColumns.NAME,
            EventsColumns.DESCRIPTION,
            EventsColumns.CAPACITY,
            EventsColumns.CATEGORY,
            EventsColumns.STATUS,
            EventsColumns.URL,
            EventsColumns.LOGO_URL,
            EventsColumns.START_DATE_TIME,
            EventsColumns.END_DATE_TIME,
            VenuesColumns.NAME,
            VenuesColumns.LATITUDE,
            VenuesColumns.LONGITUDE,
            EventDatabase.EVENTS + "." + EventsColumns._ID
    };


    // These indices are tied to REGION_COLUMNS.  If REGION_COLUMNS changes, these
    // must change.

    public static class Regions {
        public static final int COL_ID = 0;
        public static final int COL_LATITUDE = 1;
        public static final int COL_LONGITUDE = 2;
        public static final int COL_RADIUS = 3;
        public static final int ADDED_DATE_TIME = 4;
    }

    // These indices are tied to EVENT_COLUMNS.  If EVENT_COLUMNS changes, these
    // must change.

    public static class Events {
        static final int COL_NAME = 0;
        static final int COL_DESCRIPTION = 1;
        static final int COL_CAPACITY = 2;
        static final int COL_CATEGORY = 3;
        static final int COL_STATUS = 4;
        static final int COL_URL = 5;
        static final int COL_LOGO_URL = 6;
        static final int COL_START_DATE_TIME = 7;
        static final int COL_END_DATE_TIME = 8;
        static final int COL_VENUE_NAME = 9;
        static final int COL_VENUE_LAT = 10;
        static final int COL_VENUE_LON = 11;
        static final int COL_EVENT_ID = 12;
    }
}
