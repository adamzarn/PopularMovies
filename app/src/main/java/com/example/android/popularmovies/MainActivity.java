package com.example.android.popularmovies;

import android.graphics.Movie;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.GridView;
import com.example.android.popularmovies.GridViewAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.view.View;
import android.widget.AdapterView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    GridView myGridView;
    GridViewAdapter myGridViewAdapter;
    String[] myImageData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GridView gridview = (GridView) findViewById(R.id.gridview);
        myGridViewAdapter = new GridViewAdapter(this);
        gridview.setAdapter(myGridViewAdapter);

        gridview.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(MainActivity.this, "" + position,
                        Toast.LENGTH_SHORT).show();
            }
        });

        makeMovieDBSearchQuery();
    }

    private void makeMovieDBSearchQuery() {
        MovieDBClient client = new MovieDBClient();
        String apiKey = client.getApiKey(getApplicationContext());
        URL movieDBSearchUrl = MovieDBClient.buildUrl("popular", apiKey);
        new MovieDBQueryTask().execute(movieDBSearchUrl);
    }

    public class MovieDBQueryTask extends AsyncTask<URL, Void, JSONObject[]> {
        @Override
        protected JSONObject[] doInBackground(URL... urls) {
            URL searchUrl = urls[0];
            JSONObject[] movies = null;

            try {
                String jsonMovieResponse = MovieDBClient
                        .getResponseFromHttpUrl(searchUrl);

                movies = MovieDBClient
                        .getSimpleMovieStringsFromJson(MainActivity.this, jsonMovieResponse);

                return movies;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(JSONObject[] movies) {
            if (movies != null) {
                myImageData = new String[movies.length];
                int i = 0;
                for (JSONObject movie : movies) {
                    try {
                        String posterPath = movie.getString("poster_path");
                        myImageData[i] = posterPath;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    i = i + 1;
                }
                System.out.println(myImageData.length);
                if (myImageData == null) {
                } else {
                    myGridViewAdapter.setImageData(myImageData);
                }
            }
        }

    }
}
