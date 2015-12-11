package com.example.olivi.maphap;

import com.example.olivi.maphap.data.EventDatabase;
import com.example.olivi.maphap.data.EventsColumns;
import com.example.olivi.maphap.data.RegionsColumns;
import com.example.olivi.maphap.data.VenuesColumns;
import com.example.olivi.maphap.service.EventsDataJsonParser;

/**
 * Created by olivi on 12/8/2015.
 */
public class Projections {

    public static final String[] REGION_COLUMNS = {
            EventDatabase.REGIONS + "." + RegionsColumns._ID,
            EventDatabase.REGIONS + "." + RegionsColumns.LATITUDE,
            EventDatabase.REGIONS + "." + RegionsColumns.LONGITUDE,
            EventDatabase.REGIONS + "." + RegionsColumns.RADIUS,


    };

    public static final String[] EVENT_COLUMNS = {
            EventDatabase.EVENTS + "." + EventsColumns._ID,
            EventsColumns.CAPACITY,
            EventsColumns.CATEGORY,
            EventsColumns.DESCRIPTION,
            EventsColumns.EB_ID,
            EventsColumns.END_DATE_TIME,
            EventsColumns.EVENTBRITE_VENUE_ID,
            EventsColumns.LOGO_URL,
            EventsColumns.NAME,
            EventsColumns.START_DATE_TIME,
            EventsColumns.STATUS,
            EventsColumns.URL
    };

    public static final String[] VENUE_COLUMNS = {
            VenuesColumns._ID,
            VenuesColumns.NAME,
            VenuesColumns.EB_VENUE_ID,
            VenuesColumns.LATITUDE,
            VenuesColumns.LONGITUDE,
    };


    // These indices are tied to REGION_COLUMNS.  If REGION_COLUMNS changes, these
    // must change.

    public static class Regions {
        static final int COL_ID = 0;
        static final int COL_LATITUDE = 1;
        static final int COL_LONGITUDE = 2;
        static final int COL_RADIUS = 3;
    }

    // These indices are tied to EVENT_COLUMNS.  If EVENT_COLUMNS changes, these
    // must change.

    public static class Events {
        static final int COL_ID = 0;
        static final int COL_CAPACITY = 1;
        static final int COL_CATEGORY = 2;
        static final int COL_DESCRIPTION = 3;
        static final int EB_ID = 4;
        static final int END_DATE_TIME = 5;
        static final int EVENTBRITE_VENUE_ID = 6;
        static final int LOGO_URL = 7;
        static final int NAME = 8;
        static final int START_DATE_TIME = 9;
        static final int STATUS = 10;
        static final int URL = 11;
    }


}
