package com.example.olivi.maphap.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.NotifyBulkInsert;
import net.simonvt.schematic.annotation.NotifyDelete;
import net.simonvt.schematic.annotation.TableEndpoint;


@ContentProvider(authority = EventProvider.AUTHORITY, database = EventDatabase.class)
public final class EventProvider {
    public static final String AUTHORITY =
            "com.example.olivi.maphap.data.EventProvider";

    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    interface Path{
        String EVENTS = "events";
        String REGIONS = "regions";
        String VENUES = "venues";
        String EVENTS_REGIONS = "events_regions";
    }

    private static Uri buildUri(String ... paths){
        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
        for (String path : paths){
            builder.appendPath(path);
        }
        return builder.build();
    }


    @TableEndpoint(table = EventDatabase.EVENTS) public static class Events {

        @ContentUri(
                path = Path.EVENTS,
                type = "vnd.android.cursor.dir/event",
                defaultSort = EventsColumns._ID + " ASC")
        public static final Uri CONTENT_URI = buildUri(Path.EVENTS);


        @InexactContentUri(
                name = "EVENTS_WITH_REGION",
                path = Path.EVENTS + "/" + Path.REGIONS + "/#",
                type= "vnd.android.cursor.dir/event",
                whereColumn = EventsAndRegionsColumns.REGION_ID,
                pathSegment = 2,
                join = "INNER JOIN " + EventDatabase.EVENTS_REGIONS + " ON "
                + EventDatabase.EVENTS + "." + EventsColumns.EB_ID
                + " = "
                + EventDatabase.EVENTS_REGIONS + "." + EventsAndRegionsColumns.EVENT_ID
                + " INNER JOIN " + EventDatabase.VENUES + " ON "
                + EventDatabase.EVENTS + "." + EventsColumns.EVENTBRITE_VENUE_ID
                + " = "
                + EventDatabase.VENUES + "." + VenuesColumns.EB_VENUE_ID

        )

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
                join = EventDatabase.VENUES)

        public static Uri withId(long id){
            return buildUri(Path.EVENTS, String.valueOf(id));
        }
//
//        @NotifyDelete(paths = Path.EVENTS + "/#") public static Uri[] onDelete(Context context,
//                                                                              Uri uri) {
//            final long noteId = Long.valueOf(uri.getPathSegments().get(1));
//            Cursor c = context.getContentResolver().query(uri, null, null, null, null);
//            c.moveToFirst();
//            final long listId = c.getLong(c.getColumnIndex(EventsColumns.LIST_ID));
//            c.close();
//
//            return new Uri[] {
//                    withId(noteId), fromList(listId), Lists.withId(listId),
//            };
//        }
    }

    @TableEndpoint(table = EventDatabase.VENUES) public static class Venues{
        @ContentUri(
                path = Path.VENUES,
                type = "vnd.android.cursor.dir/venue",
                defaultSort = VenuesColumns._ID + " ASC"
        )
        public static final Uri CONTENT_URI = buildUri(Path.VENUES);
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
    }

    @TableEndpoint(table = EventDatabase.REGIONS) public static class Regions{
        @ContentUri(
                path = Path.REGIONS,
                type = "vnd.android.cursor.dir/region",
                defaultSort = RegionsColumns._ID + " ASC"
        )
        public static final Uri CONTENT_URI = buildUri(Path.REGIONS);

        @InexactContentUri(
                name = "REGION_ID",
                path = Path.REGIONS + "/#",
                type = "vnd.android.cursor.item/region",
                whereColumn = RegionsColumns._ID,
                pathSegment = 1
        )
        public static Uri withId(long id){
            return buildUri(Path.REGIONS, String.valueOf(id));
        }
    }


    @TableEndpoint(table = EventDatabase.EVENTS_REGIONS) public static class EventsAndRegions{
        @ContentUri(
                path = Path.EVENTS_REGIONS,
                type = "vnd.android.cursor.dir/event_region",
                defaultSort = EventsAndRegionsColumns.EVENT_ID + " ASC"
        )
        public static final Uri CONTENT_URI = buildUri(Path.EVENTS_REGIONS);
//
//        @InexactContentUri(
//                name = "REGION_ID",
//                path = Path.REGIONS + "/#",
//                type = "vnd.android.cursor.item/region",
//                whereColumn = RegionColumns._ID,
//                pathSegment = 1
//        )
//        public static Uri withId(long id){
//            return buildUri(Path.REGIONES, String.valueOf(id));
//        }
    }
}

