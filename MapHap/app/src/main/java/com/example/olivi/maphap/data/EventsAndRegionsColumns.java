package com.example.olivi.maphap.data;

import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.PrimaryKey;
import net.simonvt.schematic.annotation.References;

/**
 * Created by olivi on 11/30/2015.
 */
public interface EventsAndRegionsColumns {
    @DataType(DataType.Type.INTEGER)
    @PrimaryKey
    @References(table = EventDatabase.Tables.EVENTS,
            column = EventsColumns._ID)
    public static final String EVENT_ID =
            "event_id";


    @DataType(DataType.Type.INTEGER)
    @PrimaryKey
    @References(table = EventDatabase.Tables.REGIONS,
            column = SearchColumns._ID)
    public static final String REGION_ID =
            "region_id";
}
