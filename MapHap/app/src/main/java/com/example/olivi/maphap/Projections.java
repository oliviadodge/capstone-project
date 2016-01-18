package com.example.olivi.maphap;

import com.example.olivi.maphap.data.CategoriesAndRegionsColumns;
import com.example.olivi.maphap.data.EventDatabase;
import com.example.olivi.maphap.data.EventsColumns;
import com.example.olivi.maphap.data.RegionsColumns;
import com.example.olivi.maphap.data.VenuesColumns;

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
    public static final String[] CATEGORIES_COLUMNS = {
            EventDatabase.CATEGORIES_REGIONS + "." + CategoriesAndRegionsColumns.CATEGORY_ID,
            EventDatabase.CATEGORIES_REGIONS + "." + CategoriesAndRegionsColumns.ADDED_DATE_TIME,


    };

    // These indices are tied to REGION_COLUMNS.
    // If REGION_COLUMNS changes, these
    // must change.

    public static class Regions {
        public static final int COL_ID = 0;
        public static final int COL_LATITUDE = 1;
        public static final int COL_LONGITUDE = 2;
        public static final int COL_RADIUS = 3;
        public static final int ADDED_DATE_TIME = 4;
    }

    public static class Categoires {
        public static final int COL_CATEGORY_ID = 0;
        public static final int COL_DATE_ADDED = 1;
    }


    public static final String[] EVENT_COLUMNS_LIST_VIEW = {
            EventsColumns.NAME,
            EventsColumns.CATEGORY,
            EventsColumns.LOGO_URL,
            EventsColumns.START_DATE_TIME,
            EventsColumns.END_DATE_TIME,
            VenuesColumns.NAME,
            VenuesColumns.LATITUDE,
            VenuesColumns.LONGITUDE,
            EventDatabase.EVENTS + "." + EventsColumns._ID
    };

    // These indices are tied to EVENT_COLUMNS_LIST_VIEW.
    // If EVENT_COLUMNS_LIST_VIEW changes, these
    // must change.

    public static class EventsListView {
        public static final int COL_NAME = 0;
        public static final int COL_CATEGORY = 1;
        public static final int COL_LOGO_URL = 2;
        public static final int COL_START_DATE_TIME = 3;
        public static final int COL_END_DATE_TIME = 4;
        public static final int COL_VENUE_NAME = 5;
        public static final int COL_VENUE_LAT = 6;
        public static final int COL_VENUE_LON = 7;
        public static final int COL_EVENT_ID = 8;
    }


    public static final String[] EVENT_COLUMNS_DETAIL_VIEW = {
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
            EventDatabase.EVENTS + "." + EventsColumns._ID
    };

    // These indices are tied to EVENT_COLUMNS_DETAIL_VIEW.
    // If EVENT_COLUMNS_DETAIL_VIEW
    // changes, these must change.

    public static class EventsDetailView {
        public static final int COL_NAME = 0;
        public static final int COL_DESCRIPTION = 1;
        public static final int COL_CAPACITY = 2;
        public static final int COL_CATEGORY = 3;
        public static final int COL_STATUS = 4;
        public static final int COL_URL = 5;
        public static final int COL_LOGO_URL = 6;
        public static final int COL_START_DATE_TIME = 7;
        public static final int COL_END_DATE_TIME = 8;
        public static final int COL_VENUE_NAME = 9;
        public static final int COL_EVENT_ID = 10;
    }

}
