/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lc23.android.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for the weather database.
 */
public class MovieContract {

    public static final String CONTENT_AUTHORITY = "com.lc23.android.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


    public static final String PATH_MOVIE = "movie";

    public static final class MovieEntry implements BaseColumns {
        public static final String TABLE_NAME = "movie";

        // Movie DB id stored as an int
        public static final String COLUMN_MOVIE_ID = "movie_db_id";
        // Movie title stored as a string
        public static final String COLUMN_ORIGINAL_TITLE = "original_title";
        // Path to image stored as a string
        public static final String COLUMN_POSTER_THUMBNAIL = "poster_thumb_path";
        // Plot synopsis (overview) stored as a string
        public static final String COLUMN_PLOT = "overview";
        // User rating stored as a float
        public static final String COLUMN_USER_RATING= "vote_average";
        // Release date stored as a long
        public static final String COLUMN_RELEASE_DATE= "release_date";
        // Sort type stored as a String
        public static final String COLUMN_SORT = "sort_type";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static Uri buildMovieForSortUri(String sort) {
            return CONTENT_URI.buildUpon().appendQueryParameter(COLUMN_SORT, sort).build();
        }

        public static String getSortFromUri(Uri uri) {
            String sort = uri.getQueryParameter(COLUMN_SORT);
            return sort;
        }
    }
}
