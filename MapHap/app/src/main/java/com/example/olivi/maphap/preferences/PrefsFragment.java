package com.example.olivi.maphap.preferences;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.olivi.maphap.R;

/**
 * Created by olivi on 1/12/2016.
 */
public class PrefsFragment extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = PrefsFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
        // updated when the preference changes.
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_radius_key)));
    }

    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getInt(preference.getKey(), NumberPickerPreference.DEFAULT_VALUE));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.i(TAG, "onPreferenceChange called");

        if (preference instanceof NumberPickerPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            NumberPickerPreference numPicker = (NumberPickerPreference) preference;
            int radius = numPicker.getValue();

            preference.setSummary("Select search radius (in miles) for the events you're " +
                    "interested in. " +
                    "Current radius: " + radius + " miles.");

        }
        return true;
    }
}
