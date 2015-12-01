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
}
