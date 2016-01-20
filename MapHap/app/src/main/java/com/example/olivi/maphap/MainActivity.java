package com.example.olivi.maphap;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.olivi.maphap.data.EventProvider;
import com.example.olivi.maphap.data.EventsColumns;
import com.example.olivi.maphap.data.RegionsColumns;
import com.example.olivi.maphap.preferences.NumberPickerPreference;
import com.example.olivi.maphap.service.MapHapService;
import com.example.olivi.maphap.utils.Constants;
import com.example.olivi.maphap.utils.DateUtils;
import com.example.olivi.maphap.utils.LocationUtils;
import com.example.olivi.maphap.utils.Utility;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends LocationActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        OnMapReadyCallback, EventListFragment.OnListFragmentInteractionListener,
        SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REGIONS_LOADER = 0;
    private static final int CATEGORIES_LOADER = 1;
    private static final int EVENTS_LOADER = 2;
    private static final int EVENT_LOADER = 3;


    private static final int REQUEST_FILTER = 0;

    public static final String REGION_ID_EXTRA = "region_id";
    public static final String EVENT_URI_EXTRA = "event_uri";
    public static final String CATEGORY_IDS_EXTRA = "category_ids";

    private static final String LATITUDE_KEY = "latitude_key";
    private static final String LONGITUDE_KEY = "longitude_key";

    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    private LatLng mLastLocation;
    private boolean mMapReady;
    private boolean mListFragmentReady;
    private GoogleMap mMap;
    private Cursor mDataSet;
    private EventRecyclerViewAdapter mAdapter;
    private boolean mThreePane;
    private boolean mCategoriesChanged;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (findViewById(R.id.event_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp).
            mThreePane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.event_detail_container, new DetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mThreePane = false;
        }

        ((FloatingActionButton)findViewById(R.id.fab)).setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                startFilterActivity();
            }
        });
        Log.i(TAG, "onCreate called and savedInstanceState is " + savedInstanceState);
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        if (savedInstanceState != null && savedInstanceState.containsKey(LATITUDE_KEY)) {
            mLastLocation = new LatLng(savedInstanceState.getDouble(LATITUDE_KEY),
                    savedInstanceState.getDouble(LONGITUDE_KEY));
        }

        if (mLastLocation != null) {
            Log.i(TAG, "in onCreate. Regions loader restarted to query for regions");
            getLoaderManager().restartLoader(REGIONS_LOADER, null, this);
        }


        Log.i(TAG, "in onCreate. Registering the onSharedPreferenceChangeListener");
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
    }

    @Override
    void onUserLocationFoundOrChanged(LatLng latLng) {
        mLastLocation = latLng;
        double latitude = latLng.latitude;
        double longitude = latLng.longitude;
        zoomToPosition(latitude, longitude, LocationUtils.getMapZoomLevel(this));
        addYouAreHereMarker();
        getLoaderManager().initLoader(REGIONS_LOADER, null, this);
    }

    private void clearMap() {
        if (mMapReady) {
            Log.i(TAG, "clearing map");
            mMap.clear();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMapReady = true;
        mMap = googleMap;

        Log.i(TAG, "map ready!!");
        if (mLastLocation != null) {
            zoomToPosition(mLastLocation.latitude, mLastLocation.longitude,
                    LocationUtils.getMapZoomLevel(this));
            addYouAreHereMarker();
            Log.i(TAG, "zooming to and adding marker to user's current location: " +
                    mLastLocation.latitude + ", " + mLastLocation.longitude);
        }
    }

    private void zoomToPosition(double latitude, double longitude, float zoomLevel) {
        if (mMapReady) {
            Log.i(TAG, "zoomToPosition called. Zoom level is " + zoomLevel);
            LatLng position = new LatLng(latitude,
                    longitude);
            CameraPosition target = CameraPosition.builder()
                    .target(position)
                    .zoom(zoomLevel).build();
            mMap.moveCamera(CameraUpdateFactory
                    .newCameraPosition(target));
        }
    }


    private void addMarker(String name, BitmapDescriptor icon, double latitude, double longitude) {
        if (mMapReady) {
            MarkerOptions place = new MarkerOptions()
                    .position(new LatLng(latitude, longitude))
                    .title(name)
                    .icon(icon);
            mMap.addMarker(place);

        } else {
            Log.i(TAG, "addMarker called but map is not ready!");
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        switch (key) {

            case "category_key":
                Log.i(TAG, "onSharedPreferenchChanged called and key is category_key. Setting " +
                        "mCategoriesChanged to true");
                getLoaderManager().restartLoader(CATEGORIES_LOADER, null, this);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult called");
        switch (requestCode) {
            case REQUEST_FILTER:
                if (resultCode == RESULT_OK) {
                    boolean radiusChanged = data.getBooleanExtra(FilterActivity
                            .PREF_RADIUS_CHANGED_EXTRA, false);
                    boolean categoryChanged = data.getBooleanExtra(FilterActivity
                            .PREF_CATEGORY_CHANGED_EXTRA, false);
                    boolean startDateChanged = data.getBooleanExtra(FilterActivity
                            .PREF_START_DATE_CHANGED_EXTRA, false);
                    boolean endDateChanged = data.getBooleanExtra(FilterActivity
                            .PREF_END_DATE_CHANGED_EXTRA, false);

                    if (radiusChanged) {
                        int radius = PreferenceManager.getDefaultSharedPreferences(this).getInt
                                (Constants.PREF_RADIUS_KEY, NumberPickerPreference.DEFAULT_VALUE);

                        String dist = (radius > 1 ? radius + " miles." : radius + " mile.");
                        Toast toast = Toast.makeText(this, getString(R.string
                                        .toast_search_radius, dist),
                                Toast.LENGTH_LONG);
                        toast.show();

                        zoomToPosition(mLastLocation.latitude, mLastLocation.longitude,
                                LocationUtils
                                        .getMapZoomLevel(this));
                        getLoaderManager().restartLoader(REGIONS_LOADER, null, this);
                    } else if (categoryChanged) {
                        Log.i(TAG, "onActivityResult called. Category changed. Restarting " +
                                "categories loader");

                        getLoaderManager().restartLoader(CATEGORIES_LOADER, null, this);
                    } else if ((startDateChanged) || (endDateChanged)) {
                        Log.i(TAG, "onActivityResult called. Start/end date changed. Restarting " +
                                "events loader");

                        getLoaderManager().restartLoader(EVENTS_LOADER, null, this);
                    }

                }
                break;
        }
    }


    public void startFilterActivity() {
        Intent i = new Intent(this, FilterActivity.class);
        startActivityForResult(i, REQUEST_FILTER);
    }

    @Override
    public void onItemSelected(Uri eventUri, int position) {
        if (mThreePane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, eventUri);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getFragmentManager().beginTransaction()
                    .replace(R.id.event_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(eventUri);
            startActivity(intent);
        }
    }

    @Override
    public void onListFragmentReady(EventRecyclerViewAdapter adapter) {
        mListFragmentReady = true;
        EventListFragment eventListFragment =
                (EventListFragment) getFragmentManager()
                        .findFragmentById(R.id.eventListFragment);
        mAdapter = eventListFragment.setUpAdapter(mDataSet);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case REGIONS_LOADER:
                String[] radius = {Integer.toString(LocationUtils.getPreferredRadius(this))};
                Log.i(TAG, "creating regions loader. Radius found from utils is " + radius[0]);
                String selection = RegionsColumns.RADIUS + " = ?";
                Uri regionsUri = EventProvider.Regions.CONTENT_URI;

                return new CursorLoader(this,
                        regionsUri,
                        Projections.REGION_COLUMNS,
                        selection,
                        radius,
                        null);

            case CATEGORIES_LOADER:
                long regionId = PreferenceManager.getDefaultSharedPreferences(this)
                        .getLong(getString(R.string.pref_region_id_key), -1);
                Log.i(TAG, "onCreateLoader called for CATEGORIES_LOADER. Found regiond ID: " +
                        regionId);
                Uri categoriesUri = EventProvider.CategoriesAndRegions.withRegionId(regionId);

                return new CursorLoader(this,
                        categoriesUri,
                        Projections.CATEGORIES_COLUMNS,
                        null,
                        null,
                        null);

            case EVENTS_LOADER:
                long regionIdForEvents = PreferenceManager.getDefaultSharedPreferences(this)
                        .getLong(getString(R.string.pref_region_id_key), -1);
                Log.i(TAG, "onCreateLoader called for EVENTS_LOADER. Found regiond ID: " +
                        regionIdForEvents);
                Uri eventsUri = EventProvider.Events.withRegionId(regionIdForEvents);

                Set<String> categories = Utility.getPreferredCategories(this);
                Log.i(TAG, "creating events loader. Size of preferred categories is " + categories);

                StringBuilder sb = new StringBuilder();
                sb.append("(");
                for (String category : categories) {
                    sb.append(category).append(",");
                }
                //Delete the last comma
                sb.deleteCharAt(sb.length() - 1);
                sb.append(")");

                long startMillis = Utility.getPreferredMillis(this, getString(R.string
                        .pref_start_date_key), -1);

                long endMillis = Utility.getPreferredMillis(this, getString(R.string
                        .pref_end_date_key), -1);

                String dateSelection ="";

                if (startMillis != -1) {
                    dateSelection = "(" + EventsColumns.START_DATE_TIME + " >= "+ Long.toString
                            (startMillis) + ") AND (" +
                            EventsColumns.START_DATE_TIME + " <= " + Long.toString(endMillis) + ")";
                }

                String eventSelection = EventsColumns.CATEGORY + " in ";

                if (dateSelection.length() > 0) {
                    eventSelection = dateSelection + " AND (" + eventSelection + sb + ")";
                    Log.i(TAG, "creating events loader. Selection is " + eventSelection);
                } else {
                    eventSelection = eventSelection + sb;
                    Log.i(TAG, "creating events loader. Selection is " + eventSelection);
                }

                return new CursorLoader(this,
                        eventsUri,
                        Projections.EVENT_COLUMNS_LIST_VIEW,
                        eventSelection,
                        null,
                        null);
            case EVENT_LOADER:
                Uri eventUri = args.getParcelable(EVENT_URI_EXTRA);

                return new CursorLoader(this,
                        eventUri,
                        Projections.EVENT_COLUMNS_LIST_VIEW,
                        null,
                        null,
                        null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();
        switch (id) {
            case REGIONS_LOADER:
                Log.i(TAG, "Regions load finished. Data is null " + (data == null));

                if ((data != null) && (data.moveToFirst())) {
                    Log.i(TAG, "Regions load finished. data is not null and has a row!");
                    long regionId = checkIfValidRegionExists(data);
                    if ((regionId != -1) && (isDataCurrent(data))) {
                        Log.i(TAG, "found region: " + regionId + " Starting events loader");
                        LocationUtils.saveRegionIdToSharedPref(this, regionId);
                        Bundle args = new Bundle();
                        args.putLong(REGION_ID_EXTRA, regionId);
                        getLoaderManager().restartLoader(CATEGORIES_LOADER, args, this);
                    } else {
                        Log.i(TAG, "could not find current region in database. fetching data now");
                        fetchEventsData(null, -1);
                    }
                } else {
                    Log.i(TAG, "database is empty. fetching data now");
                    fetchEventsData(null, -1);
                }
                break;
            case CATEGORIES_LOADER:
                Log.i(TAG, "categories load finished. Data is null " + (data == null));

                long regionId = LocationUtils.getPreferredRegionId(this);

                if ((data != null) && (data.moveToFirst())) {
                    Log.i(TAG, "categories load finished. data is not null and has a row!");
                    String[] categoryIds = checkCategories(data);
                    if (categoryIds.length == 0) {
                        Log.i(TAG, "All categories found in DB: " + categoryIds + " Starting " +
                                "events loader");

                        getLoaderManager().restartLoader(EVENTS_LOADER, null, this);
                    } else {
                        Log.i(TAG, "could not find some categories in db. fetching data now");
                        fetchEventsData(categoryIds, regionId);
                    }
                } else {
                    Log.i(TAG, "database is empty. fetching data now");
                    fetchEventsData(null, regionId);
                }
                break;
            case EVENTS_LOADER:
                if ((data != null) && (data.moveToFirst())) {
                    Log.i(TAG, "Events load finished and cursor contains " + data.getCount() + " " +
                            "rows");
                    if (mDataSet != null) {
                        Cursor old = mDataSet;
                        old.close();
                    }

                    mDataSet = data;

                    addEventsToMap(data);


                    Log.i(TAG, "EVENTS_LOADER finished. Swapping out data set.");
                    mAdapter.changeCursor(mDataSet);

                    if (mListFragmentReady) {
                        Log.i(TAG, "onLoadFinished called and mListFragmentReady is true." +
                                "get event list fragment so we can scroll to previous position");
                        // If we don't need to restart the loader, and there's a desired position to restore
                        // to, do so now.
                        EventListFragment fragment = (EventListFragment)getFragmentManager()
                                .findFragmentById(R.id.eventListFragment);
                        if (fragment != null) {
                            fragment.scrollToPreviousPosition();
                        } else {
                            Log.i(TAG, "onLoadFinished and event list fragment is nulll!");
                        }
                    }
                }
                break;
            case EVENT_LOADER:
                if ((data != null) && (data.moveToFirst())) {
                    double latitude = data.getDouble(Projections.EventsListView
                            .COL_VENUE_LAT);
                    double longitude = data.getDouble(Projections.EventsListView
                            .COL_VENUE_LON);
                    Log.i(TAG, "zooming to position " + latitude + ", " + longitude);
                    zoomToPosition(latitude, longitude, Constants.MAP_ZOOM_LEVEL_CLOSE);
                }
                break;
            default:
                break;
        }
    }

    private boolean isDataCurrent(Cursor data) {
        double dateAdded = data.getDouble(Projections.Regions.ADDED_DATE_TIME);
        return DateUtils.isDateTimeAfterCutOff(dateAdded);
    }

    private void fetchEventsData(String[] categoryIds, long regionId) {

        Log.i(TAG, "FetchEventsData called. Attempting to start service with intent");
        Intent intent = new Intent(this, MapHapService.class);
        if (categoryIds != null) {
            intent.putExtra(CATEGORY_IDS_EXTRA, categoryIds);
        }

        if (regionId != -1) {
            intent.putExtra(REGION_ID_EXTRA, regionId);
        }

        startService(intent);
    }

    private String[] checkCategories(Cursor data) {
        data.moveToFirst();
        HashSet<String> set = Utility.getPreferredCategories(this);
        HashSet<String> cloneSet = (HashSet<String>)set.clone();

        for (String cat : cloneSet) {
            Log.i(TAG, "in checkCategories. Preferred category: " + cat);
        }

        for (int i = 0; i < data.getCount(); i++) {
            String catID = data.getString(Projections.Categoires.COL_CATEGORY_ID);
            Log.i(TAG, "from cursor, category id is " + catID);

            Log.i(TAG, "in checkCategories. Removing category ID " + catID);
            cloneSet.remove(catID);
            data.moveToNext();
        }
        String[] categories = new String[cloneSet.size()];

        cloneSet.toArray(categories);
        Log.i(TAG, "in checkCategories. Catgories we need to fetch = " + categories);

        return categories;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case EVENTS_LOADER:
                Log.i(TAG, "events loader reset");
                mAdapter.changeCursor(null);
                mDataSet.close();
                mDataSet = null;
                break;
            default:
                break;
        }

    }

    public long checkIfValidRegionExists(Cursor cursor) {
        long regionId = -1;

        double userLat = mLastLocation.latitude;
        double userLon = mLastLocation.longitude;

        for (int i = 0; i < cursor.getCount(); i++, cursor.moveToNext()) {
            double regionLat = cursor.getDouble(Projections.Regions.COL_LATITUDE);
            double regionLon = cursor.getDouble(Projections.Regions.COL_LONGITUDE);

            double distInMi = LocationUtils.milesBetweenTwoPoints(userLat, userLon,
                    regionLat, regionLon);

            double addedDate = cursor.getDouble(Projections.Regions.ADDED_DATE_TIME);

            if ((distInMi <= Constants.TOLERANCE_DIST_IN_MILES) && (DateUtils
                    .isDateTimeAfterCutOff(addedDate))){

                regionId = cursor.getLong(Projections.Regions.COL_ID);

                Log.i(TAG, "region is in DB. ID is " + regionId);
                break;
            }
        }

        return regionId;
    }

    private void addEventsToMap(Cursor data) {
        Log.i(TAG, "in addEventsToMap. Clearing map");
        Log.i(TAG, "in addEventsToMap. mMapReady is " + mMapReady);

        clearMap();

        addYouAreHereMarker();
        Log.i(TAG, "adding events to map");
        data.moveToFirst();
        for (int i = 0; i < data.getCount(); i++) {
            double lat = data.getDouble(Projections.EventsListView.COL_VENUE_LAT);
            double lon = data.getDouble(Projections.EventsListView.COL_VENUE_LON);

            String eventName = data.getString(Projections.EventsListView.COL_NAME);

            addMarker(eventName, null, lat, lon);

            data.moveToNext();
        }
    }

    private void addYouAreHereMarker() {
        addMarker("You're here.", BitmapDescriptorFactory
                .fromResource(R.mipmap.place_icon), mLastLocation.latitude, mLastLocation
                .longitude);
    }

    @Override
    public void onItemLongClicked(Uri eventUri, int position) {
        Bundle args = new Bundle();
        args.putParcelable(EVENT_URI_EXTRA, eventUri);
        Log.i(TAG, "restarting loader with eventUri " + eventUri);
        getLoaderManager().restartLoader(EVENT_LOADER, args, this);
    }

    @Override
    protected void onStart() {
        Log.i(TAG, "onStart called");
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop called");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy called");
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "onSaveInstanceState called");
        super.onSaveInstanceState(outState);
        if (mLastLocation != null) {
            outState.putDouble(LATITUDE_KEY, mLastLocation.latitude);
            outState.putDouble(LONGITUDE_KEY, mLastLocation.longitude);
        }
    }
}
