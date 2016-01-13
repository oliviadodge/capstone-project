package com.example.olivi.maphap;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by olivi on 1/12/2016.
 */
public class PrefsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}
