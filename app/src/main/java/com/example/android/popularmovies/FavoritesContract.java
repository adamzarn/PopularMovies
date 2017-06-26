package com.example.android.popularmovies;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by adamzarn on 6/26/17.
 */

public class FavoritesContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_FAVORITES = "favorites";

    public static final class FavoritesEntry implements BaseColumns {

            public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                    .appendPath(PATH_FAVORITES)
                    .build();

            public static final String TABLE_NAME = "favorites";

            public static final String COLUMN_ID = "id";
            public static final String COLUMN_TITLE = "title";
            public static final String COLUMN_RELEASE_DATE = "release_date";
            public static final String COLUMN_VOTE_AVERAGE = "vote_average";
            public static final String COLUMN_PLOT_SYNOPSIS = "plot_synopsis";
            public static final String COLUMN_POSTER = "poster";

    }

}
