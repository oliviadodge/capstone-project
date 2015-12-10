package com.example.olivi.maphap.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.ConflictResolutionType;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;
import net.simonvt.schematic.annotation.Unique;

public interface VenuesColumns {

    @DataType(DataType.Type.INTEGER) @PrimaryKey
    @AutoIncrement
    public static final String _ID =
            "_id";

    @DataType(DataType.Type.TEXT)
    public static final String NAME =
            "venue_name";

    @DataType(DataType.Type.TEXT) @NotNull
    @Unique(onConflict = ConflictResolutionType.REPLACE)
    public static final String EB_VENUE_ID =
            "eb_venue_id";

    @DataType(DataType.Type.REAL) @NotNull
    public static final String LATITUDE =
            "venue_latitude";

    @DataType(DataType.Type.REAL) @NotNull
    public static final String LONGITUDE =
            "venue_longitude";
}

