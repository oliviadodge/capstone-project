package com.example.olivi.maphap.data;

import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;
import net.simonvt.schematic.annotation.References;

/**
 * Created by olivi on 11/30/2015.
 */
public interface CategoriesAndRegionsColumns {
    @DataType(DataType.Type.TEXT)
    @PrimaryKey
    @References(table = EventDatabase.EVENTS,
            column = EventsColumns.CATEGORY)
    String CATEGORY_ID =
            "category_id";


    @DataType(DataType.Type.INTEGER)
    @PrimaryKey
    @References(table = EventDatabase.REGIONS,
            column = RegionsColumns._ID)
    String REGION_ID =
            "region_id";


    //in Julian days so we can compare
    @DataType(DataType.Type.REAL)
    @NotNull
    @References(table = EventDatabase.REGIONS,
            column = RegionsColumns.ADDED_DATE_TIME)
    String ADDED_DATE_TIME =
            "added_date_time";

}
