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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.olivi.maphap.data.EventProvider;
import com.example.olivi.maphap.data.EventsColumns;
import com.example.olivi.maphap.data.RegionsColumns;
import com.example.olivi.maphap.preferences.NumberPickerPreference;
import com.example.olivi.maphap.preferences.SettingsActivity;
import com.example.olivi.maphap.service.MapHapService;
import com.example.olivi.maphap.utils.Constants;
import com.example.olivi.maphap.utils.DateUtils;
import com.example.olivi.maphap.utils.LocationUtils;
import com.example.olivi.maphap.utils.Utility;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Set;

public class MainActivity extends LocationActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        OnMapReadyCallback, EventListFragment.OnListFragmentInteractionListener,
        SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REGIONS_LOADER = 0;
    private static final int EVENTS_LOADER = 1;
    private static final int EVENT_LOADER = 2;


    private static final int REQUEST_FILTER = 0;

    public static final String REGION_ID_EXTRA = "region_id";
    public static final String EVENT_URI_EXTRA = "event_uri";

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
            Log.i(TAG, "in onCreate. Loader restarted to query for regions");
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
    void onUserLocationFound(LatLng latLng) {
        if (checkIfLocationChanged(latLng)) {
            double latitude = latLng.latitude;
            double longitude = latLng.longitude;
            zoomToPosition(latitude, longitude, LocationUtils.getMapZoomLevel(this));
            addMarker("you're here", latitude, longitude);
            mLastLocation = latLng;
            Log.i(TAG, "onUserLocationFound called and location changed. Initializing loader");
            getLoaderManager().initLoader(REGIONS_LOADER, null, this);

            LocationUtils.saveLocationToSharedPref(this, latitude, longitude);
        }
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
            addMarker("you're here", mLastLocation.latitude, mLastLocation.longitude);
        }
    }

    private void zoomToPosition(double latitude, double longitude, float zoomLevel) {
        Log.i(TAG, "zoomToPosition called. Zoom level is " + zoomLevel);
        LatLng position = new LatLng(latitude,
                longitude);
        CameraPosition target = CameraPosition.builder()
                .target(position)
                .zoom(zoomLevel).build();
        mMap.moveCamera(CameraUpdateFactory
                .newCameraPosition(target));
    }

    public void onSearchRegionChanged(LatLng newLocation) {
        mLastLocation = newLocation;
        getLoaderManager().restartLoader(REGIONS_LOADER, null, this);

    }

    private void addMarker(String name, double latitude, double longitude) {
        if (mMapReady) {

            Log.i(TAG, "adding new place to map " + latitude + " " + longitude);
            MarkerOptions place = new MarkerOptions()
                    .position(new LatLng(latitude, longitude))
                    .title(name);
            mMap.addMarker(place);

        } else {
            Log.i(TAG, "addMarker called but map is not ready!");
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        switch (key) {
            case "radius_key":
                Log.i(TAG, "onSharedPreferenceChanged and the key is radius_key. Restarting " +
                        "REGIONS_LOADER");
                int radius = sharedPreferences.getInt(key, NumberPickerPreference.DEFAULT_VALUE);
                String dist = (radius > 1 ? radius + " miles." : radius + " mile.");
                Toast toast = Toast.makeText(this, getString(R.string.toast_search_radius, dist),
                        Toast.LENGTH_LONG);
                toast.show();

                zoomToPosition(mLastLocation.latitude, mLastLocation.longitude, LocationUtils
                        .getMapZoomLevel(this));
                getLoaderManager().restartLoader(REGIONS_LOADER, null, this);
                break;
            case "category_key":
                Log.i(TAG, "onSharedPreferenceChanged and the key is category_key");
                getLoaderManager().restartLoader(EVENTS_LOADER, null, this);

                break;
            case "start_date_key":
                Log.i(TAG, "onSharedPreferenceChanged and the key is start_date_key. Restarting " +
                        "events loader");
                getLoaderManager().restartLoader(EVENTS_LOADER, null, this);
            case "end_date_key":
                Log.i(TAG, "onSharedPreferenceChanged and the key is end_date_key. Restarting " +
                        "events loader");
                getLoaderManager().restartLoader(EVENTS_LOADER, null, this);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
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
        //TODO The list fragment is ready to receive the cursor of data. Send it to the adapter
        //and keep a handle on the adapter in case the cursor changes or the loader is reset.
        //if the loader has not finished loading data, set a boolean to true so we can add the cursor
        //to the adapter once the load has finished.
        mListFragmentReady = true;
        EventListFragment eventListFragment =
                (EventListFragment) getFragmentManager()
                        .findFragmentById(R.id.eventListFragment);
        mAdapter = eventListFragment.setUpAdapter(mDataSet);
    }

    private boolean checkIfLocationChanged(LatLng newLatLng) {
        if ((newLatLng != null) && (mLastLocation != null)) {
            double dist = LocationUtils.milesBetweenTwoPoints(newLatLng.latitude,
                    newLatLng.longitude, mLastLocation.latitude,
                    mLastLocation.longitude);
            if (dist > Constants.TOLERANCE_DIST_IN_MILES) {
                Log.d(TAG, "Dist is greater than tolerance. Returning true");
                mLastLocation = newLatLng;
                return true;
            } else {
                Log.i(TAG, "Dist is less than tolerance. No need to reload data Return false");
                return false;
            }
        } else if (newLatLng != null) {
            Log.d(TAG, "checkIfLocationChanged called and mLastLocation is null. Return true");
            mLastLocation = newLatLng;
            return true;
        }

        Log.d(TAG, "checkIfLocationChanged called but newLatLng is null! Returning false");
        return false;
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
            case EVENTS_LOADER:
                long regionId = PreferenceManager.getDefaultSharedPreferences(this)
                        .getLong(getString(R.string.pref_region_id_key), -1);
                Log.i(TAG, "onCreateLoader called for EVENTS_LOADER. Found regiond ID: " +
                        regionId);
                Uri eventsUri = EventProvider.Events.withRegionId(regionId);

                Set<String> categories = Utility.getPreferredCategories(this);

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
                } else {
                    eventSelection = eventSelection + sb;
                }

                Log.i(TAG, "creating events loader. Selection is " +eventSelection + sb.toString());

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
                        getLoaderManager().restartLoader(EVENTS_LOADER, args, this);

                    } else {
                        Log.i(TAG, "could not find current region in database. fetching data now");
                        fetchEventsData();
                    }
                } else {
                    Log.i(TAG, "database is empty. fetching data now");
                    fetchEventsData();
                }
                break;
            case EVENTS_LOADER:
                if ((data != null) && (data.moveToFirst())) {
                    Log.i(TAG, "EventsListView load finished!");
                    addEventsToMap(data);

                    mDataSet = data;

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

    private void fetchEventsData() {

        Log.i(TAG, "FetchEventsData called. Attempting to start service with intent");
        Intent intent = new Intent(this, MapHapService.class);

        startService(intent);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case EVENTS_LOADER:
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

                Log.d(TAG, "region is in DB. ID is " + regionId);
                break;
            }
        }

        return regionId;
    }

    private void addEventsToMap(Cursor data) {
        Log.i(TAG, "in addEventsToMap. Clearing map");
        Log.i(TAG, "in addEventsToMap. mMapReady is " + mMapReady);

        clearMap();

        Log.i(TAG, "adding events to map");
        data.moveToFirst();
        for (int i = 0; i < data.getCount(); i++) {
            double lat = data.getDouble(Projections.EventsListView.COL_VENUE_LAT);
            double lon = data.getDouble(Projections.EventsListView.COL_VENUE_LON);
            Log.i(TAG, "adding venue to map "
                    + data.getString(Projections.EventsListView.COL_VENUE_NAME));
            String eventName = data.getString(Projections.EventsListView.COL_NAME);


            addMarker(eventName, lat, lon);

            data.moveToNext();
        }
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
    //TODO add a task to periodically check database for old data and delete it, so we don't rack up endless history
    //TODO cont. data should be deleted from the regions table based on how old it is. Then cascade to the region_events
    //TODO cont. table and events and venues unless there is another search region that is not too old that
    //TODO cont. matches that same region. I think a simple join across all these tables will do, deleting the rows
    //TODO cont. with the matching region ID of the old data.
}
