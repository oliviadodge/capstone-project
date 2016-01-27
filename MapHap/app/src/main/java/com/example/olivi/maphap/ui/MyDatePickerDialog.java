package com.example.olivi.maphap.ui;

import android.os.Bundle;
import android.util.Log;

import com.borax12.materialdaterangepicker.date.DatePickerDialog;
import com.example.olivi.maphap.R;
import com.example.olivi.maphap.utils.Utility;

import java.util.Calendar;

/**
 * Created by olivi on 1/16/2016.
 */
public class MyDatePickerDialog extends DatePickerDialog {

    private static final String TAG = MyDatePickerDialog.class.getSimpleName();

    private static final String KEY_SELECTED_YEAR = "year";
    private static final String KEY_SELECTED_YEAR_END = "year_end";
    private static final String KEY_SELECTED_MONTH = "month";
    private static final String KEY_SELECTED_MONTH_END = "month_end";
    private static final String KEY_SELECTED_DAY = "day";
    private static final String KEY_SELECTED_DAY_END = "day_end";

    public MyDatePickerDialog() {
    }

    public static MyDatePickerDialog newInstance(OnDateSetListener callBack, int year,
                                                 int monthOfYear,
                                                 int dayOfMonth) {
        MyDatePickerDialog ret = new MyDatePickerDialog();
        ret.initialize(callBack, year, monthOfYear, dayOfMonth);
        return ret;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        long startMillis = Utility.getPreferredMillis(getActivity(), getString(R.string
                .pref_start_date_key), -1);

        long endMillis = Utility.getPreferredMillis(getActivity(), getString(R.string
                .pref_end_date_key), -1);
        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();

        if ((savedInstanceState == null) && (startMillis != -1) && (endMillis != -1)) {
            startCal.setTimeInMillis(startMillis);
            endCal.setTimeInMillis(endMillis);
            Log.i(TAG, "savedInstanceState = " + savedInstanceState + " and startMillis = "
                    + startMillis);

            savedInstanceState = new Bundle();
            savedInstanceState.putInt(KEY_SELECTED_YEAR, startCal.get(Calendar.YEAR));
            savedInstanceState.putInt(KEY_SELECTED_MONTH, startCal.get(Calendar.MONTH));
            savedInstanceState.putInt(KEY_SELECTED_DAY, startCal.get(Calendar.DAY_OF_MONTH));

            savedInstanceState.putInt(KEY_SELECTED_YEAR_END, endCal.get(Calendar.YEAR));
            savedInstanceState.putInt(KEY_SELECTED_MONTH_END, endCal.get(Calendar.MONTH));
            savedInstanceState.putInt(KEY_SELECTED_DAY_END, endCal.get(Calendar.DAY_OF_MONTH));
        }
        super.onCreate(savedInstanceState);
    }

}
