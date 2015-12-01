package com.example.olivi.maphap.service;

import android.content.ContentValues;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

/**
 * Created by olivi on 11/29/2015.
 */
public class EventsDataJsonParser {

    private static final String LOG_TAG = EventsDataJsonParser.class.getSimpleName();

    final String EB_EVENTS = "events";

    //Event attributes
    final String EB_NAME = "name";
    final String EB_EVENT_TEXT = "text";
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
    final String EB_CATEGORY = "category";

    String mEventsJson;
    Vector<ContentValues>  mEventsCVVector;
    Vector<ContentValues>  mVenuesCVVector;

    public EventsDataJsonParser(String jsonStr) {
        mEventsJson = jsonStr;
    }

    public void parse() {

        try {
            JSONObject eventsJson = new JSONObject(mEventsJson);
            JSONArray eventsArray = eventsJson.getJSONArray(EB_EVENTS);

            mEventsCVVector = new Vector<ContentValues>(eventsArray.length());

            for (int i = 0; i < 5; i++) {
                //TODO change for loop limit to size of array to be parsed
                // These are the values that will be collected for the events table
                String name;
                String description;
                String url;
                String startLocal;
                String endLocal;
                int capacity;
                String status;
                String logoUrl;
                String category;

                //These are the values that will be collected for the venues table
                String venueName;
                double venueLat;
                double venueLong;

                // Get the JSON object representing the event
                JSONObject event = eventsArray.getJSONObject(i);

                Log.i(LOG_TAG, "Here's the event JSONObject" + event);

                name = event.getJSONObject(EB_NAME).getString(EB_EVENT_TEXT);
                description = event.getJSONObject(EB_EVENT_DESCRIPTION).getString(EB_EVENT_TEXT);
                url = event.getString(EB_URL);

                //Json objects representing start and end time date and timezones.
                startLocal = event.getJSONObject(EB_EVENT_START).getString(EB_EVENT_DATE_TIME_LOCAL);
                endLocal = event.getJSONObject(EB_EVENT_END).getString(EB_EVENT_DATE_TIME_LOCAL);

                capacity = event.getInt(EB_EVENT_CAPACITY);
                status = event.getString(EB_EVENT_STATUS);

                logoUrl = event.getJSONObject(EB_LOGO).getString(EB_URL);
                category = event.getJSONObject(EB_CATEGORY).getString(EB_NAME);

                JSONObject venue = event.getJSONObject(EB_VENUE);
                venueName = venue.getString(EB_NAME);
                venueLat = Double.parseDouble(venue.getString(EB_LATITUDE));
                venueLong = Double.parseDouble(venue.getString(EB_LONGITUDE));



                ContentValues eventValues = new ContentValues();

                //TODO make ContentValues from event data and add to content values vector

//                mEventsCVVector.add(eventValues);
//                Log.i(LOG_TAG, "Got JSON event object: " + name
//                                + " " +
//                                description
//                                + " " +
//                                url
//                                + " " +
//                                startLocal
//                                + " " +
//                                endLocal
//                                + " " +
//                                capacity
//                                + " " +
//                                status
//                );

                Log.i(LOG_TAG, "Got more info: " + logoUrl
                                + " " +
                                category
                                + " " +
                                venueName
                                + " " +
                                venueLat
                                + " " +
                                venueLong
                );
            }

            int inserted = 0;
            // add to database


            Log.d(LOG_TAG, "MapHap Service Complete. " + mEventsCVVector.size() + " Inserted");

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    public ContentValues[] getEventsContentValues() {
        ContentValues[] cvArray = new ContentValues[0];

        if (mEventsCVVector.size() > 0) {
            cvArray = new ContentValues[mEventsCVVector.size()];
            mEventsCVVector.toArray(cvArray);
        }
        return cvArray;
    }
}