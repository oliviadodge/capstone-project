package com.example.olivi.maphap.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

/**
 * Created by olivi on 11/30/2015.
 */
public interface RegionsColumns {

    @DataType(DataType.Type.INTEGER) @PrimaryKey
    @AutoIncrement
    public static final String _ID =
            "_id";

    @DataType(DataType.Type.REAL)
    @NotNull
    public static final String LATITUDE =
            "latitude";

    @DataType(DataType.Type.REAL)
    @NotNull
    public static final String LONGITUDE =
            "longitude";

    @DataType(DataType.Type.INTEGER)
    @NotNull
    public static final String RADIUS =
            "radius";


    @DataType(DataType.Type.TEXT)
    String ADDED_DATE_TIME =
            "added_date_time";

    @DataType(DataType.Type.TEXT)
    String CUTOFF_DATE_TIME =
            "cutoff_date_time";
}
