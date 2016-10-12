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
import android.net.Uri;
import android.provider.BaseColumns;

import static android.content.ContentUris.withAppendedId;

public class MovieContract {

    public static final String CONTENT_AUTHORITY = "com.lc23.android.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIE = "movie";
    public static final String PATH_MOVIE_DETAIL = "movie_detail";
    public static final String PATH_MOVIE_LINKS = "movie_links";
    public static final String PATH_MOVIE_TYPES = "movie_types";

    public static final class MovieEntry implements BaseColumns {
        public static final String TABLE_NAME = "movie";

        // Movie DB id stored as an int
        public static final String COLUMN_MOVIE_DB_ID = "movie_db_id";
        // Movie title stored as a string
        public static final String COLUMN_TITLE = "title";
        // Path to image stored as a string
        public static final String COLUMN_POSTER_THUMBNAIL = "poster_thumb_path";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        public static Uri buildMovieUri(long id) {
            return withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildMovieForTypeUri(String movieType) {
            return CONTENT_URI.buildUpon().appendQueryParameter(MovieTypeEntry.COLUMN_TYPE, movieType).build();
        }

        public static String getMovieTypeFromUri(Uri uri) {
            return uri.getQueryParameter(MovieTypeEntry.COLUMN_TYPE);
        }
    }

    public static final class MovieTypeEntry implements BaseColumns {
        public static final String TABLE_NAME = "movie_type";

        // Movie DB id stored as an int
        public static final String COLUMN_MOVIE_DB_ID = "movie_db_id";
        // Movie type stored as a String
        public static final String COLUMN_TYPE = "movie_type";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE_TYPES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE_TYPES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE_TYPES;

        public static Uri buildMovieTypeUri(long id) {
            return withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildMovieTypeUri(String movieType, int movieId) {
            return withAppendedId(Uri.withAppendedPath(CONTENT_URI, movieType), movieId);
        }

        public static String getTypeFromUri(Uri uri) {
            return uri.getPathSegments().get(1);

        }

        public static int getMovieIdFromUri(Uri uri) {
            String idString = uri.getPathSegments().get(2);
            if (idString == null) {
                throw new RuntimeException("No id in URI");
            }
            return Integer.parseInt(idString);
        }

    }

    public static final class MovieDetailsEntry implements BaseColumns {
        public static final String TABLE_NAME = "movie_detail";

        // Movie id stored as an int
        public static final String COLUMN_MOVIE_DB_ID = "movie_db_id";
        // Movie title stored as a string
        public static final String COLUMN_TITLE = "title";
        // Path to image stored as a string
        public static final String COLUMN_POSTER_THUMBNAIL = "poster_thumb_path";
        // Plot synopsis (overview) stored as a string
        public static final String COLUMN_PLOT = "overview";
        // User rating stored as a float
        public static final String COLUMN_USER_RATING = "vote_average";
        // Release date stored as a long
        public static final String COLUMN_RELEASE_DATE = "release_date";
        // Runtime in minutes stored as an int
        public static final String COLUMN_RUNTIME = "runtime";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE_DETAIL).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE_DETAIL;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE_DETAIL;

        public static Uri buildMovieDetailsUri(long id) {
            return withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildMovieDetailsForMovieUri(int id) {
            return withAppendedId(Uri.withAppendedPath(CONTENT_URI, "for_movie"), id);
        }

        public static int getMovieIdFromUri(Uri uri) {
            String idString = uri.getPathSegments().get(2);
            if (idString == null) {
                throw new RuntimeException("No id in URI");
            }
            return Integer.parseInt(idString);
        }

    }

    public static final class MovieLinksEntry implements BaseColumns {
        public static final String TABLE_NAME = "movie_links";

        // Movie id stored as an int
        public static final String COLUMN_MOVIE_DB_ID = "movie_db_id";
        // Link type stored as a string
        public static final String COLUMN_LINK_TYPE = "link_type";
        // URI stored as a string
        public static final String COLUMN_URI = "uri";
        // Link text as a string
        public static final String COLUMN_LINK_TEXT = "link_text";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE_LINKS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE_LINKS + "/";
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE_LINKS;

        public static Uri buildMovieLinksUri(long id) {
            return withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildMovieLinksUri(int movieId, String linkType) {
            return withAppendedId(Uri.withAppendedPath(CONTENT_URI, linkType), movieId);
        }

        public static String getLinkTypeFromUri(Uri uri) {
            return uri.getPathSegments().get(1);

        }
        public static int getMovieIdFromUri(Uri uri) {
            String idString = uri.getPathSegments().get(2);
            if (idString == null) {
                throw new RuntimeException("No id in URI");
            }
            return Integer.parseInt(idString);
        }

    }
}
