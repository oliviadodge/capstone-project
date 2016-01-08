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

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;


import android.support.v4.app.ShareCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import com.example.olivi.maphap.utils.DateUtils;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    static final String DETAIL_URI = "URI";

    private static final String EVENT_SHARE_HASHTAG = " #MapHap";


    private static final int DETAIL_LOADER = 0;

    private Uri mUri;

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

    private HashMap<Integer, TextView> mDataColToViewMap;

    private String mEvent;


    public DetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(LOG_TAG, "onCreateView called");
        Bundle arguments = getArguments();

        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        Log.i(LOG_TAG, "adding data columns and views to HasMap");
        mDataColToViewMap = new HashMap<>(Projections.EVENT_COLUMNS_DETAIL_VIEW.length);

//        mDataColToViewMap.put(Projections.EventsDetailView.COL_NAME,
//                (TextView) rootView.findViewById(R.id.detail_name_textview));
//        mDataColToViewMap.put(Projections.EventsDetailView.COL_CAPACITY,
//                (TextView) rootView.findViewById(R.id.detail_capacity_textview));
//        mDataColToViewMap.put(Projections.EventsDetailView.COL_CATEGORY,
//                (TextView) rootView.findViewById(R.id.detail_category_textview));
//        mDataColToViewMap.put(Projections.EventsDetailView.COL_DESCRIPTION,
//                (TextView) rootView.findViewById(R.id.detail_description_textview));
//        mDataColToViewMap.put(Projections.EventsDetailView.COL_START_DATE_TIME,
//                (TextView) rootView.findViewById(R.id.detail_date_textview));
//        mDataColToViewMap.put(Projections.EventsDetailView.COL_END_DATE_TIME,
//                (TextView) rootView.findViewById(R.id.detail_day_textview));
//        mDataColToViewMap.put(Projections.EventsDetailView.COL_VENUE_NAME,
//                (TextView) rootView.findViewById(R.id.detail_venue_textview));
//        mDataColToViewMap.put(Projections.EventsDetailView.COL_STATUS,
//                (TextView) rootView.findViewById(R.id.detail_status_textview));
//        mDataColToViewMap.put(Projections.EventsDetailView.COL_URL,
//                (TextView) rootView.findViewById(R.id.detail_url_textview));

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
//
//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        inflater.inflate(R.menu.detailfragment, menu);
//
//        // Retrieve the share menu item
//        MenuItem menuItem = menu.findItem(R.id.action_share);
//
//        // Get the provider and hold onto it to set/change the share intent.
//        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
//
//        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
//        if (mEvent != null) {
//            mShareActionProvider.setShareIntent(createShareEventIntent());
//        }
//    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "onActivityCreated called");
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        //TODO add an interface for detail fragment's callbacks so we can
        //let the activity know when we have some text for the user to share.
        //Then the activity can decide what to do (get a reference to the fab button
        //and change it's onClick listener method to share the info.
        getActivity().findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Some sample text") //TODO add info about event here and maybe the url
                        .getIntent(), getString(R.string.action_share)));
            }
        });
        mImageView = (ImageView) getActivity().findViewById(R.id.detail_image);
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
        Log.i(LOG_TAG, "onCreateLoader called");
        if (null != mUri) {
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
        Log.i(LOG_TAG, "onLoadFinished called");
        if (data != null && data.moveToFirst()) {
            String imageUrl = data.getString(Projections.EventsDetailView.COL_LOGO_URL);
            ConnectivityManager connMgr = (ConnectivityManager)
                    getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if ((imageUrl.length() > 0) && (networkInfo != null && networkInfo.isConnected())) {
                Picasso.with(getActivity()).load(imageUrl).placeholder(R.drawable.default_placeholder).error(R.drawable.default_placeholder)
                        .resize(390, 260).centerInside().into(mImageView);
            } else {
                Picasso.with(getActivity()).load(R.drawable.default_placeholder)
                        .resize(390, 260).centerInside().into(mImageView);
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
            mImageView.setContentDescription(data.getString(Projections.EventsDetailView.COL_NAME));

//            Set set = mDataColToViewMap.entrySet();
//            Iterator iterator = set.iterator();
//            while (iterator.hasNext()) {
//                Map.Entry mentry = (Map.Entry) iterator.next();
//                int columnIdx = (Integer) mentry.getKey();
//                ((TextView) mentry.getValue()).setText(data.getString(columnIdx));
//                Log.i(LOG_TAG, "for " + data.getColumnName(columnIdx) + " data is "
//                        + data.getString(columnIdx) + ".");
//            }


            String eventName = data.getString(Projections.EventsDetailView.COL_NAME);
            mNameTextView.setText(eventName);


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
            mEvent = String.format("%s - %s - %s/%s", "eventName", "description", "category", "eventUrl");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.i(LOG_TAG, "onLoadReset called");
    }
}