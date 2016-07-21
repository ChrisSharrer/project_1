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
package com.lc23.android.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.lc23.android.popularmovies.data.MovieContract.MovieEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FetchMovieTask extends AsyncTask<String, Void, Void> {

    private final String LOG_TAG = FetchMovieTask.class.getSimpleName();
    private final Context context;

    public FetchMovieTask(Context context) {
        this.context = context;
    }

    private static final String movieDbDateFormat = "yyyy-MM-dd";

    private static final String RESULTS = "results";

    private static final String MOVIE_ID = "id";
    private static final String ORIGINAL_TITLE = "original_title";
    private static final String POSTER_PATH = "poster_path";
    private static final String OVERVIEW = "overview";
    private static final String RELEASE_DATE = "release_date"; // yyyy-MM-dd format
    private static final String VOTE_AVERAGE = "vote_average"; // double 0.00 to 10.00;

    private void getMovieDataFromJson(String movieJsonString, String sort) throws JSONException {
        // To parse release date
        DateFormat dateFormat = new SimpleDateFormat(movieDbDateFormat);

        try {
            JSONObject moviePageJson = new JSONObject(movieJsonString);
            JSONArray movieArray = moviePageJson.getJSONArray(RESULTS);

            // Insert the new weather information into the database
            List<ContentValues> valuesList = new ArrayList<>(movieArray.length());

            for (int i = 0; i < movieArray.length(); i++) {

                // Get the JSON object representing a movie
                JSONObject movieJson = movieArray.getJSONObject(i);

                final int movieID = movieJson.getInt(MOVIE_ID);
                final String title = movieJson.getString(ORIGINAL_TITLE);
                final String imagePath = movieJson.getString(POSTER_PATH);
                final String plot = movieJson.getString(OVERVIEW);
                final double rating = movieJson.getDouble(VOTE_AVERAGE);

                Date releaseDate = null;
                String releaseDateString = movieJson.getString(RELEASE_DATE);
                if (releaseDateString != null && releaseDateString.length() > 0) {
                    try {
                        releaseDate = dateFormat.parse(releaseDateString);
                    }
                    catch (ParseException e) {
                        Log.e(LOG_TAG, e.getMessage(), e);
                    }
                }

                ContentValues movieValues = new ContentValues();

                movieValues.put(MovieEntry.COLUMN_MOVIE_ID, movieID);
                movieValues.put(MovieEntry.COLUMN_ORIGINAL_TITLE, title);
                movieValues.put(MovieEntry.COLUMN_POSTER_THUMBNAIL, imagePath);
                movieValues.put(MovieEntry.COLUMN_PLOT, plot);
                movieValues.put(MovieEntry.COLUMN_USER_RATING, rating);
                movieValues.put(MovieEntry.COLUMN_RELEASE_DATE, releaseDate.getTime());
                movieValues.put(MovieEntry.COLUMN_SORT, sort);

                valuesList.add(movieValues);
            }

            int inserted = 0;
            int deleted = 0;
            // add to database
            if (valuesList.size() > 0) {
                // Clear old data
                deleted = context.getContentResolver().delete(MovieEntry.CONTENT_URI, MovieEntry.COLUMN_SORT + " = ?", new String[] { sort });

                // Add new data
                inserted = context.getContentResolver().bulkInsert(MovieEntry.CONTENT_URI, valuesList.toArray(new ContentValues[valuesList.size()]));
            }
            Log.d(LOG_TAG, String.format("Sort %s: deleted %d, inserted %d", sort, deleted, inserted));
        }
        catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    @Override
    protected Void doInBackground(String... params) {
        for (String sort : params)
            loadMovies(sort);

        return null;
    }

    private void loadMovies(String sort) {
        HttpURLConnection urlConnection = null;

        try {
            final String MOVIE_API_BASE_URL = "https://api.themoviedb.org/3/movie/";
            final String APP_KEY_PARAM = "api_key";

            Uri builtUri = Uri.parse(MOVIE_API_BASE_URL).buildUpon()
                    .appendEncodedPath(sort) // popular or top_rated
                    .appendQueryParameter(APP_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                    .build();

            URL url = new URL(builtUri.toString());

            // Create the request and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Get the JSON data
            String movieJsonString = readJsonData(urlConnection);

            // Parse the data, put it in the database
            if (movieJsonString != null)
                getMovieDataFromJson(movieJsonString, sort);
        }
        catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            // Incomplete or bad data, do not process
        }
        catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
    }

    private String readJsonData(URLConnection urlConnection) throws IOException {
        InputStream inputStream = urlConnection.getInputStream();

        // If no connection, nothing to do
        if (inputStream == null)
            return null;

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder buffer = new StringBuilder();
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append('\n');
            }

            // If no data, nothing to process
            if (buffer.length() == 0)
                return null;

            return buffer.toString();
        }
        finally {
            reader.close();
        }
    }
}