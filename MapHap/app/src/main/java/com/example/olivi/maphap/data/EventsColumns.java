package com.example.olivi.maphap.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.ConflictResolutionType;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;
import net.simonvt.schematic.annotation.References;
import net.simonvt.schematic.annotation.Unique;


public interface EventsColumns {

    @DataType(DataType.Type.INTEGER) @PrimaryKey
    @AutoIncrement
    public static final String _ID =
            "_id";

    @DataType(DataType.Type.INTEGER)
    @References(table = EventDatabase.Tables.VENUES, column = VenuesColumns.EVENTBRITE_VENUE_ID)
    public static final String EVENTBRITE_VENUE_ID =
            "eb_venue_id";

    @DataType(DataType.Type.TEXT) @NotNull
    public static final String NAME =
            "event_name";

    @DataType(DataType.Type.TEXT) @NotNull
    @Unique(onConflict = ConflictResolutionType.REPLACE)
    public static final String EB_ID =
            "eventbride_id";

    @DataType(DataType.Type.TEXT)
    public static final String DESCRIPTION = "description";


    @DataType(DataType.Type.TEXT)
    public static final String URL =
            "event_url";

    @DataType(DataType.Type.TEXT) @NotNull
    public static final String START_DATE_TIME =
            "start_date_time";

    @DataType(DataType.Type.TEXT) @NotNull
    public static final String END_DATE_TIME =
            "end_date_time";

    @DataType(DataType.Type.INTEGER)
    public static final String CAPACITY =
            "capacity";

    @DataType(DataType.Type.TEXT) @NotNull
    public static final String STATUS =
            "status";

    @DataType(DataType.Type.TEXT)
    public static final String LOGO_URL =
            "logo_url";

    @DataType(DataType.Type.TEXT)
    public static final String CATEGORY =
            "category";
}

