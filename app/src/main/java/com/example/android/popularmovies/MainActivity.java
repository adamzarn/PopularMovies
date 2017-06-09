package com.example.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;
import android.view.View;
import android.widget.AdapterView;
import android.content.SharedPreferences;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    GridViewAdapter myGridViewAdapter;
    String moviePreference;
    TextView myTextView;
    Context context;
    MenuItem selectedMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        final GridView gridview = (GridView) findViewById(R.id.gridview);
        myTextView = (TextView) findViewById(R.id.text_view);
        myGridViewAdapter = new GridViewAdapter(this);
        gridview.setAdapter(myGridViewAdapter);

        gridview.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                MovieObject selectedMovie = (MovieObject) parent.getAdapter().getItem(position);

                Context context = MainActivity.this;
                Class destinationClass = MovieDetailActivity.class;
                Intent intentToStartMovieDetailActivity = new Intent(context, destinationClass);
                intentToStartMovieDetailActivity.putExtra("SELECTED_MOVIE", selectedMovie);
                startActivity(intentToStartMovieDetailActivity);
            }
        });

        SharedPreferences preferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        moviePreference = preferences.getString("moviePreference", "popular");
        makeMovieDBSearchQuery(moviePreference);

        if (moviePreference.equals("popular")) {
            myTextView.setText(getString(R.string.most_popular));
        } else {
            myTextView.setText(getString(R.string.top_rated));
        }
    }

    private void makeMovieDBSearchQuery(String movieType) {
        MovieDBClient client = new MovieDBClient();
        String apiKey = client.getApiKey(getApplicationContext());
        URL movieDBSearchUrl = MovieDBClient.buildUrl(movieType, apiKey);
        new MovieDBQueryTask().execute(movieDBSearchUrl);
    }

    private class MovieDBQueryTask extends AsyncTask<URL, Void, JSONObject[]> {
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
                MovieObject[] movieObjects = new MovieObject[movies.length];
                int i = 0;
                for (JSONObject movie : movies) {
                    try {
                        String title = movie.getString("original_title");
                        String release_date = movie.getString("release_date");
                        String vote_average = movie.getString("vote_average");
                        String plot_synopsis = movie.getString("overview");
                        String poster_path = movie.getString("poster_path");
                        MovieObject newMovie = new MovieObject(title, release_date, vote_average, plot_synopsis, poster_path);
                        movieObjects[i] = newMovie;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    i = i + 1;
                }
                System.out.println(movieObjects.length);
                for (MovieObject movie : movieObjects) {
                    System.out.println(movie.getTitle());
                }
                myGridViewAdapter.setData(movieObjects);
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.movie_type, menu);
        selectedMenuItem = (MenuItem) menu.getItem(0);
        return true;
    }

    // COMPLETED (7) Override onOptionsItemSelected to handle clicks on the refresh button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        SharedPreferences preferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);

        if (id == R.id.most_popular) {
            makeMovieDBSearchQuery("popular");
            moviePreference = "popular";
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("moviePreference", moviePreference);
            editor.apply();
            myTextView.setText(getString(R.string.most_popular));
            return true;
        } else if (id == R.id.top_rated) {
            makeMovieDBSearchQuery("top_rated");
            moviePreference = "top_rated";
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("moviePreference", moviePreference);
            editor.apply();
            myTextView.setText(getString(R.string.top_rated));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
