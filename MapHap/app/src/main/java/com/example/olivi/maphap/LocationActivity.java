package com.example.olivi.maphap;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.example.olivi.maphap.utils.Constants;
import com.example.olivi.maphap.utils.LocationUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

public abstract class LocationActivity extends AppCompatActivity
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = LocationActivity.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 0;


    private static final String ABOUT_FRAGMENT_TAG = "about_fragment";
    private static final String ABOUT_BACKSTACK = "about_back";

    private GoogleApiClient mGoogleApiClient;
    private LatLng mLastLocation;
    private boolean mAskPermissionForLocation;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_location);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        LatLng prefLatLng = new LatLng(LocationUtils.getPreferredLatitude(this), LocationUtils
                .getPreferredLongitude(this));

        if (prefLatLng.latitude != 0) {
            mLastLocation = prefLatLng;
            onUserLocationFoundOrChanged(mLastLocation);
        }
    }

    abstract void onUserLocationFoundOrChanged(LatLng latLng);

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart() called. Attempting to connect GoogleApiClient...");
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            LatLng newLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            if (checkIfLocationChanged(newLatLng)) {
                mLastLocation = new LatLng(location.getLatitude(), location.getLongitude());
                LocationUtils.saveLocationToSharedPref(this, mLastLocation.latitude,
                        mLastLocation.longitude);
                onUserLocationFoundOrChanged(mLastLocation);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended!");
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (isLocationPermissionGranted()) {
            Log.i(TAG, "mGoogleApiClient is connected and permission granted! Getting user location");
            requestLocationUpdates();
            LatLng lastLocation = getUserLocation();
            if (checkIfLocationChanged(lastLocation)) {
                mLastLocation = lastLocation;
                LocationUtils.saveLocationToSharedPref(this, mLastLocation.latitude,
                        mLastLocation.longitude);
                onUserLocationFoundOrChanged(mLastLocation);
            }
        } else {
            mAskPermissionForLocation = true;
            askPermissionForLocation(false);
        }
    }

    private void requestLocationUpdates() {
        createLocationRequest();
        startLocationUpdates();
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (mAskPermissionForLocation) {
            askPermissionForLocation(false);
        }
    }

    private LatLng getUserLocation() {
        Location newLoc = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        return new LatLng(newLoc.getLatitude(), newLoc.getLongitude());

    }

    private boolean isLocationPermissionGranted() {
        boolean isLocationPermissionGranted = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);

        Log.i(TAG, "isLocationPermissionGranted called and returns " + isLocationPermissionGranted);

        return isLocationPermissionGranted;
    }

    private void askPermissionForLocation(boolean rationaleShown) {

        mAskPermissionForLocation = false;


        if ((!rationaleShown) && (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_CONTACTS))) {
            showRequestPermissionRationale();

        } else {

            // No explanation needed, we can request the permission.
            Log.i(TAG, "trying to show request dialog....");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if ((mLastLocation == null) && (mGoogleApiClient.isConnected())) {
                        Log.i(TAG, "Location permission granted. Calling getUserLocation()" +
                                "and onUserLocationFoundOrChanged");
                        mLastLocation = getUserLocation();
                        LocationUtils.saveLocationToSharedPref(this, mLastLocation.latitude,
                                mLastLocation.longitude);
                        onUserLocationFoundOrChanged(mLastLocation);
                    }
                } else if (grantResults.length == 0) {
                    Log.i(TAG, "Request was canceled! Can't access user's location");
                } else {
                    Log.i(TAG, "Permission was denied! Can't access user's location");
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed!");
    }

    private void showRequestPermissionRationale() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                askPermissionForLocation(true);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //TODO: permission has been denied again. Implement functionality allowing user
                //to enter a location of their choosing.
            }
        });
        builder.setMessage(getString(R.string.dialog_location_request_rationale));

        AlertDialog dialog = builder.create();
        dialog.show();
    }



    private boolean checkIfLocationChanged(LatLng newLatLng) {
        if (newLatLng == null) throw new IllegalArgumentException("Parameter location must not be " +
                "null");

        if (mLastLocation != null) {
            double dist = LocationUtils.milesBetweenTwoPoints(newLatLng.latitude,
                    newLatLng.longitude, mLastLocation.latitude,
                    mLastLocation.longitude);
            return (dist > Constants.TOLERANCE_DIST_IN_MILES);
        } else {
            return true;
        }
    }

}
