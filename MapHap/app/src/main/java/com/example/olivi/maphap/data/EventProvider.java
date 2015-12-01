package com.example.olivi.maphap.data;

import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;


@ContentProvider(authority = EventProvider.AUTHORITY, database = EventDatabase.class)
public final class EventProvider {
    public static final String AUTHORITY =
            "com.example.olivi.maphap.data.EventProvider";
    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    interface Path{
        String EVENTS = "events";
        String REGIONS = "regions";
        String SEARCHES = "searches";
        String VENUES="venues";
    }

    private static Uri buildUri(String ... paths){
        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
        for (String path : paths){
            builder.appendPath(path);
        }
        return builder.build();
    }


    @TableEndpoint(table = EventDatabase.Tables.EVENTS) public static class Events {
        @InexactContentUri(
                name = "EVENTS_WITH_REGION",
                path = Path.EVENTS + "/" + Path.REGIONS + "/#",
                type= "vnd.android.cursor.dir/event",
                whereColumn = EventsAndRegionsColumns.REGION_ID,
                pathSegment = 1,
                join = EventDatabase.Tables.EVENTS_REGIONS)

        public static Uri withRegionId(long id){
            return buildUri(Path.EVENTS, Path.REGIONS, String.valueOf(id));
        }

//        public static final Uri CONTENT_URI = buildUri(Path.EVENTS);

        @InexactContentUri(
                name = "EVENT_ID",
                path = Path.EVENTS + "/#",
                type = "vnd.android.cursor.item/event",
                whereColumn = EventsColumns._ID,
                pathSegment = 1,
                join = EventDatabase.Tables.VENUES)

        public static Uri withId(long id){
            return buildUri(Path.EVENTS, String.valueOf(id));
        }
    }
//
//    @TableEndpoint(table = EventDatabase.VENUES) public static class ArchivedPlanets{
//        @ContentUri(
//                path = Path.ARCHIVED_PLANETS,
//                type = "vnd.android.cursor.dir/archived_planet",
//                defaultSort = VenuesColumns.DIST_FROM_SUN + " ASC"
//        )
//        public static final Uri CONTENT_URI = buildUri(Path.ARCHIVED_PLANETS);
//
//        @InexactContentUri(
//                name = "ARCHIVED_PLANET_ID",
//                path = Path.ARCHIVED_PLANETS + "/#",
//                type = "vnd.android.cursor.item/archived_planet",
//                whereColumn = VenuesColumns._ID,
//                pathSegment = 1
//        )
//        public static Uri withId(long id){
//            return buildUri(Path.ARCHIVED_PLANETS, String.valueOf(id));
//        }
//    }
}
