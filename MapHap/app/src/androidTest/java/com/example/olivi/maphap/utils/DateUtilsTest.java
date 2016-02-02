package com.example.olivi.maphap.utils;

import junit.framework.TestCase;

/**
 * Created by olivi on 12/14/2015.
 */

public class DateUtilsTest extends TestCase{

    private static final int MILLIS_IN_AN_HOUR = 3600000;
    private static final int MILLIS_IN_A_DAY = MILLIS_IN_AN_HOUR * 24;
    private static final int MIN_CUTOFF_MILLIS =
            (-(DateUtils.ROLLBACK_TO_CUTOFF_DATE) * MILLIS_IN_A_DAY) - MILLIS_IN_AN_HOUR;
    private static final int MAX_CUTOFF_MILLIS =
            (-(DateUtils.ROLLBACK_TO_CUTOFF_DATE) * MILLIS_IN_A_DAY) + MILLIS_IN_AN_HOUR;
    protected String startDateTime;
    protected String formattedStart;
    protected String endDateTime;
    protected String formattedEnd;


    protected void setUp() {
        startDateTime = "2008-05-11T19:00:00";
        formattedStart = "May 11 at 7:00 PM";
        endDateTime = "2008-05-12T16:00:00";
        formattedEnd = "May 12 at 4:00 PM";


    }


}