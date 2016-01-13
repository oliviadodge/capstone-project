package com.example.olivi.maphap.widget;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.example.olivi.maphap.Projections;
import com.example.olivi.maphap.data.EventProvider;
import com.example.olivi.maphap.service.MapHapService;
import com.example.olivi.maphap.utils.DateUtils;
import com.example.olivi.maphap.utils.LocationUtils;

/**
 * Created by olivi on 1/12/2016.
 */
public class DetailWidgetIntentService extends IntentService {

    public DetailWidgetIntentService() {
        super("DetailWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // Get today's data from the ContentProvider
        long regionId = LocationUtils.getPreferredRegionId(this);
        Uri regionUri = EventProvider.Regions.withId(regionId);

        Cursor data = getContentResolver().query(regionUri, Projections.REGION_COLUMNS, null,
                null, null);
        if (data == null) {
            return;
        }
        if (!data.moveToFirst()) {
            data.close();
            return;
        }

        double dateAdded = data.getDouble(Projections.Regions.ADDED_DATE_TIME);
        if (!DateUtils.isDateTimeAfterCutOff(dateAdded)) {
            Intent i = new Intent(getApplicationContext(), MapHapService.class);
            startService(i);
        }

        data.close();
    }
}
