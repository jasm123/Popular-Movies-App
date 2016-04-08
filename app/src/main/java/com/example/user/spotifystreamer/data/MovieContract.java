package com.example.user.spotifystreamer.data;

import android.provider.BaseColumns;

/**
 * The MovieContract class contains all the information about
 * related URIs and columns displayed in UI.
 */
public class MovieContract {
    public static final class MovieEntry implements BaseColumns{
        public static final String TABLE_NAME="Movies";
        //ID OF THE MOVIE
        public static final String COLUMN_MID ="movie_id";
        //TITLE OF THE MOVIE SELECTED
        public static final String COLUMN_TITLE ="title";
        //POSTER PATH
        public static final String COLUMN_POSTER ="poster_path";
        //THUMBNAIL IMAGE PATH DISPLAYED IN DETAILS VIEW
        public static final String COLUMN_BACKDROP="backdrop";
        //RATING OF THE MOVIE
        public static final String COLUMN_RATING ="rating";
        //RELEASE DATE
        public static final String COLUMN_DATE ="date";
        //OVERVIEW OF THE MOVIE
        public static final String COLUMN_OVERVIEW ="plot";
    }


}
