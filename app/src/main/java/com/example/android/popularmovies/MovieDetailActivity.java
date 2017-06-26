package com.example.android.popularmovies;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

import static com.example.android.popularmovies.R.id.trailer_recycler_view;

public class MovieDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<JSONObject[]>, TrailerRecyclerViewAdapter.TrailerClickListener {

    private Context context;

    private static final String TRAILERS_URL_EXTRA = "trailers";
    private static final int TRAILERS_LOADER = 1;

    private String id;
    private String title;
    private String releaseDate;
    private String voteAverage;
    private String plotSynopsis;
    private String posterPath;

    TextView titleTextView;
    TextView releaseDateTextView;
    TextView voteAverageTextView;
    TextView plotSynopsisTextView;
    ImageView moviePosterImageView;

    RecyclerView trailerRecyclerView;
    TrailerRecyclerViewAdapter myTrailerRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        context = getApplicationContext();

        titleTextView = (TextView) findViewById(R.id.title);
        releaseDateTextView = (TextView) findViewById(R.id.release_date);
        voteAverageTextView = (TextView) findViewById(R.id.vote_average);
        plotSynopsisTextView = (TextView) findViewById(R.id.plot_synopsis);
        moviePosterImageView = (ImageView) findViewById(R.id.movie_poster);

        MovieObject selectedMovie = getIntent().getExtras().getParcelable("SELECTED_MOVIE");

        assert selectedMovie != null;
        id = selectedMovie.getID();
        title = selectedMovie.getTitle();
        releaseDate = selectedMovie.getReleaseDate();
        voteAverage = selectedMovie.getVoteAverage() + " / 10";
        plotSynopsis = selectedMovie.getPlotSynopsis();
        posterPath = selectedMovie.getPosterPath();

        titleTextView.setText(title);
        releaseDateTextView.setText(releaseDate.substring(0,4));
        voteAverageTextView.setText(voteAverage);
        plotSynopsisTextView.setText(plotSynopsis);

        getMoviePoster(posterPath);
        getTrailers();

        trailerRecyclerView = (RecyclerView) findViewById(trailer_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        trailerRecyclerView.setLayoutManager(layoutManager);
        trailerRecyclerView.setHasFixedSize(true);

        myTrailerRecyclerViewAdapter = new TrailerRecyclerViewAdapter(this);
        trailerRecyclerView.setAdapter(myTrailerRecyclerViewAdapter);

    }

    public void getTrailers() {
        MovieDBClient client = new MovieDBClient();
        String apiKey = client.getApiKey(getApplicationContext());
        String baseUrlSuffix = id + "/videos";
        URL trailersURL = MovieDBClient.buildUrl(baseUrlSuffix, apiKey);
        Bundle trailerBundle = new Bundle();
        trailerBundle.putString(TRAILERS_URL_EXTRA, trailersURL.toString());

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String[]> movieQueryLoader = loaderManager.getLoader(TRAILERS_LOADER);

        if (movieQueryLoader == null) {
            loaderManager.initLoader(TRAILERS_LOADER, trailerBundle, this).forceLoad();
        } else {
            loaderManager.restartLoader(TRAILERS_LOADER, trailerBundle, this).forceLoad();
        }
    }

    public void getMoviePoster(String moviePoster) {
        String URLString = context.getResources().getString(R.string.poster_path_base_url) + moviePoster;
        new ImageQueryTask().execute(URLString);
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        TrailerObject clickedTrailer = myTrailerRecyclerViewAdapter.getItemAtPosition(clickedItemIndex);
        String youtubeID = clickedTrailer.getKey();
        watchYoutubeVideo(youtubeID);
    }

    private class ImageQueryTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            moviePosterImageView.setImageBitmap(null);
        }

        @Override
        protected Bitmap doInBackground(String... params) {

            String URLString = params[0];
            Bitmap image = null;

            try {
                URL url = new URL(URLString);
                return BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(Bitmap bmp) {
            if (bmp != null) {
                moviePosterImageView.setImageBitmap(bmp);
            }
        }
    }

    @Override
    public Loader<JSONObject[]> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<JSONObject[]>(this) {

            @Override
            public JSONObject[] loadInBackground() {
                String trailersUrlString = args.getString(TRAILERS_URL_EXTRA);
                if (trailersUrlString == null || TextUtils.isEmpty(trailersUrlString)) {
                    return null;
                }
                JSONObject[] trailerPaths = null;
                try {
                    URL searchUrl = new URL(trailersUrlString);
                    String jsonTrailersResponse = MovieDBClient
                            .getResponseFromHttpUrl(searchUrl);
                    trailerPaths = MovieDBClient
                            .getData(MovieDetailActivity.this, jsonTrailersResponse);
                    return trailerPaths;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<JSONObject[]> loader, JSONObject[] trailers) {
        if (trailers != null) {
            TrailerObject[] trailerObjects = new TrailerObject[trailers.length];
            int i = 0;
            for (JSONObject trailer : trailers) {
                try {
                    String key = trailer.getString("key");
                    String name = trailer.getString("name");
                    TrailerObject trailerObject = new TrailerObject(key, name);
                    trailerObjects[i] = trailerObject;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                i = i + 1;
            }
            myTrailerRecyclerViewAdapter.setData(trailerObjects);
            int height = myTrailerRecyclerViewAdapter.getItemCount()*60;
            ViewGroup.LayoutParams params=trailerRecyclerView.getLayoutParams();
            int dpHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height, getResources().getDisplayMetrics());
            params.height=dpHeight;
            trailerRecyclerView.setLayoutParams(params);
            System.out.println(dpHeight);
        }
    }

    @Override
    public void onLoaderReset(Loader<JSONObject[]> loader) {
    }

    public void watchYoutubeVideo(String id) {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=" + id));
        try {
            startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            startActivity(webIntent);
        }
    }
}
