package com.example.olivi.maphap.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.example.olivi.maphap.R;
import com.example.olivi.maphap.data.EventProvider;
import com.example.olivi.maphap.data.EventsColumns;
import com.example.olivi.maphap.data.RegionsColumns;
import com.example.olivi.maphap.data.SearchColumns;
import com.example.olivi.maphap.data.VenuesColumns;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by olivi on 11/18/2015.
 */

public class MapHapService extends IntentService {
    public static final String SEARCH_QUERY_EXTRA = "sqe";
    public static final String LATITUDE_QUERY_EXTRA = "latqe";
    public static final String LONGITUDE_QUERY_EXTRA = "longqe";
    public static final String WITHIN_QUERY_EXTRA = "wqe";

    private static final String EXPANSIONS = "logo,venue,category";

    private final String LOG_TAG = MapHapService.class.getSimpleName();
    public MapHapService() {
        super("MapHap");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String searchQuery = intent.getStringExtra(SEARCH_QUERY_EXTRA);
        double latitude = intent.getDoubleExtra(LATITUDE_QUERY_EXTRA, 0.00);
        String latitudeQuery = Double
                .toString(latitude);
        double longitude = intent.getDoubleExtra(LONGITUDE_QUERY_EXTRA, 0.00);
        String longitudeQuery = Double
                .toString(longitude);
        int within = intent.getIntExtra(WITHIN_QUERY_EXTRA, 50);
        String withinQuery = Integer.toString(within) + "mi";

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String eventsJsonStr = null;


        try {
            // Construct the URL for the Eventbrite query
            // Possible parameters are avaiable at
            // https://www.eventbrite.com/developer/v3/endpoints/events/
            final String EVENTS_BASE_URL =
                    "https://www.eventbriteapi.com/v3/events/search/";
            final String SEARCH_QUERY_PARAM = "q";
            final String LATITUDE_PARAM = "location.latitude";
            final String LONGITUDE_PARAM = "location.longitude";
            final String WITHIN_PARAM = "location.within";
            final String EXPAND_PARAM = "expand";
            final String OAUTH_TOKEN = "token";

            Uri builtUri = Uri.parse(EVENTS_BASE_URL).buildUpon()
                    .appendQueryParameter(SEARCH_QUERY_PARAM, searchQuery)
                    .appendQueryParameter(LATITUDE_PARAM, latitudeQuery)
                    .appendQueryParameter(LONGITUDE_PARAM, longitudeQuery)
                    .appendQueryParameter(WITHIN_PARAM, withinQuery)
                    .appendQueryParameter(EXPAND_PARAM, EXPANSIONS)
                    .appendQueryParameter(OAUTH_TOKEN, getString(R.string.my_personal_oauth_token))
                    .build();

            URL url = new URL(builtUri.toString());

            Log.i(LOG_TAG, "URL to access eventbrite: " + builtUri);

            // Create the request to Eventbrite, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }
            eventsJsonStr = buffer.toString();

            getEventsFromJson(eventsJsonStr, latitude, longitude, within, searchQuery);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the data, there's no point in attempting
            // to parse it.
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private void getEventsFromJson(String eventJsonStr,
                                   double latitude,
                                   double longitude,
                                   int within,
                                   String searchTerm)
            throws JSONException {

        long searchId = addSearchTermToDB(searchTerm);
        long regionId = addRegionToDB(latitude, longitude, within);

        EventsDataJsonParser parser = new EventsDataJsonParser(eventJsonStr, searchId, regionId);
        parser.parse();

        ContentValues regionValues = new ContentValues();
        regionValues.put(RegionsColumns.LATITUDE, latitude);
        regionValues.put(RegionsColumns.LONGITUDE, longitude);
        regionValues.put(RegionsColumns.RADIUS, within);

        ContentValues searchValues = new ContentValues();
        searchValues.put(SearchColumns.SEARCH_TERM, searchTerm);

        ContentValues[] venuesContentValues = parser.getVenuesContentValues();
        ContentValues[] eventsContentValues = parser.getEventsContentValues();
        ContentValues[] eventsAndSearchContentValues = parser.getEventsAndSearchContentValues();
        ContentValues[] eventsAndRegionContentValues = parser.getEventsAndRegionContentValues();

        String[] allEventsColumns = {
                EventsColumns._ID,
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
        String[] allVenuesColumns = {
                VenuesColumns._ID,
                VenuesColumns.NAME,
                VenuesColumns.EVENTBRITE_VENUE_ID,
                VenuesColumns.LATITUDE,
                VenuesColumns.LONGITUDE,
        };

        //Insert the data
        this.getContentResolver().bulkInsert(EventProvider.Venues.CONTENT_URI, venuesContentValues);
        this.getContentResolver().bulkInsert(EventProvider.Events.CONTENT_URI, eventsContentValues);
        this.getContentResolver().bulkInsert(EventProvider
                .EventsAndSearches.CONTENT_URI, eventsAndSearchContentValues);
        this.getContentResolver().bulkInsert(EventProvider
                .EventsAndRegions.CONTENT_URI, eventsAndRegionContentValues);
    }

    public long addSearchTermToDB(String searchTerm) {
        ContentValues searchCV = new ContentValues();
        searchCV.put(SearchColumns.SEARCH_TERM, searchTerm);
        Uri searchUri = this.getContentResolver().insert(EventProvider.Searches.CONTENT_URI,
                searchCV);

        return getIdFromUri(searchUri);
    }

    public long addRegionToDB(double latitude, double longitude, int within) {
        ContentValues regionCV = new ContentValues();
        regionCV.put(RegionsColumns.LATITUDE, latitude);
        regionCV.put(RegionsColumns.LONGITUDE, longitude);
        regionCV.put(RegionsColumns.RADIUS, within);
        Uri regionUri = this.getContentResolver().insert(EventProvider.Regions.CONTENT_URI,
                regionCV);

        return getIdFromUri(regionUri);
    }

    public static long getIdFromUri(Uri uri) {
        return Long.parseLong(uri.getPathSegments().get(1));
    }
}