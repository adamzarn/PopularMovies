package com.example.android.popularmovies;

import android.graphics.Movie;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    TextView myTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myTextView = (TextView) findViewById(R.id.movies_text_view);
        makeMovieDBSearchQuery();
    }

    private void makeMovieDBSearchQuery() {
        MovieDBClient client = new MovieDBClient();
        String apiKey = client.getApiKey(getApplicationContext());
        URL movieDBSearchUrl = MovieDBClient.buildUrl("popular", apiKey);
        new MovieDBQueryTask().execute(movieDBSearchUrl);
    }

    public class MovieDBQueryTask extends AsyncTask<URL, Void, String> {
        @Override
        protected String doInBackground(URL... urls) {
            URL searchUrl = urls[0];
            String movies = null;
            try {
                movies = MovieDBClient.getResponseFromHttpUrl(searchUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return movies;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s != null && !s.equals("")) {
                System.out.println(s);
                myTextView.setText(s);
            }
        }
    }
}
