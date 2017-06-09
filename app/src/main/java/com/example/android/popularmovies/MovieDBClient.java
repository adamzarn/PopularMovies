package com.example.android.popularmovies;

import android.content.res.AssetManager;
import java.io.InputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import android.content.Context;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by adamzarn on 5/30/17.
 */

public class MovieDBClient {

    public static String getApiKey(Context context) {
        AssetManager am = context.getAssets();
        try {
            InputStream is = am.open("keys.txt");
            Scanner s = new Scanner(is).useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        } catch (IOException e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    final static String MOVIEDB_BASE_URL =
            "https://api.themoviedb.org/3/movie/";

    final static String API_KEY = "api_key";
    final static String PAGE = "page";

    public static URL buildUrl(String movieList, String apiKey) {
        Uri builtUri = Uri.parse(MOVIEDB_BASE_URL + movieList).buildUpon()
                .appendQueryParameter(API_KEY, apiKey)
                .appendQueryParameter(PAGE, "1")
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    public static JSONObject[] getSimpleMovieStringsFromJson(Context context, String movieJsonStr)
            throws JSONException {

        final String MOVIE_RESULTS = "results";
        final String MOVIE_STATUS_CODE = "status_code";

        String[] parsedMovieData = null;

        JSONObject movieJson = new JSONObject(movieJsonStr);

        /* Is there an error? */
        if (movieJson.has(MOVIE_STATUS_CODE)) {
            int errorCode = movieJson.getInt(MOVIE_STATUS_CODE);

            switch (errorCode) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    return null;
                default:
                    return null;
            }
        }

        JSONArray movieArray = movieJson.getJSONArray(MOVIE_RESULTS);
        JSONObject[] movies = new JSONObject[movieArray.length()];

        for (int i = 0; i < movieArray.length(); i++) {

            JSONObject movie = movieArray.getJSONObject(i);
            movies[i] = movie;

        }
        return movies;
    }
}
