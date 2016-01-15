package com.example.olivi.maphap.utils;

import android.content.Context;
import android.preference.PreferenceManager;

import com.example.olivi.maphap.R;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by olivi on 1/14/2016.
 */
public class Utility {


    private static final String TAG = Utility.class.getSimpleName();


    public static Set<String> getPreferredCategories(Context context) {

        String[] defaultArray = context.getResources()
                .getStringArray(R.array.defaultValues_category_preference);

        return PreferenceManager.getDefaultSharedPreferences(context)
                .getStringSet(context.getString(R.string.pref_category_key),
                        new HashSet<String>(Arrays.asList(defaultArray)));
    }
}
