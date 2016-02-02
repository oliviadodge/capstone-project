package com.example.olivi.maphap;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.olivi.maphap.EventListFragment.OnListFragmentInteractionListener;
import com.example.olivi.maphap.data.EventProvider;
import com.example.olivi.maphap.utils.DateUtils;
import com.squareup.picasso.Picasso;

/**
 * {@link RecyclerView.Adapter} that can display a cursor and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class EventRecyclerViewAdapter extends RecyclerView.Adapter<EventRecyclerViewAdapter
        .ViewHolder> {

    private static final String LOG_TAG = EventRecyclerViewAdapter.class.getSimpleName();
    private Context mContext;
    protected Cursor mCursor;
    protected boolean mDataValid;
    protected int mRowIdColumn;
    private final AdapterCallbacks mListener;

    public EventRecyclerViewAdapter(Cursor cursor, Context context, AdapterCallbacks listener) {
        mContext = context;
        mCursor = cursor;
        mListener = listener;
        mDataValid = cursor != null;
        mRowIdColumn = mDataValid ? mCursor.getColumnIndex("_id") : -1;
        setHasStableIds(true);
    }

    public interface AdapterCallbacks {
        void onItemSelected(Uri eventUri, int position);

        void onItemLongClicked(Uri eventUri, int position);
    }

    public Cursor getCursor() {
        return mCursor;
    }

    @Override
    public int getItemCount() {
        if (mDataValid && mCursor != null) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }

        String eventName = mCursor.getString(Projections.EventsListView.COL_NAME);
        String venueName = mCursor.getString(Projections.EventsListView.COL_VENUE_NAME);
        long eventStart = mCursor.getLong(Projections.EventsListView.COL_START_DATE_TIME);
        long eventEnd = mCursor.getLong(Projections.EventsListView.COL_END_DATE_TIME);
        String logoUrl = mCursor.getString(Projections.EventsListView.COL_LOGO_URL);

        holder.nameView.setText(eventName);
        holder.venueView.setText(venueName);

        DateUtils.setUpDateTimeTextViews(DateUtils.FORMAT_LIST_TEXTVIEW_DATE_TIME,
                holder.startView, holder.endView, eventStart, eventEnd);

        //check connectivity to see if artist images can be loaded or if a placeholder must be
        // loaded
        ConnectivityManager connMgr = (ConnectivityManager)
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if ((logoUrl.length() > 0) && (networkInfo != null && networkInfo.isConnected())) {
            Picasso.with(mContext).load(logoUrl)
                    .placeholder(R.drawable.default_placeholder)
                    .error(R.drawable.default_placeholder)
                    .resize(128, 128).centerCrop().into(holder.imageView);
        } else {
            Picasso.with(mContext).load(R.drawable.default_placeholder)
                    .resize(128, 128).centerCrop().into(holder.imageView);
        }

        holder.imageView.setContentDescription(eventName);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    Log.i(LOG_TAG, "list item clicked. Calling mListener.onItemSelected" +
                            ". Uri is " + EventProvider.Events.withId(getItemId(position))
                            + "and ID is " + getItemId(position));
                    mListener.onItemSelected(EventProvider.Events.withId(getItemId(position)),
                            position);
                }
            }
        });

        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (null != mListener) {
                    mListener.onItemLongClicked(EventProvider.Events.withId(getItemId(position)),
                            position);
                }
                return true;
            }
        });
    }

    @Override
    public long getItemId(int position) {
        if (hasStableIds() && mDataValid && mCursor != null) {
            if (mCursor.moveToPosition(position)) {
                return mCursor.getLong(mRowIdColumn);
            } else {
                return RecyclerView.NO_ID;
            }
        } else {
            return RecyclerView.NO_ID;
        }
    }

    public void changeCursor(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
    }

    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        Cursor oldCursor = mCursor;
        mCursor = newCursor;
        if (newCursor != null) {
            mRowIdColumn = newCursor.getColumnIndexOrThrow("_id");
            mDataValid = true;
            // notify the observers about the new cursor
            notifyDataSetChanged();
        } else {
            mRowIdColumn = -1;
            mDataValid = false;
            // notify the observers about the lack of a data set
            notifyItemRangeRemoved(0, getItemCount());
        }
        return oldCursor;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView imageView;
        public final TextView nameView;
        public final TextView venueView;
        public final TextView startView;
        public final TextView endView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            imageView = (ImageView) view.findViewById(R.id.list_item_image);
            nameView = (TextView) view.findViewById(R.id.list_item_name_textview);
            venueView = (TextView) view.findViewById(R.id.list_item_venue_textview);
            startView = (TextView) view.findViewById(R.id.list_item_start_textview);
            endView = (TextView) view.findViewById(R.id.list_item_end_textview);
        }
    }
}
