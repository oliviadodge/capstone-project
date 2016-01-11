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

import android.support.v4.app.ShareCompat;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
        //TODO add an interface for detail fragment's callbacks so we can
        //let the activity know when we have some text for the user to
        // share.
        //Then the activity can decide what to do (get a reference to
        // the fab button
        //and change it's onClick listener method to share the info.
        getActivity().findViewById(R.id.fab).setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(Intent.createChooser(ShareCompat
                        .IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Some sample text") //TODO add info
                                // about event here and maybe
                                // the url
                        .getIntent(), getString(R.string.action_share)));
            }
        });
        mImageView = (ImageView) getActivity().findViewById(R.id
                .detail_image);
        super.onActivityCreated(savedInstanceState);
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
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.i(LOG_TAG, "onLoadReset called");
    }

    public void setUpAppBarImage(Cursor data) {
        String imageUrl = data.getString(Projections
                .EventsDetailView.COL_LOGO_URL);
        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context
                        .CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
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

        //Create first card containing event title, date, and place.
        Card detailCard = new Card(getActivity(),R.layout.card_event_layout1);

        String name = data.getString(Projections.EventsDetailView
                .COL_NAME);

        addCardHeader(detailCard, name);

        CardViewNative cardView = (CardViewNative) getActivity()
                .findViewById(R.id.card_event_1);
        cardView.setCard(detailCard);

        View v = cardView.getInternalContentLayout();

        setTextView(v, R.id.detail_start_textview, data,
                Projections.EventsDetailView.COL_START_DATE_TIME);

        setTextView(v, R.id.detail_end_textview, data,
                Projections.EventsDetailView.COL_END_DATE_TIME);

        setTextView(v, R.id.detail_venue_textview, data,
                Projections.EventsDetailView.COL_VENUE_NAME);

        TextView urlTextView = (TextView) v.findViewById(R.id
                .detail_url_textview);

        urlTextView.setText(
                data.getString(Projections.EventsDetailView.COL_URL));

        //Create the second card with the event description and category
        Card descriptionCard = new Card(getActivity(),R.layout
                .card_event_layout2);

        addCardHeader(descriptionCard, getString(R.string
                .header_event_description));

        CardViewNative descriptionCardView = (CardViewNative) getActivity()
                .findViewById(R.id.card_event_2);
        descriptionCardView.setCard(descriptionCard);

        View v2 = descriptionCardView.getInternalContentLayout();
        String descHtml = data.getString(Projections.EventsDetailView
                .COL_DESCRIPTION);
        ((TextView) v2.findViewById(R.id.detail_description_textview))
                .setText(descHtml);

        setTextView(v2, R.id.detail_category_textview, data,
                Projections.EventsDetailView.COL_CATEGORY);



        //Create the third card with the status, and capactiy (maybe
        // ticket prices and where to find)
        Card otherDetailsCard = new Card(getActivity(),R.layout
                .card_event_layout3);

        addCardHeader(otherDetailsCard, getString(R.string
                .header_other_details));

        CardViewNative otherDetailsCardView = (CardViewNative) getActivity()
                .findViewById(R.id.card_event_3);
        otherDetailsCardView.setCard(otherDetailsCard);

        View v3 = otherDetailsCardView.getInternalContentLayout();
        setTextView(v3, R.id.detail_status_textview, data,
                Projections.EventsDetailView.COL_STATUS);

        setTextView(v3, R.id.detail_capacity_textview, data,
                Projections.EventsDetailView.COL_CAPACITY);



    }

    public void setTextView(View parent, int resId, Cursor data, int
            columnIdx) {
        ((TextView) parent.findViewById(resId)).setText(
                data.getString(columnIdx)
        );
    }

    public void addCardHeader(Card card, String title) {
        //Create a CardHeader
        CardHeader header = new CardHeader(getActivity());
        header.setTitle(title);
        card.addCardHeader(header);
    }
}