package com.example.android.popularmovies;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.net.URL;

public class MovieDetailActivity extends AppCompatActivity {

    private Context context;

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
        titleTextView.setText(selectedMovie.getTitle());
        releaseDateTextView.setText(selectedMovie.getReleaseDate().substring(0,4));
        voteAverageTextView.setText(selectedMovie.getVoteAverage() + " / 10");
        plotSynopsisTextView.setText(selectedMovie.getPlotSynopsis());

        getMoviePoster(selectedMovie.getPosterPath());

    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public void getMoviePoster(String moviePoster) {
        String URLString = context.getResources().getString(R.string.poster_path_base_url) + moviePoster;
        System.out.println(URLString);
        new ImageQueryTask().execute(URLString);
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
}
