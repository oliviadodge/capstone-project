package com.example.olivi.maphap.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.ConflictResolutionType;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;
import net.simonvt.schematic.annotation.Unique;

/**
 * Created by olivi on 11/30/2015.
 */
public interface SearchColumns {

    @DataType(DataType.Type.INTEGER) @PrimaryKey
    @AutoIncrement
    public static final String _ID =
            "_id";

    @DataType(DataType.Type.TEXT) @NotNull
    @Unique(onConflict = ConflictResolutionType.IGNORE)
    public static final String SEARCH_TERM = "search_term";

}
