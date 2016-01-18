package com.example.olivi.maphap.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.olivi.maphap.R;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by olivi on 1/14/2016.
 */
public class Utility {


    private static final String TAG = Utility.class.getSimpleName();


    public static HashSet<String> getPreferredCategories(Context context) {

        String[] defaultArray = context.getResources()
                .getStringArray(R.array.defaultValues_category_preference);

        return (HashSet<String>)PreferenceManager.getDefaultSharedPreferences(context)
                .getStringSet(context.getString(R.string.pref_category_key),
                        new HashSet<String>(Arrays.asList(defaultArray)));
    }

    public static void savePreferredStartDate(Context context, long startMillis) {
        String key = context.getString(R.string.pref_start_date_key);
        savePreferredMillis(context, key, startMillis);
    }

    public static void savePreferredEndDate(Context context, long endMillis) {
        String key = context.getString(R.string.pref_end_date_key);
        savePreferredMillis(context, key, endMillis);
    }

    public static void savePreferredMillis(Context context, String key, long millis) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        editor.putLong(key, millis);
        editor.commit();
    }

    public static long getPreferredMillis(Context context, String key, long defaultMillis) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(key, defaultMillis);
    }

}
