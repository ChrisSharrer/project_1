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
import android.widget.Toast;

import com.lc23.android.popularmovies.data.MovieContract.MovieDetailsEntry;
import com.lc23.android.popularmovies.data.MovieContract.MovieEntry;
import com.lc23.android.popularmovies.data.MovieContract.MovieLinksEntry;
import com.lc23.android.popularmovies.data.MovieContract.MovieTypeEntry;
import com.lc23.android.popularmovies.utility.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FetchMovieTask extends AsyncTask<String, Void, String> {

    private static final String LOG_TAG = FetchMovieTask.class.getSimpleName();

    private static final String MOVIE_API_BASE_URL = "https://api.themoviedb.org/3/movie/";

    private static final String YOUTUBE_BASE_URL = "https://www.youtube.com/watch?v=";

    public static final long RELEASE_DATE_NOT_FOUND = Long.MIN_VALUE;

    private static final String MOVIE_DB_DATE_FORMAT = "yyyy-MM-dd";
    private static final String RESULTS = "results";
    private static final String VIDEOS = "videos";
    private static final String REVIEWS = "reviews";

    private static final String MOVIE_ID = "id";
    private static final String TITLE = "title";
    private static final String POSTER_PATH = "poster_path";
    private static final String OVERVIEW = "overview";
    private static final String RELEASE_DATE = "release_date"; // yyyy-MM-dd format (see MOVIE_DB_DATE_FORMAT)
    private static final String VOTE_AVERAGE = "vote_average"; // double 0.00 to 10.00;
    private static final String KEY = "key";
    private static final String NAME = "name";
    private static final String RUNTIME = "runtime";
    private static final String AUTHOR = "author";

    private static final String REVIEW = "review";
    private static final String TRAILER = "trailer";
    private static final String URL = "url";

    private final Context context;

    public FetchMovieTask(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(String... params) {
        String errorMessage = null;
        for (String movieType : params) {

            if (! movieType.equals(context.getString(R.string.pref_type_favorites)))
                errorMessage = loadMovies(movieType);

            if (errorMessage != null) break;
            else Utility.addTypeLoaded(movieType);
        }
        Utility.setDataLoaded();
        return errorMessage;
    }

    @Override
    protected void onPostExecute(String statusMessage) {
        if (statusMessage != null)
            Toast.makeText(context, statusMessage, Toast.LENGTH_SHORT).show();
    }


    private String loadMovies(String movieType) {
        try {
            final String APP_KEY_PARAM = "api_key";

            Uri builtUri = Uri.parse(MOVIE_API_BASE_URL).buildUpon()
                    .appendEncodedPath(movieType) // popular or top_rated
                    .appendQueryParameter(APP_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                    .build();

            URL url = new URL(builtUri.toString());

            // Get the JSON data
            String movieJsonString = readJsonData(url);

            // Parse the data, put it in the database
            if (movieJsonString != null)
                putMovieDataInDatabase(movieJsonString, movieType);

        }
        catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            return "Could not access internet.";
        }
        catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
            return "Could not process movie data.";
        }
        return null;
    }


    private String loadMovieDetails(int movieDbId) {
        try {
            final String APP_KEY_PARAM = "api_key";

            Uri builtUri = Uri.parse(MOVIE_API_BASE_URL).buildUpon()
                    .appendEncodedPath(Integer.toString(movieDbId))
                    .appendQueryParameter(APP_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                    .appendQueryParameter("append_to_response", "videos,reviews")
                    .build();

            URL url = new URL(builtUri.toString());

            // Get the JSON data
            String movieDetailsJsonString = readJsonData(url);

            return movieDetailsJsonString;

        }
        catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        return null;
    }

    private String readJsonData(URL url) throws IOException {
        HttpURLConnection urlConnection = null;

        try {
            // Create the request and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

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
        finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
    }

    private void putMovieDataInDatabase(String movieJsonString, String movieType) throws JSONException {

        try {
            JSONObject moviePageJson = new JSONObject(movieJsonString);
            JSONArray movieArray = moviePageJson.getJSONArray(RESULTS);

            List<ContentValues> movies = new ArrayList<>(movieArray.length());
            List<ContentValues> types = new ArrayList<>(movieArray.length());
            List<ContentValues> details = new ArrayList<>(movieArray.length());
            List<ContentValues> trailers = new ArrayList<>();
            List<ContentValues> reviews = new ArrayList<>();

            for (int i = 0; i < movieArray.length(); i++) {

                // Get the JSON object representing a movie
                JSONObject movieJson = movieArray.getJSONObject(i);

                final int movieDbId = movieJson.getInt(MOVIE_ID);

                movies.add(getMovieData(movieJson, movieDbId));
                types.add(getMovieTypeData(movieDbId, movieType));

                String movieDetailsJsonString = loadMovieDetails(movieDbId);

                if (movieDetailsJsonString != null) {

                    details.add(getMovieDetailsData(movieDetailsJsonString));

                    trailers.addAll(getMovieTrailersData(movieDetailsJsonString));
                    reviews.addAll(getMovieReviewData(movieDetailsJsonString));
                }
            }

            // Delete old movies
            context.getContentResolver().delete(MovieTypeEntry.CONTENT_URI, MovieTypeEntry.COLUMN_TYPE + " = ?", new String[] { movieType });

            // Add data
            context.getContentResolver().bulkInsert(MovieEntry.CONTENT_URI, movies.toArray(new ContentValues[movies.size()]));
            context.getContentResolver().bulkInsert(MovieTypeEntry.CONTENT_URI, types.toArray(new ContentValues[types.size()]));
            context.getContentResolver().bulkInsert(MovieDetailsEntry.CONTENT_URI, details.toArray(new ContentValues[details.size()]));
            context.getContentResolver().bulkInsert(MovieLinksEntry.CONTENT_URI, trailers.toArray(new ContentValues[trailers.size()]));
            context.getContentResolver().bulkInsert(MovieLinksEntry.CONTENT_URI, reviews.toArray(new ContentValues[reviews.size()]));
        }
        catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private ContentValues getMovieData(JSONObject movieJson, int movieDbId) throws JSONException {

        try {
            final String title = movieJson.getString(TITLE);
            final String imagePath = movieJson.getString(POSTER_PATH);

            ContentValues movieValues = new ContentValues();

            movieValues.put(MovieEntry.COLUMN_MOVIE_DB_ID, movieDbId);
            movieValues.put(MovieEntry.COLUMN_TITLE, title);
            movieValues.put(MovieEntry.COLUMN_POSTER_THUMBNAIL, imagePath);

            return movieValues;
        }
        catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    private ContentValues getMovieTypeData(int movieDbId, String movieType) throws JSONException {

        ContentValues movieTypeValues = new ContentValues();
        movieTypeValues.put(MovieTypeEntry.COLUMN_MOVIE_DB_ID, movieDbId);
        movieTypeValues.put(MovieTypeEntry.COLUMN_TYPE, movieType);

        return movieTypeValues;
    }

    private ContentValues getMovieDetailsData(String movieDetailsJsonString) throws JSONException {
        // To parse release date
        DateFormat dateFormat = new SimpleDateFormat(MOVIE_DB_DATE_FORMAT);

        try {
            JSONObject movieDetailsJson = new JSONObject(movieDetailsJsonString);

            final int movieDbId = movieDetailsJson.getInt(MOVIE_ID);
            final String title = movieDetailsJson.getString(TITLE);
            final String poster = movieDetailsJson.getString(POSTER_PATH);
            final String plot = movieDetailsJson.getString(OVERVIEW);
            final double rating = movieDetailsJson.getDouble(VOTE_AVERAGE);

            Date releaseDate = new Date(RELEASE_DATE_NOT_FOUND);
            String releaseDateString = movieDetailsJson.getString(RELEASE_DATE);
            if (releaseDateString != null && releaseDateString.length() > 0) {
                try {
                    releaseDate = dateFormat.parse(releaseDateString);
                }
                catch (ParseException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                }
            }
            final int runtime = movieDetailsJson.getInt(RUNTIME);

            ContentValues movieDetailsValues = new ContentValues();

            movieDetailsValues.put(MovieDetailsEntry.COLUMN_MOVIE_DB_ID, movieDbId);
            movieDetailsValues.put(MovieDetailsEntry.COLUMN_TITLE, title);
            movieDetailsValues.put(MovieDetailsEntry.COLUMN_POSTER_THUMBNAIL, poster);
            movieDetailsValues.put(MovieDetailsEntry.COLUMN_PLOT, plot);
            movieDetailsValues.put(MovieDetailsEntry.COLUMN_USER_RATING, rating);
            movieDetailsValues.put(MovieDetailsEntry.COLUMN_RELEASE_DATE, releaseDate.getTime());
            movieDetailsValues.put(MovieDetailsEntry.COLUMN_RUNTIME, runtime);

            return movieDetailsValues;

        }
        catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    private List<ContentValues> getMovieTrailersData(String movieDetailsJsonString) throws JSONException {

        try {
            JSONObject movieDetailsJson = new JSONObject(movieDetailsJsonString);

            final int movieDbId = movieDetailsJson.getInt(MOVIE_ID);

            JSONObject videosJson = movieDetailsJson.getJSONObject(VIDEOS);
            JSONArray videoArray = videosJson.getJSONArray(RESULTS);

            List<ContentValues> links = new ArrayList<>(videoArray.length());

            for (int i = 0; i < videoArray.length(); i++) {
                JSONObject videoJson = videoArray.getJSONObject(i);
                String link = YOUTUBE_BASE_URL + videoJson.getString(KEY);
                String text = videoJson.getString(NAME);

                ContentValues movieLinksValues = new ContentValues();
                movieLinksValues.put(MovieLinksEntry.COLUMN_MOVIE_DB_ID, movieDbId);
                movieLinksValues.put(MovieLinksEntry.COLUMN_LINK_TEXT, text);
                movieLinksValues.put(MovieLinksEntry.COLUMN_LINK_TYPE, TRAILER);
                movieLinksValues.put(MovieLinksEntry.COLUMN_URI, link);

                links.add(movieLinksValues);
            }

            return links;

        }
        catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    private List<ContentValues> getMovieReviewData(String movieDetailsJsonString) throws JSONException {

        try {
            JSONObject movieDetailsJson = new JSONObject(movieDetailsJsonString);

            final int movieDbId = movieDetailsJson.getInt(MOVIE_ID);

            JSONObject reviewsJson = movieDetailsJson.getJSONObject(REVIEWS);
            JSONArray reviewArray = reviewsJson.getJSONArray(RESULTS);

            List<ContentValues> links = new ArrayList<>(reviewArray.length());

            for (int i = 0; i < reviewArray.length(); i++) {
                JSONObject reviewJson = reviewArray.getJSONObject(i);
                String link = reviewJson.getString(URL);
                String text = "By " + reviewJson.getString(AUTHOR);

                ContentValues movieLinksValues = new ContentValues();
                movieLinksValues.put(MovieLinksEntry.COLUMN_MOVIE_DB_ID, movieDbId);
                movieLinksValues.put(MovieLinksEntry.COLUMN_LINK_TEXT, text);
                movieLinksValues.put(MovieLinksEntry.COLUMN_LINK_TYPE, REVIEW);
                movieLinksValues.put(MovieLinksEntry.COLUMN_URI, link);

                links.add(movieLinksValues);
            }

            return links;

        }
        catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }
}