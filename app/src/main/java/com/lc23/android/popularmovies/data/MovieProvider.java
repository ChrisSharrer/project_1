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

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.lc23.android.popularmovies.data.MovieContract.MovieDetailsEntry;
import com.lc23.android.popularmovies.data.MovieContract.MovieEntry;
import com.lc23.android.popularmovies.data.MovieContract.MovieLinksEntry;
import com.lc23.android.popularmovies.data.MovieContract.MovieTypeEntry;

public class MovieProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mOpenHelper;

    static final int MOVIE = 100;
    static final int MOVIE_WITH_ID = 101;
    static final int MOVIE_DETAILS = 200;
    static final int MOVIE_DETAILS_WITH_ID = 201;
    static final int MOVIE_DETAILS_WITH_MOVIE = 202;
    static final int MOVIE_LINKS = 300;
    static final int MOVIE_LINKS_WITH_ID = 301;
    static final int MOVIE_LINKS_WITH_TYPE_AND_MOVIE = 302;
    static final int MOVIE_TYPE = 400;
    static final int MOVIE_TYPE_WITH_ID = 401;
    static final int MOVIE_TYPE_WITH_TYPE_AND_MOVIE = 402;

    private static final SQLiteQueryBuilder moviesByTypeQueryBuilder;
    static {
        moviesByTypeQueryBuilder = new SQLiteQueryBuilder();

        moviesByTypeQueryBuilder.setTables(
                MovieEntry.TABLE_NAME + " INNER JOIN " +
                        MovieTypeEntry.TABLE_NAME +
                        " ON " + MovieEntry.TABLE_NAME +
                        "." + MovieEntry.COLUMN_MOVIE_DB_ID +
                        " = " + MovieTypeEntry.TABLE_NAME +
                        "." + MovieTypeEntry.COLUMN_MOVIE_DB_ID);
    }

    private static final SQLiteQueryBuilder MOVIE_QUERY_BUILDER;
    static {
        MOVIE_QUERY_BUILDER = new SQLiteQueryBuilder();

        MOVIE_QUERY_BUILDER.setTables(MovieEntry.TABLE_NAME);
    }

    private static final SQLiteQueryBuilder MOVIE_DETAILS_QUERY_BUILDER;
    static {
        MOVIE_DETAILS_QUERY_BUILDER = new SQLiteQueryBuilder();

        MOVIE_DETAILS_QUERY_BUILDER.setTables(MovieDetailsEntry.TABLE_NAME);
    }


    private static final SQLiteQueryBuilder MOVIE_LINKS_QUERY_BUILDER;
    static {
        MOVIE_LINKS_QUERY_BUILDER = new SQLiteQueryBuilder();

        MOVIE_LINKS_QUERY_BUILDER.setTables(MovieLinksEntry.TABLE_NAME);
    }

    private static final SQLiteQueryBuilder MOVIE_TYPE_QUERY_BUILDER;
    static {
        MOVIE_TYPE_QUERY_BUILDER= new SQLiteQueryBuilder();

        MOVIE_TYPE_QUERY_BUILDER.setTables(MovieTypeEntry.TABLE_NAME);
    }

    private Cursor getMovies(Uri uri, String[] projection, String sortOrder) {
        String selection = null;
        String[] selectionArgs = null;

        String type = MovieEntry.getMovieTypeFromUri(uri);
        if (type != null) {
            selection = MovieContract.MovieTypeEntry.COLUMN_TYPE + " = ?";
            selectionArgs = new String[] { type };
        }
        return moviesByTypeQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getMovieDetailsByMovie(Uri uri, String[] projection) {
        int movieId = MovieContract.MovieDetailsEntry.getMovieIdFromUri(uri);

        return MOVIE_DETAILS_QUERY_BUILDER.query(mOpenHelper.getReadableDatabase(),
                projection,
                MovieDetailsEntry.COLUMN_MOVIE_DB_ID + " = ?",
                new String[] { Integer.toString(movieId) },
                null,
                null,
                null
        );
    }

    private Cursor getMovieLinksByTypeAndMovie(Uri uri, String[] projection) {
        String linkType = MovieLinksEntry.getLinkTypeFromUri(uri);
        int movieId = MovieContract.MovieLinksEntry.getMovieIdFromUri(uri);

        return MOVIE_LINKS_QUERY_BUILDER.query(mOpenHelper.getReadableDatabase(),
                projection,
                MovieLinksEntry.COLUMN_MOVIE_DB_ID + " = ? AND " + MovieLinksEntry.COLUMN_LINK_TYPE + " = ?",
                new String[] { Integer.toString(movieId), linkType },
                null,
                null,
                null
        );
    }


    private Cursor getMovieTypeByTypeAndMovie(Uri uri, String[] projection) {
        String type = MovieTypeEntry.getTypeFromUri(uri);
        int movieId = MovieTypeEntry.getMovieIdFromUri(uri);

        return MOVIE_TYPE_QUERY_BUILDER.query(mOpenHelper.getReadableDatabase(),
                projection,
                MovieTypeEntry.COLUMN_MOVIE_DB_ID + " = ? AND " + MovieTypeEntry.COLUMN_TYPE + " = ?",
                new String[] { Integer.toString(movieId), type },
                null,
                null,
                null
        );
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        matcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIE, MOVIE);
        matcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIE + "/#", MOVIE_WITH_ID);

        matcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIE_DETAIL, MOVIE_DETAILS);
        matcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIE_DETAIL + "/#", MOVIE_DETAILS_WITH_ID);
        matcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIE_DETAIL + "/for_movie/#", MOVIE_DETAILS_WITH_MOVIE);

        matcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIE_LINKS, MOVIE_LINKS);
        matcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIE_LINKS +"/#", MOVIE_LINKS_WITH_ID);
        matcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIE_LINKS + "/*/#", MOVIE_LINKS_WITH_TYPE_AND_MOVIE);

        matcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIE_TYPES, MOVIE_TYPE);
        matcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIE_TYPES + "/#", MOVIE_TYPE_WITH_ID);
        matcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIE_TYPES + "/*/#", MOVIE_TYPE_WITH_TYPE_AND_MOVIE);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIE:
                return MovieEntry.CONTENT_TYPE;
            case MOVIE_WITH_ID:
                return MovieEntry.CONTENT_ITEM_TYPE;
            case MOVIE_DETAILS:
                return MovieDetailsEntry.CONTENT_TYPE;
            case MOVIE_DETAILS_WITH_ID:
                return MovieDetailsEntry.CONTENT_ITEM_TYPE;
            case MOVIE_DETAILS_WITH_MOVIE:
                return MovieDetailsEntry.CONTENT_ITEM_TYPE;
            case MOVIE_LINKS:
                return MovieLinksEntry.CONTENT_TYPE;
            case MOVIE_LINKS_WITH_ID:
                return MovieLinksEntry.CONTENT_ITEM_TYPE;
            case MOVIE_LINKS_WITH_TYPE_AND_MOVIE:
                return MovieLinksEntry.CONTENT_ITEM_TYPE;
            case MOVIE_TYPE:
                return MovieTypeEntry.CONTENT_TYPE;
            case MOVIE_TYPE_WITH_ID:
                return MovieTypeEntry.CONTENT_ITEM_TYPE;
            case MOVIE_TYPE_WITH_TYPE_AND_MOVIE:
                return MovieTypeEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case MOVIE: {
                retCursor = getMovies(uri, projection, sortOrder);
                break;
            }
            case MOVIE_DETAILS_WITH_MOVIE: {
                retCursor = getMovieDetailsByMovie(uri, projection);
                break;
            }
            case MOVIE_LINKS_WITH_TYPE_AND_MOVIE: {
                retCursor = getMovieLinksByTypeAndMovie(uri, projection);
                break;
            }
            case MOVIE_TYPE_WITH_TYPE_AND_MOVIE: {
                retCursor = getMovieTypeByTypeAndMovie(uri, projection);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    /*
        Student: Add the ability to insert Locations to the implementation of this function.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIE: {
                long id = db.insert(MovieEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = MovieEntry.buildMovieUri(id);
                }
                else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case MOVIE_DETAILS: {
                long id = db.insert(MovieDetailsEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = MovieDetailsEntry.buildMovieDetailsUri(id);
                }
                else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case MOVIE_LINKS: {
                long id = db.insert(MovieLinksEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = MovieLinksEntry.buildMovieLinksUri(id);
                }
                else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case MOVIE_TYPE: {
                long id = db.insert(MovieTypeEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = MovieTypeEntry.buildMovieTypeUri(id);
                }
                else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        if (selection == null) selection = "1";
        switch (match) {
            case MOVIE: {
                rowsDeleted = db.delete(MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case MOVIE_DETAILS: {
                rowsDeleted = db.delete(MovieDetailsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case MOVIE_LINKS: {
                rowsDeleted = db.delete(MovieLinksEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case MOVIE_TYPE: {
                rowsDeleted = db.delete(MovieTypeEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsDeleted != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return rowsDeleted;
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case MOVIE: {
                rowsUpdated = db.update(MovieEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case MOVIE_DETAILS: {
                rowsUpdated = db.update(MovieDetailsEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case MOVIE_LINKS: {
                rowsUpdated = db.update(MovieLinksEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsUpdated != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] valuesArray) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount = 0;
        switch (match) {
            case MOVIE:
                db.beginTransaction();
                try {
                    for (ContentValues values : valuesArray) {
                        long _id = db.insert(MovieEntry.TABLE_NAME, null, values);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                }
                finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;

            case MOVIE_DETAILS:
                db.beginTransaction();
                try {
                    for (ContentValues values : valuesArray) {
                        long _id = db.insert(MovieDetailsEntry.TABLE_NAME, null, values);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                }
                finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;

            case MOVIE_LINKS:
                db.beginTransaction();
                try {
                    for (ContentValues values : valuesArray) {
                        long _id = db.insert(MovieLinksEntry.TABLE_NAME, null, values);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                }
                finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;

            case MOVIE_TYPE:
                db.beginTransaction();
                try {
                    for (ContentValues values : valuesArray) {
                        long _id = db.insert(MovieTypeEntry.TABLE_NAME, null, values);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                }
                finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;

            default:
                return super.bulkInsert(uri, valuesArray);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}