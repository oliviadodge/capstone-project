package com.example.olivi.maphap;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.olivi.maphap.ui.DividerItemDecoration;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class EventListFragment extends Fragment {

    public static final String LOG_TAG = EventListFragment.class.getSimpleName();

    private static final String SELECTED_KEY = "selected_position";

    private EventRecyclerViewAdapter mAdapter;
    private int mPosition = RecyclerView.NO_POSITION;
    private RecyclerView mRecyclerView;
    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EventListFragment() {
    }

    @SuppressWarnings("unused")
    public static EventListFragment newInstance(int columnCount) {
        EventListFragment fragment = new EventListFragment();
        Bundle args = new Bundle();
        //TODO do we need args? if so, args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(LOG_TAG, "onCreate called");

        //TODO do we need a menu on this fragment? if so, setHasOptionsMenu(true);

        if (getArguments() != null) {
            //TODO do we need to send arguments to this fragment?
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(LOG_TAG, "onCreateView called");

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);

            Log.i(LOG_TAG, "onCreateView called and mPosition found: " + mPosition);
        }

        View view = inflater.inflate(R.layout.fragment_event_list, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            mRecyclerView = (RecyclerView) view;
            mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                    DividerItemDecoration.VERTICAL_LIST));
        }
        return view;
    }

    public void scrollToPreviousPosition() {
        if (mPosition != RecyclerView.NO_POSITION) {
            Log.i(LOG_TAG, "scrolling to position " + mPosition);
//            mRecyclerView.smoothScrollToPosition(mPosition);
            //TODO remove code related to saving mPosition. It is not needed because recycler view
            //will automatically save the scroll position
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((OnListFragmentInteractionListener) getActivity()).onListFragmentReady(mAdapter);
    }

    public EventRecyclerViewAdapter setUpAdapter(Cursor cursor) {

        Log.i(LOG_TAG, "MainActivity calling EventListFragment.setUpAdapter. Value of mListener " +
                "is " + mListener);
        mAdapter = new EventRecyclerViewAdapter(cursor, getActivity().getApplicationContext(),
                new EventRecyclerViewAdapter.AdapterCallbacks() {
                    @Override
                    public void onItemSelected(Uri eventUri, int position) {
                        mPosition = position;
                        mListener.onItemSelected(eventUri, position);
                    }

                    @Override
                    public void onItemLongClicked(Uri eventUri, int position) {
                        mListener.onItemLongClicked(eventUri, position);
                    }
                });

        mRecyclerView.setAdapter(mAdapter);

        return mAdapter;
    }

    public EventRecyclerViewAdapter getAdapter() {
        return mAdapter;
    }

    @TargetApi(23)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.i(LOG_TAG, "onAttach called. Setting up mListener.");
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    //For versions 22 and below
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Log.i(LOG_TAG, "onAttach called. Setting up mListener.");
        if (activity instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mPosition != RecyclerView.NO_POSITION) {
            Log.i(LOG_TAG, "onSaveInstanceState called. Saving mPosition " + mPosition);
            outState.putInt(SELECTED_KEY, mPosition);
        }

    }

    @Override
    public void onStart() {
        Log.i(LOG_TAG, "onStart called");
        super.onStart();
    }

    @Override
    public void onStop() {
        Log.i(LOG_TAG, "onStop called");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.i(LOG_TAG, "onDestroy called");
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(LOG_TAG, "EventListFragment.onDetach() called. Setting mListener to null");
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        void onItemSelected(Uri eventUri, int position);

        void onListFragmentReady(EventRecyclerViewAdapter adapter);

        void onItemLongClicked(Uri eventUri, int position);
    }
}
