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
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case Constants.PREF_RADIUS_KEY:
                Log.i(TAG, "onSharedPreference changed and key is radius_key");
                setExtra(PREF_RADIUS_CHANGED_EXTRA, true);
                break;

            case Constants.PREF_CATEGORY_KEY:
                setExtra(PREF_CATEGORY_CHANGED_EXTRA, true);
                break;

            case Constants.PREF_START_DATE_KEY:
                setExtra(PREF_START_DATE_CHANGED_EXTRA, true);
                break;

            case Constants.PREF_END_DATE_KEY:
                setExtra(PREF_END_DATE_CHANGED_EXTRA, true);
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

}
