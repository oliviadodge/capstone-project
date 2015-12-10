package com.example.olivi.maphap.data;

import com.example.olivi.maphap.service.EventsDataJsonParser;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.ConflictResolutionType;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.PrimaryKey;
import net.simonvt.schematic.annotation.References;

/**
 * Created by olivi on 11/30/2015.
 */
public interface EventsAndSearchesColumns {


    @PrimaryKey(onConflict = ConflictResolutionType.REPLACE)
    @DataType(DataType.Type.TEXT)
    @References(table = EventDatabase.EVENTS,
            column = EventsColumns.EB_ID)
    String EVENT_ID =
            "event_id";


    @PrimaryKey(onConflict = ConflictResolutionType.REPLACE)
    @DataType(DataType.Type.INTEGER)
    @References(table = EventDatabase.SEARCHES,
            column = SearchColumns._ID)
    String SEARCH_ID =
            "search_id";


}
