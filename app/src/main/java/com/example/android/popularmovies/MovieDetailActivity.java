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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

import static com.example.android.popularmovies.R.id.review_recycler_view;
import static com.example.android.popularmovies.R.id.trailer_recycler_view;

public class MovieDetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<JSONObject[]>,
        TrailerRecyclerViewAdapter.TrailerClickListener,
        ReviewRecyclerViewAdapter.ReviewClickListener {

    private Context context;

    private static final String TRAILERS_URL_EXTRA = "trailers";
    private static final int TRAILERS_LOADER = 1;

    private static final String REVIEWS_URL_EXTRA = "reviews";
    private static final int REVIEWS_LOADER = 2;

    private String id;

    TextView titleTextView;
    TextView releaseDateTextView;
    TextView voteAverageTextView;
    TextView plotSynopsisTextView;
    ImageView moviePosterImageView;

    RecyclerView trailerRecyclerView;
    RecyclerView reviewRecyclerView;
    TrailerRecyclerViewAdapter myTrailerRecyclerViewAdapter;
    ReviewRecyclerViewAdapter myReviewRecyclerViewAdapter;

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
        String title = selectedMovie.getTitle();
        String releaseDate = selectedMovie.getReleaseDate();
        String voteAverage = selectedMovie.getVoteAverage() + " / 10";
        String plotSynopsis = selectedMovie.getPlotSynopsis();
        String posterPath = selectedMovie.getPosterPath();

        titleTextView.setText(title);
        releaseDateTextView.setText(releaseDate.substring(0,4));
        voteAverageTextView.setText(voteAverage);
        plotSynopsisTextView.setText(plotSynopsis);

        getMoviePoster(posterPath);
        getTrailersAndReviews();

        trailerRecyclerView = (RecyclerView) findViewById(trailer_recycler_view);
        reviewRecyclerView = (RecyclerView) findViewById(review_recycler_view);

        LinearLayoutManager trailersLayoutManager = new LinearLayoutManager(this);
        LinearLayoutManager reviewsLayoutManager = new LinearLayoutManager(this);

        trailerRecyclerView.setLayoutManager(trailersLayoutManager);
        reviewRecyclerView.setLayoutManager(reviewsLayoutManager);

        trailerRecyclerView.setHasFixedSize(true);
        reviewRecyclerView.setHasFixedSize(true);

        myTrailerRecyclerViewAdapter = new TrailerRecyclerViewAdapter(this);
        myReviewRecyclerViewAdapter = new ReviewRecyclerViewAdapter(this);

        trailerRecyclerView.setAdapter(myTrailerRecyclerViewAdapter);
        reviewRecyclerView.setAdapter(myReviewRecyclerViewAdapter);

    }

    public void getTrailersAndReviews() {

        MovieDBClient client = new MovieDBClient();
        String apiKey = client.getApiKey(getApplicationContext());

        String trailersUrlSuffix = id + "/videos";
        String reviewsUrlSuffix = id + "/reviews";

        URL trailersUrl = MovieDBClient.buildUrl(trailersUrlSuffix, apiKey);
        URL reviewsUrl = MovieDBClient.buildUrl(reviewsUrlSuffix, apiKey);

        Bundle trailersBundle = new Bundle();
        trailersBundle.putString(TRAILERS_URL_EXTRA, trailersUrl.toString());

        Bundle reviewsBundle = new Bundle();
        reviewsBundle.putString(REVIEWS_URL_EXTRA, reviewsUrl.toString());

        LoaderManager loaderManager = getSupportLoaderManager();

        Loader<String[]> trailersLoader = loaderManager.getLoader(TRAILERS_LOADER);
        Loader<String[]> reviewsLoader = loaderManager.getLoader(REVIEWS_LOADER);

        if (trailersLoader == null) {
            loaderManager.initLoader(TRAILERS_LOADER, trailersBundle, this).forceLoad();
        } else {
            loaderManager.restartLoader(TRAILERS_LOADER, trailersBundle, this).forceLoad();
        }

        if (reviewsLoader == null) {
            loaderManager.initLoader(REVIEWS_LOADER, reviewsBundle, this).forceLoad();
        } else {
            loaderManager.restartLoader(REVIEWS_LOADER, reviewsBundle, this).forceLoad();
        }
    }


    public void getMoviePoster(String moviePoster) {
        String URLString = context.getResources().getString(R.string.poster_path_base_url) + moviePoster;
        new ImageQueryTask().execute(URLString);
    }

    @Override
    public void onListItemClick(View view, int clickedItemIndex) {
        int id = ((ViewGroup) view.getParent()).getId();
        if (id == R.id.trailer_recycler_view) {
            TrailerObject clickedTrailer = myTrailerRecyclerViewAdapter.getItemAtPosition(clickedItemIndex);
            String youtubeID = clickedTrailer.getKey();
            watchYoutubeVideo(youtubeID);
        } else if (id == R.id.review_recycler_view) {
            ReviewObject clickedReview = myReviewRecyclerViewAdapter.getItemAtPosition(clickedItemIndex);
            String reviewID = clickedReview.getUrl();
            openReview(reviewID);
        }
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
    public Loader<JSONObject[]> onCreateLoader(final int id, final Bundle args) {
        return new AsyncTaskLoader<JSONObject[]>(this) {

            @Override
            public JSONObject[] loadInBackground() {
                String urlString = null;
                switch (id) {
                    case 1:
                        urlString = args.getString(TRAILERS_URL_EXTRA);
                        break;
                    case 2:
                        urlString = args.getString(REVIEWS_URL_EXTRA);
                        break;
                    default:
                        break;
                }
                System.out.println(urlString);

                if (urlString == null || TextUtils.isEmpty(urlString)) {
                    return null;
                }
                JSONObject[] objects;
                try {
                    URL searchUrl = new URL(urlString);
                    String jsonResponse = MovieDBClient
                            .getResponseFromHttpUrl(searchUrl);
                    objects = MovieDBClient
                            .getData(MovieDetailActivity.this, jsonResponse);
                    return objects;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<JSONObject[]> loader, JSONObject[] objects) {

        int loaderID = loader.getId();
        switch (loaderID) {
            case 1:
                if (objects != null) {
                    TrailerObject[] trailerObjects = new TrailerObject[objects.length];
                    int i = 0;
                    for (JSONObject object : objects) {
                        try {
                            String key = object.getString("key");
                            String name = object.getString("name");
                            TrailerObject trailerObject = new TrailerObject(key, name);
                            trailerObjects[i] = trailerObject;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        i = i + 1;
                    }
                    myTrailerRecyclerViewAdapter.setData(trailerObjects);
                    int height = myTrailerRecyclerViewAdapter.getItemCount() * 60;
                    ViewGroup.LayoutParams params = trailerRecyclerView.getLayoutParams();
                    params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height, getResources().getDisplayMetrics());
                    trailerRecyclerView.setLayoutParams(params);
                    break;
                }
            case 2:
                if (objects != null) {
                    ReviewObject[] reviewObjects = new ReviewObject[objects.length];
                    int i = 0;
                    for (JSONObject object : objects) {
                        try {
                            String author = object.getString("author");
                            String content = object.getString("content");
                            String url = object.getString("url");
                            ReviewObject reviewObject = new ReviewObject(author, content, url);
                            reviewObjects[i] = reviewObject;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        i = i + 1;
                    }
                    myReviewRecyclerViewAdapter.setData(reviewObjects);
                    int height = myReviewRecyclerViewAdapter.getItemCount() * 360;
                    ViewGroup.LayoutParams params = reviewRecyclerView.getLayoutParams();
                    params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height, getResources().getDisplayMetrics());
                    reviewRecyclerView.setLayoutParams(params);
                }
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

    public void openReview(String url) {
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(url));
        try {
            startActivity(webIntent);
        } catch (ActivityNotFoundException ex) {
            return;
        }
    }
}
