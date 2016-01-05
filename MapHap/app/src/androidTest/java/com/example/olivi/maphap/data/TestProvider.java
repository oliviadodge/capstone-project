/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.olivi.maphap.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.olivi.maphap.utils.DateUtils;

/*
    Note: This is not a complete set of tests of the Sunshine ContentProvider, but it does test
    that at least the basic functionality has been implemented correctly.

    Students: Uncomment the tests in this class as you implement the functionality in your
    ContentProvider to make sure that you've implemented things reasonably correctly.
 */
public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    /*
       This helper function deletes all records from both database tables using the ContentProvider.
       It also queries the ContentProvider to make sure that the database has been successfully
       deleted, so it cannot be used until the Query and Delete functions have been written
       in the ContentProvider.

       Students: Replace the calls to deleteAllRecordsFromDB with this one after you have written
       the delete functionality in the ContentProvider.
     */
    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                EventProvider.Events.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                EventProvider.Regions.CONTENT_URI,
                null,
                null
        );

        mContext.getContentResolver().delete(
                EventProvider.Venues.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                EventProvider.EventsAndRegions.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                EventProvider.Events.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from events table during delete", 0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                EventProvider.Regions.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from regions table during delete", 0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                EventProvider.Venues.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from venues table during delete", 0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                EventProvider.EventsAndRegions.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from events_regions table during delete", 0, cursor.getCount());
        cursor.close();

    }

    /*
        Student: Refactor this function to use the deleteAllRecordsFromProvider functionality once
        you have implemented delete functionality there.
     */
    public void deleteAllRecords() {
        deleteAllRecordsFromProvider();
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    // Make sure we can still delete after adding/updating stuff
    //
    // Student: Uncomment this test after you have completed writing the delete functionality
    // in your provider.  It relies on insertions with testInsertReadProvider, so insert and
    // query functionality must also be complete before this test can be used.
    public void testDeleteRecords() {
        testBulkInsert();

        // Register a content observer for our location delete.
        TestUtilities.TestContentObserver regionObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(
                EventProvider.Regions.CONTENT_URI, true, regionObserver);

        // Register a content observer for our weather delete.
        TestUtilities.TestContentObserver eventObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(
                EventProvider.Events.CONTENT_URI, true, eventObserver);

        String cutOffDateString = DateUtils.getCutOffDateTime();

        mContext.getContentResolver().delete(EventProvider.Regions.CONTENT_URI_DELETE,
                "julianday(" + RegionsColumns.ADDED_DATE_TIME + ") <= ?",
                new String[]{"julianday(" + cutOffDateString + ")"});

        // A eventCursor is your primary interface to the query results.
        Cursor eventCursor = mContext.getContentResolver().query(
                EventProvider.Events.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                EventsColumns._ID + " ASC"
        );


        assertTrue("events table not empty after deleting by cutoff date: " + cutOffDateString,
                eventCursor.getCount() == 0);

        mContext.getContentResolver().unregisterContentObserver(regionObserver);
        mContext.getContentResolver().unregisterContentObserver(eventObserver);
    }


    static private final int BULK_INSERT_RECORDS_TO_INSERT = 10;

    public void testBulkInsert() {
        // first, let's create a location value
        ContentValues testValues = TestUtilities.createNorthPoleRegionValues();
        Uri regionUri = mContext.getContentResolver()
        .insert(EventProvider.Regions.CONTENT_URI, testValues);
        long regionRowId = Long.parseLong(regionUri.getPathSegments().get(1));

        // Verify we got a row back.
        assertTrue(regionRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A eventCursor is your primary interface to the query results.
        Cursor regionCursor = mContext.getContentResolver().query(
                EventProvider.Regions.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testBulkInsert. Error validating RegionEntry.",
                regionCursor, testValues);

        // Now we can bulkInsert some weather.  In fact, we only implement BulkInsert for weather
        // entries.  With ContentProviders, you really only have to implement the features you
        // use, after all.
        ContentValues[] eventContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];
        ContentValues[] venueContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];
        ContentValues[] eventAndRegionContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];

        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++) {

            String venueEbId = Integer.toString(2 * i);
            String eventEBId = Integer.toString(i + 1);

            ContentValues eventValues = new ContentValues();
            eventValues.put(EventsColumns.EB_ID, eventEBId);
            eventValues.put(EventsColumns.EVENTBRITE_VENUE_ID, venueEbId);
            eventValues.put(EventsColumns.CATEGORY, "yo mamma");
            eventValues.put(EventsColumns.CAPACITY, 100);
            eventValues.put(EventsColumns.DESCRIPTION, "event description");
            eventValues.put(EventsColumns.NAME, "event name");
            eventValues.put(EventsColumns.STATUS, "live");
            eventValues.put(EventsColumns.LOGO_URL, "www.logo-authToken.com");
            eventValues.put(EventsColumns.URL, "www.event-authToken.com");
            eventValues.put(EventsColumns.START_DATE_TIME, "2009-08-25T21:00:00");
            eventValues.put(EventsColumns.END_DATE_TIME, "2009-08-31T16:00:00");
            eventContentValues[i] = eventValues;

            ContentValues venueValues = new ContentValues();
            venueValues.put(VenuesColumns.EB_VENUE_ID, venueEbId);
            venueValues.put(VenuesColumns.NAME, "venue name");
            venueValues.put(VenuesColumns.LATITUDE, 1.43);
            venueValues.put(VenuesColumns.LONGITUDE, -1.93);
            venueContentValues[i] = venueValues;

            ContentValues eventAndRegionValues = new ContentValues();
            eventAndRegionValues.put(EventsAndRegionsColumns.REGION_ID, (int) regionRowId);
            Log.i(LOG_TAG, "putting events and regions content values together" +
                    "regionRowId: " + regionRowId + " eventEbId: " + eventEBId);
            eventAndRegionValues.put(EventsAndRegionsColumns.EVENT_ID, eventEBId);
            eventAndRegionContentValues[i] = eventAndRegionValues;

        }

        // Register a content observer for our bulk insert.
        TestUtilities.TestContentObserver eventObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver()
                .registerContentObserver(EventProvider.Events.CONTENT_URI, true, eventObserver);

        int venueInsertCount = mContext.getContentResolver()
                .bulkInsert(EventProvider.Venues.CONTENT_URI, venueContentValues);

        int eventInsertCount = mContext.getContentResolver()
                .bulkInsert(EventProvider.Events.CONTENT_URI, eventContentValues);

        int eventAndRegionInsertCount = mContext.getContentResolver()
                .bulkInsert(EventProvider.EventsAndRegions.CONTENT_URI, eventAndRegionContentValues);

        //If this fails, it means that you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in your BulkInsert
        // ContentProvider method.
        mContext.getContentResolver().unregisterContentObserver(eventObserver);

        assertEquals(eventInsertCount, BULK_INSERT_RECORDS_TO_INSERT);
        assertEquals(venueInsertCount, BULK_INSERT_RECORDS_TO_INSERT);
        assertEquals(eventAndRegionInsertCount, BULK_INSERT_RECORDS_TO_INSERT);

        // A eventCursor is your primary interface to the query results.
        Cursor eventCursor = mContext.getContentResolver().query(
                EventProvider.Events.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                EventsColumns._ID + " ASC"
        );


        Cursor venueCursor = mContext.getContentResolver().query(
                EventProvider.Venues.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                VenuesColumns._ID + " ASC"
        );


        Cursor eventAndRegionCursor = mContext.getContentResolver().query(
                EventProvider.EventsAndRegions.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                EventsAndRegionsColumns.EVENT_ID + " ASC"
        );

        // we should have as many records in the database as we've inserted
        assertEquals(eventCursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        // and let's make sure they match the ones we created
        eventCursor.moveToFirst();
        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, eventCursor.moveToNext() ) {
            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating Event Entry " + i,
                    eventCursor, eventContentValues[i]);
        }
        eventCursor.close();

        // we should have as many records in the database as we've inserted
        assertEquals(venueCursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        // and let's make sure they match the ones we created
        venueCursor.moveToFirst();
        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, venueCursor.moveToNext() ) {
            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating venue Entry " + i,
                    venueCursor, venueContentValues[i]);
        }
        venueCursor.close();

        // we should have as many records in the database as we've inserted
        assertEquals(eventAndRegionCursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        // and let's make sure they match the ones we created
        eventAndRegionCursor.moveToFirst();
        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, eventAndRegionCursor.moveToNext() ) {
            Log.i(LOG_TAG, "From cursor: Event ID = " + eventAndRegionCursor.getString(eventAndRegionCursor
                    .getColumnIndex(EventsAndRegionsColumns.EVENT_ID)));
            Log.i(LOG_TAG, "From cursor: Region ID = " + eventAndRegionCursor.getString(eventAndRegionCursor
                    .getColumnIndex(EventsAndRegionsColumns.REGION_ID)));
        }

        eventAndRegionCursor.moveToFirst();
        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, eventAndRegionCursor.moveToNext() ) {
            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating event " +
                            "and region Entry " + i,
                    eventAndRegionCursor, eventAndRegionContentValues[i]);
        }
    eventAndRegionCursor.close();
    }
}
