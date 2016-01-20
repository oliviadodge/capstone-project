package com.example.olivi.maphap;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.olivi.maphap.utils.Constants;

public class FilterActivity extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String PREF_RADIUS_CHANGED_EXTRA = "pref_radius_changed";
    public static final String PREF_CATEGORY_CHANGED_EXTRA = "pref_category_changed";
    public static final String PREF_START_DATE_CHANGED_EXTRA = "pref_start_date_changed";
    public static final String PREF_END_DATE_CHANGED_EXTRA = "pref_end_date_changed";


    private boolean mRadiusChanged;
    private boolean mCategoryChanged;
    private boolean mStartDateChanged;
    private boolean mEndDateChanged;


    private static final String TAG = FilterActivity.class.getSimpleName();

    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        Log.i(TAG, "in onCreate. Registering the onSharedPreferenceChangeListener");
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        mIntent = new Intent();
        setResult(RESULT_OK, mIntent);

        ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle(getString(R.string.activity_filter_title_action_bar));
        }

        if (savedInstanceState != null) {
            mRadiusChanged = savedInstanceState.getBoolean(PREF_RADIUS_CHANGED_EXTRA);
            mCategoryChanged = savedInstanceState.getBoolean(PREF_CATEGORY_CHANGED_EXTRA);
            mStartDateChanged = savedInstanceState.getBoolean(PREF_START_DATE_CHANGED_EXTRA);
            mEndDateChanged = savedInstanceState.getBoolean(PREF_END_DATE_CHANGED_EXTRA);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case Constants.PREF_RADIUS_KEY:
                Log.i(TAG, "onSharedPreference changed and key is radius_key");
                setExtra(PREF_RADIUS_CHANGED_EXTRA, true);
                mRadiusChanged = true;
                break;

            case Constants.PREF_CATEGORY_KEY:
                setExtra(PREF_CATEGORY_CHANGED_EXTRA, true);
                mCategoryChanged = true;

                break;

            case Constants.PREF_START_DATE_KEY:
                setExtra(PREF_START_DATE_CHANGED_EXTRA, true);
                mStartDateChanged = true;

                break;

            case Constants.PREF_END_DATE_KEY:
                setExtra(PREF_END_DATE_CHANGED_EXTRA, true);
                mEndDateChanged = true;

                break;
        }
    }

    private void setExtra(String extra, boolean changed) {
        mIntent.putExtra(extra, changed);
        setResult(RESULT_OK, mIntent);
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
        outState.putBoolean(PREF_RADIUS_CHANGED_EXTRA, mRadiusChanged);
        outState.putBoolean(PREF_CATEGORY_CHANGED_EXTRA, mCategoryChanged);
        outState.putBoolean(PREF_START_DATE_CHANGED_EXTRA, mStartDateChanged);
        outState.putBoolean(PREF_END_DATE_CHANGED_EXTRA, mEndDateChanged);
        super.onSaveInstanceState(outState);
    }
}
