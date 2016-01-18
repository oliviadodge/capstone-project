package com.example.olivi.maphap.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.olivi.maphap.R;

import java.util.ArrayList;
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


    public static boolean[] getPreferredCategoriesBooleanArray(Context context) {

        HashSet<String> prefCat = getPreferredCategories(context);
        String[] allCats = context.getResources().getStringArray(R.array
                .entryvalues_category_preference);

        boolean[] categoriesForDialog = new boolean[allCats.length];

        for (int i = 0; i < allCats.length; i++) {
            String cat = allCats[i];
            if (prefCat.contains(cat)) {
                categoriesForDialog[i] = true;
            } else {
                categoriesForDialog[i] = false;
            }
        }

        return categoriesForDialog;
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

    public static void savePreferredCategories(Context context, ArrayList<Integer> items) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        String[] categoryArray = context.getResources().getStringArray(R.array
                .entryvalues_category_preference);

        HashSet<String> prefCategories = new HashSet<>(items.size());

        for (int item : items) {
            String cat = categoryArray[item];
            Log.i(TAG, "saving category " + cat + " to shared prefs");
            prefCategories.add(cat);
        }

        editor.putStringSet(Constants.PREF_CATEGORY_KEY, prefCategories);
        editor.commit();
    }

    public static long getPreferredMillis(Context context, String key, long defaultMillis) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(key, defaultMillis);
    }

}
