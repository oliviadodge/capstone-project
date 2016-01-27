package com.example.olivi.maphap.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.olivi.maphap.MainActivity;
import com.example.olivi.maphap.R;
import com.example.olivi.maphap.data.CategoriesAndRegionsColumns;
import com.example.olivi.maphap.data.EventProvider;
import com.example.olivi.maphap.data.RegionsColumns;
import com.example.olivi.maphap.utils.DateUtils;
import com.example.olivi.maphap.utils.LocationUtils;

import org.json.JSONException;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by olivi on 11/18/2015.
 */

public class MapHapService extends IntentService {
    public static final String ACTION_DATA_UPDATED =
            "com.example.olivi.maphap.ACTION_DATA_UPDATED";

    //These are the data types for content values we want to get from the EventsNetworker response
    public static final int VENUES = 0;
    public static final int EVENTS = 1;
    public static final int EVENTS_REGIONS = 2;
    //The order of this array is important for adding data to DB.
    //(ie. Can't add events until you add venues and events_regions until you add events)
    public static final int[] REQUIRED_CONTENT_VALUES = {
            VENUES, EVENTS, EVENTS_REGIONS
    };

    //The order of this array is important for deleting data from DB.
    //(ie. Can't delete events_regions until you delete regions and events)
    public static final Uri[] URIS_TO_DELETE_FROM = {
            EventProvider.Regions.CONTENT_URI,
            EventProvider.Events.CONTENT_URI,
            EventProvider.EventsAndRegions.CONTENT_URI,
            EventProvider.CategoriesAndRegions.CONTENT_URI
    };

    private static final String LOG_TAG = MapHapService.class.getSimpleName();

    public MapHapService() {
        super("MapHap");
    }

    private double mJulianDateAdded;
    private double mLatitude;
    private double mLongitude;
    private int mRadius;
    private long mRegionId;
    private Set<String> mCategories;

    @Override
    protected void onHandleIntent(Intent intent) {
        mLatitude = LocationUtils.getPreferredLatitude(this);
        mLongitude = LocationUtils.getPreferredLongitude(this);
        mRadius = LocationUtils.getPreferredRadius(this);
        mRegionId = intent.getLongExtra(MainActivity.REGION_ID_EXTRA, -1);

        //Get categories extra. These are the categories from SharedPreferences that we have not
        //yet downloaded from the API
        if (intent.hasExtra(MainActivity.CATEGORY_IDS_EXTRA)) {
            mCategories = new HashSet<String>(Arrays.asList(intent.getStringArrayExtra
                    (MainActivity.CATEGORY_IDS_EXTRA)));
        }

        String[] defaultArray = getResources()
                .getStringArray(R.array.defaultValues_category_preference);

        //If no categories were given, we will download data for all the categories stored in
        // SharedPreferences.
        if (mCategories == null) {
            Log.i(LOG_TAG, "No categories were given. Getting categories from defaultSharedPrefs");
            mCategories = PreferenceManager.getDefaultSharedPreferences(this)
                    .getStringSet(getString(R.string.pref_category_key),
                            new HashSet<String>(Arrays.asList(defaultArray)));
        }

        EventsNetworker.HttpRequest request = EventsNetworker.HttpRequest.newBuilder()
                .friendlyName("events_request")
                .latitude(mLatitude)
                .longitude(mLongitude)
                .categories(mCategories)
                .radius(mRadius)
                .method(EventsNetworker.HttpMethod.GET)
                .authToken(getString(R.string.my_personal_oauth_token))
                .build();
        EventsNetworker.getsInstance(request, getEventsCallbackHandler())
                .execute();

        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
        getApplicationContext().sendBroadcast(dataUpdatedIntent);

    }

    public EventsNetworker.Callback getEventsCallbackHandler() {
        return new EventsNetworker.Callback() {
            @Override
            public void onResponse(EventsNetworker.HttpResponse result) {
                if (result.statusCode == 200) {
                    try {
                        //If the region ID = -1, this means the current location and preferred
                        // radius have not yet been saved to the databaase.
                        if (mRegionId == -1) {
                            mRegionId = addRegionToDB(mLatitude, mLongitude, mRadius);
                        } else {
                            Log.i(LOG_TAG, "Region had already been added to DB. Add categories " +
                                    "next");
                        }

                        int added = addCategoriesToDB(mRegionId, mCategories);
                        Log.i(LOG_TAG, "Categories added " + added);
                        //Update shared preferences with the new region ID.

                        EventsDataJsonParser parser = new EventsDataJsonParser(result.body,
                                mRegionId,
                                mJulianDateAdded);
                        parser.parse();

                        for (int i = 0; i < REQUIRED_CONTENT_VALUES.length; i++) {
                            ContentValues[] cv = parser.getContentValues
                                    (REQUIRED_CONTENT_VALUES[i]);
                            addContentValuesToDB(REQUIRED_CONTENT_VALUES[i], cv);
                        }
                        deleteOldData(getApplicationContext());
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "JSON error", e);
                    }
                }
            }

            @Override
            public void onFailure(IOException e) {
                // Show in Stetho :)
            }
        };
    }

    private void addContentValuesToDB(int dataType, ContentValues[] contentValues) {
        Uri contentUri = new Uri.Builder().build();
        switch (dataType) {
            case VENUES:
                contentUri = EventProvider.Venues.CONTENT_URI;
                break;
            case EVENTS:
                contentUri = EventProvider.Events.CONTENT_URI;
                break;
            case EVENTS_REGIONS:
                contentUri = EventProvider.EventsAndRegions.CONTENT_URI;
                break;
        }
        if (contentUri.getPath() == null) {
            throw new IllegalArgumentException("Data type must be one of the three listed in " +
                    "MapHapService.REQUIRED_CONTENT_VALUES");
        } else {
            int added = this.getContentResolver().bulkInsert(contentUri, contentValues);

            Log.i(LOG_TAG, "bulk insert to " + contentUri + ". Added " + added);
        }
    }

    private long addRegionToDB(double latitude, double longitude, int within) {
        double dateAdded = DateUtils.getCurrentJulianDateTime();

        Log.i(LOG_TAG, "Date region added: " + dateAdded);

        ContentValues regionCV = new ContentValues();
        regionCV.put(RegionsColumns.LATITUDE, latitude);
        regionCV.put(RegionsColumns.LONGITUDE, longitude);
        regionCV.put(RegionsColumns.RADIUS, within);
        regionCV.put(RegionsColumns.ADDED_DATE_TIME, dateAdded);
        Uri regionUri = this.getContentResolver().insert(EventProvider.Regions.CONTENT_URI,
                regionCV);

        if (regionUri != null) {
            mJulianDateAdded = dateAdded;
        }

        return getIdFromUri(regionUri);
    }

    private int addCategoriesToDB(long regionId, Set<String> categories) {

        mJulianDateAdded = DateUtils.getCurrentJulianDateTime();

        String[] categoriesArray = new String[categories.size()];
        categories.toArray(categoriesArray);

        ContentValues[] categoriesRegionCV = new ContentValues[categoriesArray.length];

        for (int i = 0; i < categoriesArray.length; i++) {
            ContentValues cv = new ContentValues();
            cv.put(CategoriesAndRegionsColumns.REGION_ID, regionId);
            cv.put(CategoriesAndRegionsColumns.CATEGORY_ID, categoriesArray[i]);
            Log.i(LOG_TAG, "adding " + categoriesArray[i] + " to database");
            cv.put(CategoriesAndRegionsColumns.ADDED_DATE_TIME, mJulianDateAdded);

            categoriesRegionCV[i] = cv;
        }

        return this.getContentResolver().bulkInsert(EventProvider.CategoriesAndRegions
                        .CONTENT_URI,
                categoriesRegionCV);
    }

    private static long getIdFromUri(Uri uri) {
        return Long.parseLong(uri.getPathSegments().get(1));
    }

    private static int[] deleteOldData(Context context) {
        double cutOffJulian = DateUtils.getCutOffJulianDateTime();

        int[] rowsDeleted = new int[URIS_TO_DELETE_FROM.length];

        Log.i(LOG_TAG, "cut off date is " + cutOffJulian);

        for (int i = 0; i < URIS_TO_DELETE_FROM.length; i++) {

            rowsDeleted[i] = context.getContentResolver().delete(URIS_TO_DELETE_FROM[i],
                    RegionsColumns.ADDED_DATE_TIME + " <= ?",
                    new String[]{Double.toString(cutOffJulian)});

            Log.i(LOG_TAG, "Rows deleted from database " + rowsDeleted[i]);

        }

        return rowsDeleted;
    }
}
