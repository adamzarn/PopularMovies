package com.example.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    GridViewAdapter myGridViewAdapter;
    String moviePreference;
    TextView myTextView;
    Context context;
    MenuItem selectedMenuItem;

    private static final String SEARCH_QUERY_URL_EXTRA = "query";
    private static final int MOVIE_QUERY_LOADER = 1;
    private static final int FAVORITE_MOVIE_QUERY_LOADER = 2;

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

        if (moviePreference.equals("popular")) {
            myTextView.setText(getString(R.string.most_popular));
            makeMovieDBSearchQuery(moviePreference);
        } else if (moviePreference.equals("top_rated")) {
            myTextView.setText(getString(R.string.top_rated));
            makeMovieDBSearchQuery(moviePreference);
        } else {
            myTextView.setText(getString(R.string.favorites));
            getFavoriteMovies();
        }
    }

    private void makeMovieDBSearchQuery(String movieType) {
        MovieDBClient client = new MovieDBClient();
        String apiKey = client.getApiKey(getApplicationContext());
        URL movieDBSearchUrl = MovieDBClient.buildUrl(movieType, apiKey);

        Bundle queryBundle = new Bundle();
        queryBundle.putString(SEARCH_QUERY_URL_EXTRA, movieDBSearchUrl.toString());

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<JSONObject[]> movieQueryLoader = loaderManager.getLoader(MOVIE_QUERY_LOADER);

        if (movieQueryLoader == null) {
            loaderManager.initLoader(MOVIE_QUERY_LOADER, queryBundle, onlineDatabaseListener).forceLoad();
        } else {
            loaderManager.restartLoader(MOVIE_QUERY_LOADER, queryBundle, onlineDatabaseListener).forceLoad();
        }

    }

    private void getFavoriteMovies() {
        Bundle queryBundle = new Bundle();

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<Cursor> favoriteMovieQueryLoader = loaderManager.getLoader(FAVORITE_MOVIE_QUERY_LOADER);

        if (favoriteMovieQueryLoader == null) {
            loaderManager.initLoader(FAVORITE_MOVIE_QUERY_LOADER, queryBundle, localDatabaseListener).forceLoad();
        } else {
            loaderManager.restartLoader(FAVORITE_MOVIE_QUERY_LOADER, queryBundle, localDatabaseListener).forceLoad();
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
        myGridViewAdapter.setData(null);

        SharedPreferences preferences = context.getSharedPreferences(getString(R.string.my_preferences), Context.MODE_PRIVATE);

        if (id == R.id.most_popular) {
            makeMovieDBSearchQuery(getString(R.string.QUERY_POPULAR));
            moviePreference = getString(R.string.QUERY_POPULAR);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("moviePreference", moviePreference);
            editor.apply();
            myTextView.setText(getString(R.string.most_popular));
            return true;
        } else if (id == R.id.top_rated) {
            makeMovieDBSearchQuery(getString(R.string.QUERY_TOP_RATED));
            moviePreference = getString(R.string.QUERY_TOP_RATED);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("moviePreference", moviePreference);
            editor.apply();
            myTextView.setText(getString(R.string.top_rated));
            return true;
        } else if (id == R.id.favorites) {
            getFavoriteMovies();
            moviePreference = getString(R.string.QUERY_FAVORITES);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("moviePreference", moviePreference);
            editor.apply();
            myTextView.setText(getString(R.string.favorites));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class DbBitmapUtility {

        public static byte[] getBytes(Bitmap bitmap) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
            return stream.toByteArray();
        }

        public static Bitmap getImage(byte[] image) {
            return BitmapFactory.decodeByteArray(image, 0, image.length);
        }

    }

    private LoaderManager.LoaderCallbacks<JSONObject[]> onlineDatabaseListener
            = new LoaderManager.LoaderCallbacks<JSONObject[]>() {

        @Override
        public Loader<JSONObject[]> onCreateLoader(int id, final Bundle args) {
            return new AsyncTaskLoader<JSONObject[]>(context) {

                @Override
                public JSONObject[] loadInBackground() {
                    String searchQueryUrlString = args.getString(SEARCH_QUERY_URL_EXTRA);
                    if (searchQueryUrlString == null || TextUtils.isEmpty(searchQueryUrlString)) {
                        return null;
                    }
                    JSONObject[] movies = null;
                    try {
                        URL searchUrl = new URL(searchQueryUrlString);
                        String jsonMovieResponse = MovieDBClient
                                .getResponseFromHttpUrl(searchUrl);
                        movies = MovieDBClient
                                .getData(MainActivity.this, jsonMovieResponse);
                        return movies;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<JSONObject[]> loader, JSONObject[] movies) {
            if (movies != null) {
                MovieObject[] movieObjects = new MovieObject[movies.length];
                int i = 0;
                for (JSONObject movie : movies) {
                    try {
                        String id = movie.getString(getString(R.string.id));
                        String title = movie.getString(getString(R.string.original_title));
                        String release_date = movie.getString(getString(R.string.release_date));
                        String vote_average = movie.getString(getString(R.string.vote_average));
                        String plot_synopsis = movie.getString(getString(R.string.plot_synopsis));
                        String poster_path = movie.getString(getString(R.string.poster_jpg));
                        MovieObject newMovie = new MovieObject(id, title, release_date, vote_average, plot_synopsis, poster_path, null);
                        movieObjects[i] = newMovie;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    i = i + 1;
                }
                myGridViewAdapter.setData(movieObjects);
            }
        }

        @Override
        public void onLoaderReset(Loader<JSONObject[]> loader) {
        }
    };

    private LoaderManager.LoaderCallbacks<Cursor> localDatabaseListener
            = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new AsyncTaskLoader<Cursor>(context) {

                @Override
                public Cursor loadInBackground() {
                    try {
                        return getContentResolver().query(FavoritesContract.FavoritesEntry.CONTENT_URI,
                                null,
                                null,
                                null,
                                FavoritesContract.FavoritesEntry._ID);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            };
        }

        @Override
        public void onLoadFinished (Loader<Cursor> loader, Cursor data) {
            MovieObject[] movieObjects = new MovieObject[data.getCount()];
            if (data.getCount() > 0) {
                int i = 0;
                while (data.moveToNext()) {
                    String id = data.getString(data.getColumnIndex("id"));
                    String title = data.getString(data.getColumnIndex("title"));
                    String release_date = data.getString(data.getColumnIndex("release_date"));
                    String vote_average = data.getString(data.getColumnIndex("vote_average"));
                    String plot_synopsis = data.getString(data.getColumnIndex("plot_synopsis"));
                    String poster_path = data.getString(data.getColumnIndex("poster_path"));
                    byte[] posterBytes = data.getBlob(data.getColumnIndex("poster"));
                    Bitmap poster = DbBitmapUtility.getImage(posterBytes);
                    MovieObject newMovie = new MovieObject(id, title, release_date, vote_average, plot_synopsis, poster_path, poster);
                    movieObjects[i] = newMovie;
                    i++;
                }
                myGridViewAdapter.setData(movieObjects);
            } else {
                myGridViewAdapter.setData(null);
            }

        }

        @Override
        public void onLoaderReset (Loader<Cursor> loader) {
        }

    };

}
