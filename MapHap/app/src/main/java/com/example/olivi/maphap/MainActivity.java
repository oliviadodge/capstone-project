package com.example.olivi.maphap;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.olivi.maphap.data.EventProvider;
import com.example.olivi.maphap.data.RegionsColumns;
import com.example.olivi.maphap.service.MapHapService;
import com.example.olivi.maphap.utils.Constants;
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
        OnMapReadyCallback{

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REGIONS_LOADER = 0;



    private Location mLastLocation;
    private boolean mMapReady;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
    }

    @Override
    void onUserLocationFound(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        zoomToPosition(latitude, longitude);
        addMarker(latitude, longitude);
        mLastLocation = location;


        getLoaderManager().initLoader(REGIONS_LOADER, null, this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMapReady = true;
        mMap = googleMap;

        if (mLastLocation != null) {
            zoomToPosition(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        } else {
            zoomToPosition(37.808196, -122.286276);
        }
    }

    private void zoomToPosition(double latitude, double longitude) {
        LatLng position = new LatLng(latitude,
                longitude);
        CameraPosition target = CameraPosition.builder()
                .target(position)
                .zoom(14).build();
        mMap.moveCamera(CameraUpdateFactory
                .newCameraPosition(target));
    }

    private void addMarker(double latitude, double longitude) {
        if (mMapReady) {
            MarkerOptions place = new MarkerOptions()
                    .position(new LatLng(latitude, longitude))
                    .title("place")
                    .icon(BitmapDescriptorFactory
                            .fromResource(R.mipmap.ic_launcher));
            mMap.addMarker(place);
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
        } else if (id == R.id.refresh) {
            String expansions = "logo,venue,category";
            Intent i = new Intent(this, MapHapService.class);
            i.putExtra(MapHapService.SEARCH_QUERY_EXTRA, "music");
            i.putExtra(MapHapService.LATITUDE_QUERY_EXTRA, mLastLocation.getLatitude());
            i.putExtra(MapHapService.LONGITUDE_QUERY_EXTRA, mLastLocation.getLongitude());
            i.putExtra(MapHapService.WITHIN_QUERY_EXTRA, 30);
            i.putExtra(MapHapService.EXPANSIONS_QUERY_EXTRA, expansions);

            Log.i("MainActivity", "refresh button hit. Starting service...");
            startService(i);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case REGIONS_LOADER:

                String[] radius = {LocationUtils.getPreferredRadius(this)};
                String selection = RegionsColumns.RADIUS + " = ?";
                Uri regionsUri = EventProvider.Regions.CONTENT_URI;

                //TODO check for how old data is. If it's older than a day we should probably refetch

                return new CursorLoader(this,
                        regionsUri,
                        RegionProjections.REGION_COLUMNS,
                        selection,
                        radius,
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
                    long regionId = checkIfRegionIsInDB(data);
                    if (regionId != -1) {
                        //TODO start loader to query for events matching the region id found in the db
                    }
                } else {
                    //TODO start MapHapService to fetch events data since region is not in DB
                }
            default:
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public long checkIfRegionIsInDB(Cursor cursor) {
        long regionId = -1;

        double userLat = mLastLocation.getLatitude();
        double userLon = mLastLocation.getLongitude();

        for (int i = 0; i < cursor.getCount(); i++) {
            double regionLat = cursor.getDouble(RegionProjections.COL_LATITUDE);
            double regionLon = cursor.getDouble(RegionProjections.COL_LONGITUDE);

            double distInMi = LocationUtils.milesBetweenTwoPoints(userLat, userLon,
                    regionLat, regionLon);

            if (distInMi <= Constants.TOLERANCE_DIST_IN_MILES) {
                regionId = cursor.getLong(RegionProjections.COL_ID);
                break;
            }

        }

        return regionId;
    }
}
