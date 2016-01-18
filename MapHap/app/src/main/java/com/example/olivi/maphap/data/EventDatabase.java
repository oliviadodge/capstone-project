package com.example.olivi.maphap.data;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;


@Database(version = EventDatabase.VERSION)
public final class EventDatabase {
    private EventDatabase(){}

    public static final int VERSION = 1;

    @Table(EventsColumns.class)
    public static final String EVENTS =
            "events";

    @Table(VenuesColumns.class)
    public static final String VENUES =
            "venues";

    @Table(RegionsColumns.class)
    public static final String REGIONS =
            "regions";

    @Table(EventsAndRegionsColumns.class)
    public static final String EVENTS_REGIONS =
            "events_regions";

    @Table(CategoriesAndRegionsColumns.class)
    public static final String CATEGORIES_REGIONS =
            "categories_regions";
}
