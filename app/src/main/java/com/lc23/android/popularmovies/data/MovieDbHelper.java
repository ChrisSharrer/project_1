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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.lc23.android.popularmovies.data.MovieContract.MovieDetailsEntry;
import com.lc23.android.popularmovies.data.MovieContract.MovieEntry;
import com.lc23.android.popularmovies.data.MovieContract.MovieLinksEntry;
import com.lc23.android.popularmovies.data.MovieContract.MovieTypeEntry;

public class MovieDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 20;

    static final String DATABASE_NAME = "popularmovies.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +

                MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                MovieEntry.COLUMN_MOVIE_DB_ID + " INTEGER KEY NOT NULL, " +
                MovieEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_POSTER_THUMBNAIL + " TEXT NOT NULL, " +

                "UNIQUE (" + MovieEntry.COLUMN_MOVIE_DB_ID + ") ON CONFLICT REPLACE " +

                ");";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);

        final String SQL_CREATE_MOVIE_TYPE_TABLE = "CREATE TABLE " + MovieTypeEntry.TABLE_NAME + " (" +

                MovieTypeEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                MovieTypeEntry.COLUMN_MOVIE_DB_ID + " INTEGER NOT NULL, " +
                MovieTypeEntry.COLUMN_TYPE + " STRING NOT NULL, " +


                // The movie key is a foreign key to the movie table
                "FOREIGN KEY (" + MovieDetailsEntry.COLUMN_MOVIE_DB_ID + ") REFERENCES " + MovieEntry.TABLE_NAME + "( " + MovieEntry.COLUMN_MOVIE_DB_ID + "), " +

                // One record per movie/type
                "UNIQUE (" + MovieTypeEntry.COLUMN_MOVIE_DB_ID + ", " + MovieTypeEntry.COLUMN_TYPE + ") ON CONFLICT REPLACE " +

                ");";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TYPE_TABLE);

        final String SQL_CREATE_MOVIE_DETAIL_TABLE = "CREATE TABLE " + MovieDetailsEntry.TABLE_NAME + " (" +

                MovieDetailsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                MovieDetailsEntry.COLUMN_MOVIE_DB_ID + " INTEGER NOT NULL, " +
                MovieDetailsEntry.COLUMN_POSTER_THUMBNAIL + " TEXT NOT NULL, " +
                MovieDetailsEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                MovieDetailsEntry.COLUMN_PLOT + " TEXT NOT NULL, " +
                MovieDetailsEntry.COLUMN_USER_RATING + " REAL NOT NULL, " +
                MovieDetailsEntry.COLUMN_RELEASE_DATE + " INTEGER NOT NULL, " +
                MovieDetailsEntry.COLUMN_RUNTIME + " INTEGER NOT NULL, " +

                // The movie key is a foreign key to the movie table
                "FOREIGN KEY (" + MovieDetailsEntry.COLUMN_MOVIE_DB_ID + ") REFERENCES " + MovieEntry.TABLE_NAME + "( " + MovieEntry.COLUMN_MOVIE_DB_ID + "), " +

                // One record per movie
                "UNIQUE (" + MovieDetailsEntry.COLUMN_MOVIE_DB_ID + ") ON CONFLICT REPLACE " +

                ");";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_DETAIL_TABLE);


        final String SQL_CREATE_MOVIE_LINK_TABLE = "CREATE TABLE " + MovieLinksEntry.TABLE_NAME + " (" +

                MovieLinksEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                MovieLinksEntry.COLUMN_MOVIE_DB_ID + " INTEGER NOT NULL, " +
                MovieLinksEntry.COLUMN_LINK_TYPE + " STRING NOT NULL, " +
                MovieLinksEntry.COLUMN_URI + " TEXT NOT NULL, " +
                MovieLinksEntry.COLUMN_LINK_TEXT + " TEXT NOT NULL, " +

                // The movie key is a foreign key to the movie table
                "FOREIGN KEY (" + MovieLinksEntry.COLUMN_MOVIE_DB_ID + ") REFERENCES " + MovieEntry.TABLE_NAME + "( " + MovieEntry.COLUMN_MOVIE_DB_ID + "), " +

                // One record per movie per uri
                "UNIQUE (" + MovieLinksEntry.COLUMN_MOVIE_DB_ID + ", " + MovieLinksEntry.COLUMN_URI + ") ON CONFLICT REPLACE " +

                ");";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_LINK_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieTypeEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieDetailsEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieLinksEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
