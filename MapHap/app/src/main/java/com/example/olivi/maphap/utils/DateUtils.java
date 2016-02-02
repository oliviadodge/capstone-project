package com.example.olivi.maphap.utils;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by olivi on 12/27/2015.
 */
public class DateUtils {

    private static final String TAG = DateUtils.class.getSimpleName();
    public static final String DATABASE_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String DATABASE_QUERY_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String UI_DATE_TIME_FORMAT = "MMM d 'at' h:mm a";
    public static final String FORMAT_LIST_TEXTVIEW_DATE_TIME = "EEE, MMM d 'at' ";
    public static final String FORMAT_TIME = "h:mm a";
    public static final String FORMAT_TIME_WITHOUT_MIN = "h a";
    public static final String FORMAT_DETAIL_TEXTVIEW_DATE_TIME = "EEEE, MMMM d 'at' ";

    static final String TEST_ADDED_DATE = "2011-01-12T16:00:00";

    public static final int ROLLBACK_TO_CUTOFF_DATE = -1;

    public static long convertDateTimeStringToLong(String dateTimeString) {

        Date dateTime = getDateFromString(dateTimeString);
        return dateTime.getTime();
    }

    public static Calendar getCutOffDateTime() {

        Date currentDateTime = new Date();

        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDateTime);
        cal.add(Calendar.DAY_OF_MONTH, ROLLBACK_TO_CUTOFF_DATE);

        return cal;
    }

    public static double getCutOffJulianDateTime() {
        Calendar cutoffCal = getCutOffDateTime();
        return JulianDateConverter.dateToJulian(cutoffCal);
    }

    public static double getCurrentJulianDateTime() {
        Date currentDateTime = new Date();

        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDateTime);

        return JulianDateConverter.dateToJulian(cal);
    }

    public static double getJulianDateFromDate(Date date) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        return JulianDateConverter.dateToJulian(cal);
    }

    public static Date getDateFromString(String date) {
        SimpleDateFormat fromFormat =
                new SimpleDateFormat(DATABASE_DATE_TIME_FORMAT, Locale.US);
        try {
            return fromFormat.parse(date);
        } catch (ParseException e) {
            Log.d(TAG, "couldn't parse date! Must be in wrong format");
            e.printStackTrace();
        }

        return null;
    }

    public static boolean isDateTimeAfterCutOff(double julianDays) {
        double cutoff = getCutOffJulianDateTime();
        return (julianDays > cutoff);
    }

    public static double getTestDateAdded() {
        Date testDate = DateUtils.getDateFromString(TEST_ADDED_DATE);
        return DateUtils.getJulianDateFromDate(testDate);
    }

    //method to determine whether, given a start date time string and
    //end date time string, the interval occurs within one calendar day as
    //opposed to over the course of a few days
    public static boolean isIntervalInOneDay(@NonNull String startDateTime,
                                             @NonNull String endDateTime) {

        Calendar startCal = Calendar.getInstance();
        startCal.setTime(getDateFromString(startDateTime));

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(getDateFromString(endDateTime));

        int startDay = startCal.get(Calendar.DAY_OF_MONTH);
        if (startDay != endCal.get(Calendar.DAY_OF_MONTH)) {
            return false;
        }
        int startMonth = startCal.get(Calendar.MONTH);
        if (startMonth != endCal.get(Calendar.MONTH)) {
            return false;
        }
        int startYear = startCal.get(Calendar.YEAR);

        return (startYear == endCal.get(Calendar.YEAR));
    }

    public static String formatDateTime(String format, long dateTime) {
        String timeFormat = getTimeFormatter(dateTime);
        SimpleDateFormat toFormat =
                new SimpleDateFormat(format + timeFormat, Locale.US);
        Date date = new Date(dateTime);
        return toFormat.format(date);
    }

    private static int getMinutes(long timeInMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeInMillis);
        return cal.get(Calendar.MINUTE);
    }

    private static String getTimeFormatter(long timeInMillis) {
        int minutes = getMinutes(timeInMillis);
        if (minutes == 0) {
            return FORMAT_TIME_WITHOUT_MIN;
        } else
            return FORMAT_TIME;
    }

    public static String getSingleDayStartAndEndTime(String format, long startDateTime, long
            endDateTime) {
        String formattedStartDateTime = DateUtils.formatDateTime(format,
                startDateTime);

        String endFormat = getTimeFormatter(endDateTime);
        SimpleDateFormat toFormat = new SimpleDateFormat(endFormat, Locale.US);
        Date endDate = new Date(endDateTime);
        String formattedEndTime = toFormat.format(endDate);

        return formattedStartDateTime + " - " + formattedEndTime;
    }

    public static String formatEndDateTimeString(String format, long endDateTime) {
        String formattedEndTime = DateUtils.formatDateTime(format, endDateTime);

        return "to " + formattedEndTime;
    }

    public static void setUpDateTimeTextViews(String format, TextView start, TextView end, long
            startMillis, long endMillis) {

        if ((endMillis - startMillis) <= Constants.MILLIS_IN_A_DAY) {
            String text = DateUtils.getSingleDayStartAndEndTime(format, startMillis, endMillis);
            start.setText(text);
            end.setVisibility(View.GONE);
        } else {
            String startString = DateUtils.formatDateTime(format,
                    startMillis);
            String endString = DateUtils.formatEndDateTimeString(format, endMillis);
            start.setText(startString);
            end.setText(endString);

        }

    }

    public static String getShareDateTIme(String format, long
            startMillis, long endMillis) {

        if ((endMillis - startMillis) <= Constants.MILLIS_IN_A_DAY) {
            return DateUtils.getSingleDayStartAndEndTime(DateUtils
                    .FORMAT_DETAIL_TEXTVIEW_DATE_TIME, startMillis, endMillis);
        } else {
            String startString = DateUtils.formatDateTime(DateUtils
                            .FORMAT_DETAIL_TEXTVIEW_DATE_TIME,
                    startMillis);
            String endString = DateUtils.formatEndDateTimeString(DateUtils
                    .FORMAT_DETAIL_TEXTVIEW_DATE_TIME, endMillis);

            return startString + endString;

        }

    }

    public static long getTodayInMillis() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTimeInMillis();
    }
}

