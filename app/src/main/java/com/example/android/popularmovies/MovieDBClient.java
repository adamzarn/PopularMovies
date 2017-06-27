package com.example.android.popularmovies;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

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

    public static URL buildUrl(String baseUrlSuffix, String apiKey) {
        Uri builtUri = Uri.parse(MOVIEDB_BASE_URL + baseUrlSuffix).buildUpon()
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

    public static JSONObject[] getData(Context context, String jsonStr)
            throws JSONException {

        final String RESULTS = "results";
        final String STATUS_CODE = "status_code";

        JSONObject jsonObject = new JSONObject(jsonStr);

        if (jsonObject.has(STATUS_CODE)) {
            int errorCode = jsonObject.getInt(STATUS_CODE);

            switch (errorCode) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    return null;
                default:
                    return null;
            }
        }

        JSONArray dataArray = jsonObject.getJSONArray(RESULTS);
        JSONObject[] data = new JSONObject[dataArray.length()];

        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject datum = dataArray.getJSONObject(i);
            data[i] = datum;
        }

        return data;
    }

}
