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

/**
 * Created by adamzarn on 5/30/17.
 */

public class MovieDBClient {

    public static String getApiKey(Context context) {
        AssetManager am = context.getAssets();
        try {
            InputStream is = am.open("keys.txt");
            Scanner s = new Scanner(is).useDelimiter("\\A");
            String apiKey = s.hasNext() ? s.next() : "";
            return apiKey;
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
        System.out.println("Calling getResponse");
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
}
