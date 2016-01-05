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
package com.example.olivi.maphap;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.olivi.maphap.utils.DateUtils;
import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    static final String DETAIL_URI = "URI";

    private static final String EVENT_SHARE_HASHTAG = " #SunshineApp";

    private ShareActionProvider mShareActionProvider;
    private String mEvent;
    private Uri mUri;

    private static final int DETAIL_LOADER = 0;

    private ImageView mImageView;
    private TextView mNameTextView;
    private TextView mFriendlyDateView;
    private TextView mDateView;
    private TextView mDescriptionView;
    private TextView mCategoryView;
    private TextView mUrlView;
    private TextView mVenueView;
    private TextView mStatusView;
    private TextView mCapacityView;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mImageView = (ImageView) rootView.findViewById(R.id.detail_image);
        mNameTextView = (TextView) rootView.findViewById(R.id.detail_name_textview);
        mDateView = (TextView) rootView.findViewById(R.id.detail_date_textview);
        mFriendlyDateView = (TextView) rootView.findViewById(R.id.detail_day_textview);
        mDescriptionView = (TextView) rootView.findViewById(R.id.detail_description_textview);
        mCategoryView = (TextView) rootView.findViewById(R.id.detail_category_textview);
        mUrlView = (TextView) rootView.findViewById(R.id.detail_url_textview);
        mVenueView = (TextView) rootView.findViewById(R.id.detail_venue_textview);
        mStatusView = (TextView) rootView.findViewById(R.id.detail_status_textview);
        mCapacityView = (TextView) rootView.findViewById(R.id.detail_capacity_textview);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (mEvent != null) {
            mShareActionProvider.setShareIntent(createShareEventIntent());
        }
    }

    private Intent createShareEventIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mEvent + EVENT_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }
//
//    void onLocationChanged( String newLocation ) {
//        Uri uri = mUri;
//        if (null != uri) {
//            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
//            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
//            mUri = updatedUri;
//            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
//        }
//    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if ( null != mUri ) {
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    Projections.EVENT_COLUMNS_DETAIL_VIEW,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            int eventId = data.getInt(Projections.EventsDetailView.COL_EVENT_ID);


            String imageUrl = data.getString(Projections.EventsDetailView.COL_LOGO_URL);
            ConnectivityManager connMgr = (ConnectivityManager)
                    getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

            if ((imageUrl.length() > 0) && (networkInfo != null && networkInfo.isConnected())) {
                Picasso.with(getActivity()).load(imageUrl)
                        .placeholder(R.drawable.default_placeholder)
                        .error(R.drawable.default_placeholder)
                        .resize(128, 128).centerCrop().into(mImageView);
            } else {
                Picasso.with(getActivity()).load(R.drawable.default_placeholder)
                        .resize(128, 128).centerCrop().into(mImageView);
            }

            // Read date from cursor and update views for day of week and date
            String eventStart = data.getString(Projections.EventsListView.COL_START_DATE_TIME);
            String eventEnd = data.getString(Projections.EventsListView.COL_END_DATE_TIME);

//            if ((eventStart != null) && (eventEnd != null)) {
//                String[] formattedDateTimes = DateUtils.formatStartAndEndDateTimes(eventStart, eventEnd);
//
//                mFriendlyDateView.setText(formattedDateTimes[0]);
//                mDateView.setText(formattedDateTimes[1]);
//            }


            String eventName = data.getString(Projections.EventsDetailView.COL_NAME);
            mNameTextView.setText(eventName);

            mImageView.setContentDescription(eventName);

            String description = data.getString(Projections.EventsDetailView.COL_DESCRIPTION);
            mDescriptionView.setText(description);


            String category = data.getString(Projections.EventsDetailView.COL_CATEGORY);
            mCategoryView.setText(category);

            String eventUrl = data.getString(Projections.EventsDetailView.COL_URL);
            mUrlView.setText(eventUrl);

            String venue = data.getString(Projections.EventsDetailView.COL_VENUE_NAME);
            mVenueView.setText(venue);

            String status = data.getString(Projections.EventsDetailView.COL_STATUS);
            mStatusView.setText(status);

            int capacity = data.getInt(Projections.EventsDetailView.COL_CAPACITY);
            mCapacityView.setText(Integer.toString(capacity));

            // We still need this for the share intent
            mEvent = String.format("%s - %s - %s/%s", eventName, description, category, eventUrl);

            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareEventIntent());
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }
}