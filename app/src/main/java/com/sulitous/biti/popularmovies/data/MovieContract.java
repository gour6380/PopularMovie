package com.sulitous.biti.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class MovieContract {

     static final String CONTENT_AUTHORITY = "com.sulitous.biti.popularmovies";

     private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

     static final String PATH_MOVIES = "movies";

     public static final class MovieEntry implements BaseColumns{

         public static final String TABLE_NAME = "movies";

         public static final String COLUMN_TITLE = "title";
         public static final String COLUMN_POSTER = "poster";
         public static final String COLUMN_SYNOPSIS = "synopsis";
         public static final String COLUMN_USER_RATING = "user_rating";
         public static final String COLUMN_RELEASE_DATE = "release_date";
         public static final String COLUMN_FAVORITE = "favorite";

         public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                 .appendPath(TABLE_NAME).build();

         static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TABLE_NAME;
         static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TABLE_NAME;

         public static Uri buildMovieUri(long id){
             return ContentUris.withAppendedId(CONTENT_URI, id);
         }
     }
}
