package com.example.olivi.maphap;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.olivi.maphap.data.EventProvider;
import com.example.olivi.maphap.data.RegionsColumns;
import com.example.olivi.maphap.sync.MapHapSyncAdapter;
import com.example.olivi.maphap.utils.Constants;
import com.example.olivi.maphap.utils.DateUtils;
import com.example.olivi.maphap.utils.LocationUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends LocationActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        OnMapReadyCallback, EventListFragment.OnListFragmentInteractionListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REGIONS_LOADER = 0;
    private static final int EVENTS_LOADER = 1;

    public static final String REGION_ID_EXTRA = "region_id";

    private static final String LATITUDE_KEY = "latitude_key";
    private static final String LONGITUDE_KEY = "longitude_key";

    private LatLng mLastLocation;
    private boolean mMapReady;
    private boolean mListFragmentReady;
    private GoogleMap mMap;
    private Cursor mDataSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        Log.i(TAG, "initialize SyncAdapter in onCreate");
        MapHapSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    void onUserLocationFound(LatLng latLng) {
        if (checkIfLocationChanged(latLng)) {
            double latitude = latLng.latitude;
            double longitude = latLng.longitude;
            zoomToPosition(latitude, longitude);
            addMarker("you're here", latitude, longitude);
            mLastLocation = latLng;
            Log.i(TAG, "onUserLocationFound called and location changed. Initializing loader");
            getLoaderManager().initLoader(REGIONS_LOADER, null, this);
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

        if (mLastLocation != null) {
            zoomToPosition(mLastLocation.latitude, mLastLocation.longitude);
            addMarker("you're here", mLastLocation.latitude, mLastLocation.longitude);
        }
    }

    private void zoomToPosition(double latitude, double longitude) {
        LatLng position = new LatLng(latitude,
                longitude);
        CameraPosition target = CameraPosition.builder()
                .target(position)
                .zoom(Constants.MAP_ZOOM_LEVEL).build();
        mMap.moveCamera(CameraUpdateFactory
                .newCameraPosition(target));
    }

    private void addMarker(String name, double latitude, double longitude) {
        if (mMapReady) {
            MarkerOptions place = new MarkerOptions()
                    .position(new LatLng(latitude, longitude))
                    .title(name)
                    .icon(BitmapDescriptorFactory
                            .fromResource(R.mipmap.ic_launcher));
            mMap.addMarker(place);
        } else {
            Log.i(TAG, "addMarker called but map is not ready!");
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Uri eventUri) {
        //TODO open new detail fragment/activity showing the event.
        //The detail activity will load the event info unless its a tablet
        //in which case we will load it here.
    }

    @Override
    public void onListFragmentReady(MyEventRecyclerViewAdapter adapter) {
        //TODO The list fragment is ready to receive the cursor of data. Send it to the adapter
        //and keep a handle on the adapter in case the cursor changes or the loader is reset.
        //if the loader has not finished loading data, set a boolean to true so we can add the cursor
        //to the adapter once the load has finished.
        mListFragmentReady = true;
        EventListFragment eventListFragment =
                (EventListFragment) getFragmentManager()
                        .findFragmentById(R.id.eventListFragment);
        eventListFragment.setUpAdapter(mDataSet);
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
                String selection = RegionsColumns.RADIUS + " = ?";
                Uri regionsUri = EventProvider.Regions.CONTENT_URI;

                return new CursorLoader(this,
                        regionsUri,
                        Projections.REGION_COLUMNS,
                        selection,
                        radius,
                        null);
            case EVENTS_LOADER:
                long regionId = args.getLong(REGION_ID_EXTRA);
                Uri eventsUri = EventProvider.Events.withRegionId(regionId);

                return new CursorLoader(this,
                        eventsUri,
                        Projections.EVENT_COLUMNS,
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
                if ((data != null) && (data.moveToFirst())) {
                    Log.i(TAG, "Regions load finished. data is not null and has a row!");
                    long regionId = checkIfRegionIsInDB(data);
                    if (regionId != -1) {
                        Log.i(TAG, "found region: " + regionId + " Starting events loader");
                        //TODO test this data to see if it is older than a day. If so, we should refetch from API
                        Bundle args = new Bundle();
                        args.putLong(REGION_ID_EXTRA, regionId);
                        getLoaderManager().initLoader(EVENTS_LOADER, args, this);
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
                    Log.i(TAG, "Events load finished!");
                    mDataSet = data;
                    addEventsToMap(data);
                    if (mListFragmentReady) {
                        getAdapterFromListFragment().changeCursor(mDataSet);
                    }
                }
                break;
            default:
                break;
        }

    }

    private void fetchEventsData() {

        Log.i(TAG, "FetchEventsData called. Attempting to sync immediately with full bundle");
        Bundle bundle = new Bundle();
        bundle.putDouble(MapHapSyncAdapter.LATITUDE_QUERY_EXTRA, mLastLocation.latitude);
        bundle.putDouble(MapHapSyncAdapter.LONGITUDE_QUERY_EXTRA, mLastLocation.longitude);
        bundle.putInt(MapHapSyncAdapter.WITHIN_QUERY_EXTRA,
                LocationUtils.getPreferredRadius(this));
        MapHapSyncAdapter.syncImmediately(getApplicationContext(), bundle);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case EVENTS_LOADER:
                getAdapterFromListFragment().changeCursor(null);
                mDataSet.close();
                mDataSet = null;
                break;
            default:
                break;
        }

    }

    private MyEventRecyclerViewAdapter getAdapterFromListFragment() {
        EventListFragment eventListFragment =
                (EventListFragment) getFragmentManager()
                        .findFragmentById(R.id.eventListFragment);
        MyEventRecyclerViewAdapter adapter = eventListFragment.getAdapter();
        return adapter;
    }

    public long checkIfRegionIsInDB(Cursor cursor) {
        long regionId = -1;

        double userLat = mLastLocation.latitude;
        double userLon = mLastLocation.longitude;

        for (int i = 0; i < cursor.getCount(); i++) {
            double regionLat = cursor.getDouble(Projections.Regions.COL_LATITUDE);
            double regionLon = cursor.getDouble(Projections.Regions.COL_LONGITUDE);

            double distInMi = LocationUtils.milesBetweenTwoPoints(userLat, userLon,
                    regionLat, regionLon);

            String addedDateString = cursor.getString(Projections.Regions.ADDED_DATE_TIME);

            if ((distInMi <= Constants.TOLERANCE_DIST_IN_MILES) && (DateUtils
                    .isDateTimeStringAfterCutOff(addedDateString))){
                regionId = cursor.getLong(Projections.Regions.COL_ID);

                Log.d(TAG, "region is in DB. ID is " + regionId);
                break;
            }

            cursor.moveToNext();

        }

        return regionId;
    }

    private void addEventsToMap(Cursor data) {

        Log.i(TAG, "adding events to map");
        data.moveToFirst();
        for (int i = 0; i < data.getCount(); i++) {
            double lat = data.getDouble(Projections.Events.COL_VENUE_LAT);
            double lon = data.getDouble(Projections.Events.COL_VENUE_LON);
            String eventName = data.getString(Projections.Events.COL_NAME);

            Log.i(TAG, "adding event: " + eventName + " at " + lat + " " + lon);
            addMarker(eventName, lat, lon);

            data.moveToNext();
        }
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mLastLocation != null) {
            outState.putDouble(LATITUDE_KEY, mLastLocation.latitude);
            outState.putDouble(LONGITUDE_KEY, mLastLocation.longitude);
        }
        super.onSaveInstanceState(outState);
    }
    //TODO add a task to periodically check database for old data and delete it, so we don't rack up endless history
    //TODO cont. data should be deleted from the regions table based on how old it is. Then cascade to the region_events
    //TODO cont. table and events and venues unless there is another search region that is not too old that
    //TODO cont. matches that same region. I think a simple join across all these tables will do, deleting the rows
    //TODO cont. with the matching region ID of the old data.
}
