package com.example.olivi.maphap.service;

import android.content.ContentValues;
import android.util.Log;

import com.example.olivi.maphap.data.EventsAndRegionsColumns;
import com.example.olivi.maphap.data.EventsColumns;
import com.example.olivi.maphap.data.VenuesColumns;
import com.example.olivi.maphap.utils.Constants;
import com.example.olivi.maphap.utils.DateUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

/**
 * Created by olivia on 11/29/2015.
 */
public class EventsDataJsonParser {

    private static final String LOG_TAG = EventsDataJsonParser.class.getSimpleName();

    final String EB_EVENTS = "events";

    //Event attributes
    final String EB_NAME = "name";
    final String EB_ID = "id";
    final String EB_EVENT_TEXT = "text";
    final String EB_EVENT_HTML = "html";
    final String EB_EVENT_DESCRIPTION = "description";
    final String EB_URL = "url";
    final String EB_EVENT_START = "start";
    final String EB_EVENT_END = "end";
    final String EB_EVENT_DATE_TIME_LOCAL = "local";
    final String EB_EVENT_CAPACITY = "capacity";
    final String EB_EVENT_STATUS = "status";

    //Public expansions
    final String EB_LOGO = "logo";
    final String EB_VENUE = "venue";
    final String EB_LATITUDE = "latitude";
    final String EB_LONGITUDE = "longitude";
    final String EB_CATEGORY_ID = "category_id";

    String mEventsJson;
    long mRegionId;
    double mJulianDateAdded;
    Vector<ContentValues>  mEventsCVVector;
    Vector<ContentValues>  mVenuesCVVector;
    Vector<ContentValues>  mEventsAndRegionCVVector;

    public EventsDataJsonParser(String jsonStr, long regionId, double
            julianDateAdded) {
        mEventsJson = jsonStr;
        mRegionId = regionId;
        mJulianDateAdded = julianDateAdded;
    }

    public void parse() throws JSONException {

        try {
            JSONObject eventsJson = new JSONObject(mEventsJson);
            JSONArray eventsArray = eventsJson.getJSONArray(EB_EVENTS);

            mVenuesCVVector = new Vector<ContentValues>(eventsArray.length());
            mEventsCVVector = new Vector<ContentValues>(eventsArray.length());
            mEventsAndRegionCVVector = new Vector<ContentValues>(eventsArray.length());

            Log.i(LOG_TAG, "eventsArray.length() is " + eventsArray.length());

            for (int i = 0; i < (eventsArray.length() <= Constants.MAX_EVENTS_PER_REQUEST ?
                    eventsArray.length() : Constants.MAX_EVENTS_PER_REQUEST) ; i++) {
                //TODO change for loop limit to size of array to be parsed
                // These are the values that will be collected for the events table
                String name;
                String eventBriteId;
                String description = ""; //This is optional so it may not get set during parsing
                String url;
                long startLocal;
                long endLocal;
                int capacity;
                String status;
                String logoUrl = ""; //This is optional so it may not get set during parsing
                String category = ""; //This is optional so it may not get set during parsing

                //These are the values that will be collected for the venues table
                //This object is optional so these must all be initialized b/c they may not be set
                //during parsing
                String venueName = "";
                String venueEBId = "";
                double venueLat = 0.0;
                double venueLong = 0.0;

                // Get the JSON object representing the event
                JSONObject event = eventsArray.getJSONObject(i);

                name = event.getJSONObject(EB_NAME).getString(EB_EVENT_TEXT);
                eventBriteId = event.getString(EB_ID);

                url = event.getString(EB_URL);

                //Json objects representing start and end time date and timezones.
                startLocal = DateUtils.convertDateTimeStringToLong(event.getJSONObject
                        (EB_EVENT_START)
                        .getString(EB_EVENT_DATE_TIME_LOCAL));

                Log.i(LOG_TAG, "putting start time into database: " + startLocal);
                endLocal = DateUtils.convertDateTimeStringToLong(event.getJSONObject
                        (EB_EVENT_END).getString
                        (EB_EVENT_DATE_TIME_LOCAL));
                Log.i(LOG_TAG, "putting end time into database: " + endLocal);

                capacity = event.getInt(EB_EVENT_CAPACITY);
                status = event.getString(EB_EVENT_STATUS);

                //The following 4 items are optional meaning they may be null if the organiser
                //didn't provide values for them. Therefore we need to check if they exist and
                //are not null, otherwise a JSONException will be thrown.

                if (!event.isNull(EB_EVENT_DESCRIPTION)) {
                    description = event.getJSONObject
                            (EB_EVENT_DESCRIPTION).getString(EB_EVENT_HTML);
                }

                if (!event.isNull(EB_LOGO)) {
                    logoUrl = event.getJSONObject(EB_LOGO).getString(EB_URL);
                }

                if (!event.isNull(EB_CATEGORY_ID)) {
                    category = event.getString(EB_CATEGORY_ID);
                }

                if (!event.isNull(EB_VENUE)) {
                    JSONObject venue = event.getJSONObject(EB_VENUE);
                    venueName = venue.getString(EB_NAME);
                    venueEBId = venue.getString(EB_ID);
                    venueLat = Double.parseDouble(venue.getString(EB_LATITUDE));
                    venueLong = Double.parseDouble(venue.getString(EB_LONGITUDE));
                } else {
                    //If there is no venue object, we should skip this iteration because
                    //we can't display an event with no venue on a map.
                    Log.i(LOG_TAG, "venue for this event is null. skipping event");
                    continue;
                }

                ContentValues venueValues = new ContentValues();

                venueValues.put(VenuesColumns.NAME, venueName);
                venueValues.put(VenuesColumns.EB_VENUE_ID, venueEBId);
                venueValues.put(VenuesColumns.LATITUDE, venueLat);
                venueValues.put(VenuesColumns.LONGITUDE, venueLong);
                mVenuesCVVector.add(venueValues);

                ContentValues eventValues = new ContentValues();
                eventValues.put(EventsColumns.EVENTBRITE_VENUE_ID, venueEBId);
                eventValues.put(EventsColumns.NAME, name);
                eventValues.put(EventsColumns.EB_ID, eventBriteId);
                eventValues.put(EventsColumns.DESCRIPTION, description);
                eventValues.put(EventsColumns.URL, url);
                eventValues.put(EventsColumns.START_DATE_TIME, startLocal);
                eventValues.put(EventsColumns.END_DATE_TIME, endLocal);
                eventValues.put(EventsColumns.CAPACITY, capacity);
                eventValues.put(EventsColumns.STATUS, status);
                eventValues.put(EventsColumns.LOGO_URL, logoUrl);
                eventValues.put(EventsColumns.CATEGORY, category);
                eventValues.put(EventsColumns.ADDED_DATE_TIME, mJulianDateAdded);
                mEventsCVVector.add(eventValues);

                ContentValues eventsAndRegionValues = new ContentValues();
                eventsAndRegionValues.put(EventsAndRegionsColumns.REGION_ID, mRegionId);
                eventsAndRegionValues.put(EventsAndRegionsColumns.EVENT_ID, eventBriteId);
                eventsAndRegionValues.put(EventsAndRegionsColumns.ADDED_DATE_TIME, mJulianDateAdded);
                mEventsAndRegionCVVector.add(eventsAndRegionValues);
            }

            Log.d(LOG_TAG, "MapHap Service Complete. " + mEventsCVVector.size() + " Inserted");

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    public ContentValues[] getContentValues(int dataType) {
        switch (dataType) {
            case MapHapService.VENUES:
                return getContentValuesArray(mVenuesCVVector);
            case MapHapService.EVENTS:
                return getContentValuesArray(mEventsCVVector);
            case MapHapService.EVENTS_REGIONS:
                return getContentValuesArray(mEventsAndRegionCVVector);
            default:
                throw new IllegalArgumentException("dataType must correspond to one of the values " +
                        "in MapHapService.REQUIRED_CONTENT_VALUES");
        }
    }

    //Helper method to return a ContentValues array from a Vector.
    private ContentValues[] getContentValuesArray(Vector<ContentValues> cvVector) {
        ContentValues[] cvArray = new ContentValues[0];

        if (cvVector.size() > 0) {
            cvArray = new ContentValues[cvVector.size()];
            cvVector.toArray(cvArray);
        }
        return cvArray;
    }
}