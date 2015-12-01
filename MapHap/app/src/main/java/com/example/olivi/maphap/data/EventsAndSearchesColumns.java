package com.example.olivi.maphap.data;

import com.example.olivi.maphap.service.EventsDataJsonParser;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.PrimaryKey;
import net.simonvt.schematic.annotation.References;

/**
 * Created by olivi on 11/30/2015.
 */
public interface EventsAndSearchesColumns {

    @DataType(DataType.Type.INTEGER)
    @PrimaryKey
    @References(table = EventDatabase.Tables.EVENTS,
            column = EventsColumns._ID)
    public static final String EVENT_ID =
            "event_id";


    @DataType(DataType.Type.INTEGER)
    @PrimaryKey
    @References(table = EventDatabase.Tables.SEARCHES,
            column = SearchColumns._ID)
    public static final String SEARCH_ID =
            "search_id";
}
