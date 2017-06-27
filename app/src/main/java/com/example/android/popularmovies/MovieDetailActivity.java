package com.example.android.popularmovies;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.URL;

import static com.example.android.popularmovies.R.id.no_reviews;
import static com.example.android.popularmovies.R.id.no_trailers;
import static com.example.android.popularmovies.R.id.review_recycler_view;
import static com.example.android.popularmovies.R.id.trailer_recycler_view;

public class MovieDetailActivity extends AppCompatActivity implements
        TrailerRecyclerViewAdapter.TrailerClickListener,
        ReviewRecyclerViewAdapter.ReviewClickListener {

    private Context context;

    private static final String TRAILERS_URL_EXTRA = "trailers";
    private static final int TRAILERS_LOADER = 1;

    private static final String REVIEWS_URL_EXTRA = "reviews";
    private static final int REVIEWS_LOADER = 2;

    private static final String FAVORITE_EXTRA = "id";
    private static final int FAVORITE_LOADER = 3;

    private String id;
    private String title;
    private String releaseDate;
    private String voteAverage;
    private String voteAverageString;
    private String plotSynopsis;

    TextView titleTextView;
    TextView releaseDateTextView;
    TextView voteAverageTextView;
    TextView favoritedTextView;
    Button toggleFavoritesButton;
    Bitmap poster;
    String posterPath;
    TextView plotSynopsisTextView;
    ImageView moviePosterImageView;

    RecyclerView trailerRecyclerView;
    TextView noTrailersTextView;

    RecyclerView reviewRecyclerView;
    TextView noReviewsTextView;

    TrailerRecyclerViewAdapter myTrailerRecyclerViewAdapter;
    ReviewRecyclerViewAdapter myReviewRecyclerViewAdapter;

    FavoritesProvider myFavoritesProvider;
    FavoritesDbHelper myFavoritesDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        MovieObject selectedMovie = getIntent().getExtras().getParcelable("SELECTED_MOVIE");

        context = getApplicationContext();

        titleTextView = (TextView) findViewById(R.id.title);
        releaseDateTextView = (TextView) findViewById(R.id.release_date);
        voteAverageTextView = (TextView) findViewById(R.id.vote_average);
        favoritedTextView = (TextView) findViewById(R.id.favorited);
        toggleFavoritesButton = (Button) findViewById(R.id.toggle_favorites_button);
        plotSynopsisTextView = (TextView) findViewById(R.id.plot_synopsis);
        moviePosterImageView = (ImageView) findViewById(R.id.movie_poster);

        assert selectedMovie != null;
        id = selectedMovie.getID();
        title = selectedMovie.getTitle();
        releaseDate = selectedMovie.getReleaseDate();
        voteAverage = selectedMovie.getVoteAverage();
        voteAverageString = voteAverage + " / 10";
        plotSynopsis = selectedMovie.getPlotSynopsis();
        posterPath = selectedMovie.getPosterPath();
        poster = selectedMovie.getPoster();

        titleTextView.setText(title);
        releaseDateTextView.setText(releaseDate.substring(0,4));
        voteAverageTextView.setText(voteAverage);
        plotSynopsisTextView.setText(plotSynopsis);
        moviePosterImageView.setImageBitmap(poster);

        getTrailersAndReviews();

        trailerRecyclerView = (RecyclerView) findViewById(trailer_recycler_view);
        noTrailersTextView = (TextView) findViewById(no_trailers);

        reviewRecyclerView = (RecyclerView) findViewById(review_recycler_view);
        noReviewsTextView = (TextView) findViewById(no_reviews);

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

        checkIfMovieIsFavorited(id);

    }

    public void checkIfMovieIsFavorited(String id) {
        Bundle favoriteBundle = new Bundle();
        favoriteBundle.putString(FAVORITE_EXTRA, id);

        LoaderManager loaderManager = getSupportLoaderManager();

        Loader<String[]> favoriteLoader = loaderManager.getLoader(FAVORITE_LOADER);

        if (favoriteLoader == null) {
            loaderManager.initLoader(FAVORITE_LOADER, favoriteBundle, checkIfRecordExists).forceLoad();
        } else {
            loaderManager.restartLoader(FAVORITE_LOADER, favoriteBundle, checkIfRecordExists).forceLoad();
        }
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
            loaderManager.initLoader(TRAILERS_LOADER, trailersBundle, recyclerViewLoader).forceLoad();
        } else {
            loaderManager.restartLoader(TRAILERS_LOADER, trailersBundle, recyclerViewLoader).forceLoad();
        }

        if (reviewsLoader == null) {
            loaderManager.initLoader(REVIEWS_LOADER, reviewsBundle, recyclerViewLoader).forceLoad();
        } else {
            loaderManager.restartLoader(REVIEWS_LOADER, reviewsBundle, recyclerViewLoader).forceLoad();
        }
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

    public void toggleFavoritesClicked(View view) {

        if (toggleFavoritesButton.getText() == getString(R.string.add_to_favorites)) {

            ContentValues contentValues = new ContentValues();
            contentValues.put(FavoritesContract.FavoritesEntry.COLUMN_ID, id);
            contentValues.put(FavoritesContract.FavoritesEntry.COLUMN_TITLE, title);
            contentValues.put(FavoritesContract.FavoritesEntry.COLUMN_RELEASE_DATE, releaseDate);
            contentValues.put(FavoritesContract.FavoritesEntry.COLUMN_VOTE_AVERAGE, voteAverage);
            contentValues.put(FavoritesContract.FavoritesEntry.COLUMN_PLOT_SYNOPSIS, plotSynopsis);
            contentValues.put(FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH, posterPath);
            byte[] posterBytes = DbBitmapUtility.getBytes(poster);
            contentValues.put(FavoritesContract.FavoritesEntry.COLUMN_POSTER, posterBytes);

            Uri uri = getContentResolver().insert(FavoritesContract.FavoritesEntry.CONTENT_URI, contentValues);

            if (uri != null) {
                favoritedTextView.setVisibility(View.VISIBLE);
                Toast.makeText(getBaseContext(), getString(R.string.added_to_favorites), Toast.LENGTH_LONG).show();
                toggleFavoritesButton.setText(getString(R.string.remove_from_favorites));
            } else {
                Toast.makeText(getBaseContext(), getString(R.string.not_added_to_favorites), Toast.LENGTH_LONG).show();
            }

        } else {
            int numDeleted = getContentResolver().delete(FavoritesContract.FavoritesEntry.CONTENT_URI, "id=?", new String[]{id});
            if (numDeleted > 0) {
                favoritedTextView.setVisibility(View.GONE);
                Toast.makeText(getBaseContext(), getString(R.string.removed_from_favorites), Toast.LENGTH_LONG).show();
                toggleFavoritesButton.setText(getString(R.string.add_to_favorites));
            } else {
                Toast.makeText(getBaseContext(), getString(R.string.not_removed_from_favorites), Toast.LENGTH_LONG).show();
            }
        }

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

    private LoaderManager.LoaderCallbacks<JSONObject[]> recyclerViewLoader
            = new LoaderManager.LoaderCallbacks<JSONObject[]>() {

        @Override
        public Loader<JSONObject[]> onCreateLoader(final int id, final Bundle args) {
            return new AsyncTaskLoader<JSONObject[]>(context) {

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
                        if (trailerObjects.length > 0) {
                            noTrailersTextView.setVisibility(View.GONE);
                            trailerRecyclerView.setVisibility(View.VISIBLE);
                            int height = myTrailerRecyclerViewAdapter.getItemCount() * 60;
                            ViewGroup.LayoutParams params = trailerRecyclerView.getLayoutParams();
                            params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height, getResources().getDisplayMetrics());
                            trailerRecyclerView.setLayoutParams(params);
                            break;
                        } else {
                            noTrailersTextView.setVisibility(View.VISIBLE);
                            trailerRecyclerView.setVisibility(View.GONE);
                        }
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
                        if (reviewObjects.length > 0) {
                            noReviewsTextView.setVisibility(View.GONE);
                            reviewRecyclerView.setVisibility(View.VISIBLE);
                            int height = myReviewRecyclerViewAdapter.getItemCount() * 360;
                            ViewGroup.LayoutParams params = reviewRecyclerView.getLayoutParams();
                            params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height, getResources().getDisplayMetrics());
                            reviewRecyclerView.setLayoutParams(params);
                        } else {
                            noReviewsTextView.setVisibility(View.VISIBLE);
                            reviewRecyclerView.setVisibility(View.GONE);
                        }
                    }
            }
        }

        @Override
        public void onLoaderReset(Loader<JSONObject[]> loader) {
        }

    };


    private LoaderManager.LoaderCallbacks<Cursor> checkIfRecordExists
            = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int id, final Bundle args) {
            return new AsyncTaskLoader<Cursor>(context) {

                @Override
                public Cursor loadInBackground() {
                    String selection = FavoritesContract.FavoritesEntry.COLUMN_ID + "=?";
                    String[] selectionArgs = {args.getString(FAVORITE_EXTRA)};
                    return getContentResolver().query(FavoritesContract.FavoritesEntry.CONTENT_URI,
                            null,
                            selection,
                            selectionArgs,
                            FavoritesContract.FavoritesEntry._ID);
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data.getCount() > 0) {
                System.out.println("Favorite");
                favoritedTextView.setVisibility(View.VISIBLE);
                toggleFavoritesButton.setText(getString(R.string.remove_from_favorites));
            } else {
                System.out.println("Not a favorite");
                favoritedTextView.setVisibility(View.GONE);
                toggleFavoritesButton.setText(getString(R.string.add_to_favorites));
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };

}
