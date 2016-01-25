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
import android.provider.CalendarContract;
import android.support.v4.app.ShareCompat;
import android.text.Editable;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.olivi.maphap.utils.DateUtils;
import com.squareup.picasso.Picasso;

import org.xml.sax.XMLReader;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.view.CardViewNative;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager
        .LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class
            .getSimpleName();
    static final String DETAIL_URI = "URI";

    private static final String EVENT_SHARE_HASHTAG = " #MapHap";

    private static final int DETAIL_LOADER = 0;

    private Uri mUri;

    private ImageView mImageView;
    private TextView mCapacityView;

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
        View rootView = inflater.inflate(R.layout.fragment_detail,
                container, false);

        mCapacityView = (TextView) rootView.findViewById(R.id
                .detail_capacity_textview);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "onActivityCreated called");
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);

        mImageView = (ImageView) getActivity().findViewById(R.id
                .detail_image);
        super.onActivityCreated(savedInstanceState);
    }

    private String getEventShareText(Cursor data) {
        if (!data.moveToFirst()) return null;

        String eventName = data.getString(Projections.EventsDetailView.COL_NAME);
        String venueName = data.getString(Projections.EventsDetailView.COL_VENUE_NAME);

        long startMillis = data.getLong(Projections.EventsDetailView
                .COL_START_DATE_TIME);
        long endMillis = data.getLong(Projections.EventsDetailView
                .COL_END_DATE_TIME);

        String dateTimeText = DateUtils.getShareDateTIme(DateUtils
                .FORMAT_DETAIL_TEXTVIEW_DATE_TIME, startMillis, endMillis);

        String eventUrl = data.getString(Projections.EventsDetailView.COL_URL);

        return eventName + " at " + venueName + " on " + dateTimeText + ". Event URL: " + eventUrl;
    }

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
            setUpCards(data);
            setUpAppBarImage(data);
        }

        final String shareText = getEventShareText(data);
        getActivity().findViewById(R.id.fab).setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(Intent.createChooser(ShareCompat
                        .IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText(shareText)
                        .getIntent(), getString(R.string.action_share)));
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.i(LOG_TAG, "onLoadReset called");
    }

    public void setUpAppBarImage(Cursor data) {
        String imageUrl = data.getString(Projections
                .EventsDetailView.COL_LOGO_URL);

        NetworkInfo networkInfo = getNetworkInfo();

        if ((imageUrl.length() > 0) && (networkInfo != null &&
                networkInfo.isConnected())) {
            Picasso.with(getActivity()).load(imageUrl).placeholder(R
                    .drawable
                    .default_placeholder).error(R.drawable
                    .default_placeholder)
                    .resize(390, 260).centerInside().into(mImageView);
        } else {
            Picasso.with(getActivity()).load(R.drawable
                    .default_placeholder)
                    .resize(390, 260).centerInside().into(mImageView);
        }

        mImageView.setContentDescription(data.getString(Projections
                .EventsDetailView.COL_NAME));

    }

    public void setUpCards(Cursor data) {
        setUpFirstCard(data);
        setUpSecondCard(data);
        setUpThirdCard(data);
    }

    private void setUpFirstCard(Cursor data) {
        //Create first card containing event title, date, and place.
        Card detailCard = new Card(getActivity(),R.layout.card_event_layout1);

        String name = data.getString(Projections.EventsDetailView
                .COL_NAME);

        addCardHeader(detailCard, name);

        CardViewNative cardView = (CardViewNative) getActivity()
                .findViewById(R.id.card_event_1);
        cardView.setCard(detailCard);

        View v = cardView.getInternalContentLayout();
        long startMillis = data.getLong(Projections.EventsDetailView
                .COL_START_DATE_TIME);
        long endMillis = data.getLong(Projections.EventsDetailView
                .COL_END_DATE_TIME);

        LinearLayout addToCalendar = (LinearLayout) v.findViewById(R.id.detail_button_add_event_to_calendar);
        addToCalendar.setOnClickListener(new AddToCalendarClickListener(data));

        TextView start = (TextView)v.findViewById(R.id.detail_start_textview);
        TextView end = (TextView)v.findViewById(R.id.detail_end_textview);

        DateUtils.setUpDateTimeTextViews(DateUtils.FORMAT_DETAIL_TEXTVIEW_DATE_TIME,
                start, end, startMillis, endMillis);

        setTextView(v, R.id.detail_venue_textview, data,
                Projections.EventsDetailView.COL_VENUE_NAME);

        TextView urlTextView = (TextView) v.findViewById(R.id
                .detail_url_textview);

        urlTextView.setText(
                data.getString(Projections.EventsDetailView.COL_URL));
    }

    public void setUpSecondCard(Cursor data) {
        //Create the second card with the event description and category
        Card descriptionCard = new Card(getActivity(), R.layout
                .card_event_layout2);
        addCardHeader(descriptionCard, getString(R.string
                .header_event_description));
        CardViewNative descriptionCardView = (CardViewNative) getActivity()
                .findViewById(R.id.card_event_2);
        descriptionCardView.setCard(descriptionCard);
        View v2 = descriptionCardView.getInternalContentLayout();
        String descHtml = data.getString(Projections.EventsDetailView
                .COL_DESCRIPTION);

        Html.TagHandler th = new Html.TagHandler() {
            @Override
            public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
                Log.i(LOG_TAG, "handleTag called. Tag: " + tag);
            }
        };

        ((TextView) v2.findViewById(R.id.detail_description_textview))
                .setText(Html.fromHtml(descHtml));

        String catId = data.getString(Projections.EventsDetailView.COL_CATEGORY);
        String category = getCategoryFromId(catId);


        ((TextView) v2.findViewById(R.id.detail_category_textview))
                .setText(category);
    }

    public void setUpThirdCard(Cursor data) {
        //Create the third card with the status, and capactiy (maybe
        // ticket prices and where to find)
        Card otherDetailsCard = new Card(getActivity(), R.layout
                .card_event_layout3);
        addCardHeader(otherDetailsCard, getString(R.string
                .header_other_details));
        CardViewNative otherDetailsCardView = (CardViewNative)
                getActivity()
                        .findViewById(R.id.card_event_3);
        otherDetailsCardView.setCard(otherDetailsCard);
        View v3 = otherDetailsCardView.getInternalContentLayout();
        setTextView(v3, R.id.detail_status_textview, data,
                Projections.EventsDetailView.COL_STATUS);
        setTextView(v3, R.id.detail_capacity_textview, data,
                Projections.EventsDetailView.COL_CAPACITY);
    }

    private void setTextView(View parent, int resId, Cursor data, int
            columnIdx) {
        ((TextView) parent.findViewById(resId)).setText(
                data.getString(columnIdx)
        );
    }

    private void addCardHeader(Card card, String title) {
        //Create a CardHeader
        CardHeader header = new CardHeader(getActivity());
        header.setTitle(title);
        card.addCardHeader(header);
    }

    private NetworkInfo getNetworkInfo() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context
                        .CONNECTIVITY_SERVICE);

        return connMgr.getActiveNetworkInfo();
    }

    private String getCategoryFromId(String categoryId) {
        String [] catIds = getResources().getStringArray(R.array.entryvalues_category_preference);
        int catIndex = -1;

        for (int i = 0; i < catIds.length; i++) {
            if (catIds[i].equals(categoryId))
                catIndex = i;
        }

        if (catIndex != -1) {
            return getResources().getStringArray(R.array.entries_category_preference)[catIndex];
        } else {
            return null;
        }
    }

    private class AddToCalendarClickListener implements View.OnClickListener {

        Cursor data;
        long startMillis;
        long endMillis;

        public AddToCalendarClickListener(Cursor cursor) {
            data = cursor;
            startMillis = cursor.getLong(Projections.EventsDetailView.COL_START_DATE_TIME);
            endMillis = cursor.getLong(Projections.EventsDetailView.COL_END_DATE_TIME);
        }
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
                    .putExtra(CalendarContract.Events.TITLE, data.getString(Projections
                            .EventsDetailView.COL_NAME))
                    .putExtra(CalendarContract.Events.DESCRIPTION, Projections.EventsDetailView
                            .COL_URL)
                    .putExtra(CalendarContract.Events.EVENT_LOCATION, Projections
                            .EventsDetailView.COL_VENUE_NAME);
            startActivity(intent);
        }
    }
}