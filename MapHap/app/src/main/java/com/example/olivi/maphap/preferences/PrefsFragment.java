package com.example.olivi.maphap.preferences;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.example.olivi.maphap.R;

/**
 * Created by olivi on 1/12/2016.
 */
public class PrefsFragment extends PreferenceFragment {
    private static final String TAG = PrefsFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

    }
}
