package com.example.android.popularmovies;

import android.os.AsyncTask;
import android.widget.BaseAdapter;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.GridView;
import android.view.ViewGroup;
import android.content.res.Resources;

import java.net.URL;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.json.JSONObject;
import org.json.JSONException;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by adamzarn on 5/31/17.
 */

public class GridViewAdapter extends BaseAdapter {

    private Context context;
    private MovieObject[] myMovieData = null;

    public GridViewAdapter(Context c) {
        context = c;
    }

    public int getCount() {
        if (myMovieData == null) { return 0; }
        return myMovieData.length;
    }

    public MovieObject getItem(int position) {
        if (myMovieData == null) { return null; }
        return myMovieData[position];
    }

    public long getItemId(int position) {
        return 0;
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(context);
            int x = getScreenWidth()/2;
            Double yDouble = x*1.5027;
            int y = yDouble.intValue();
            imageView.setLayoutParams(new GridView.LayoutParams(x, y));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(0, 0, 0, 0);
        } else {
            imageView = (ImageView) convertView;
        }

        MovieObject currentMovie = myMovieData[position];

        String currentMoviePoster;

        currentMoviePoster = currentMovie.getPosterPath();
        String URLString = context.getResources().getString(R.string.poster_path_base_url) + currentMoviePoster;
        ImageQueryTask task = new ImageQueryTask(context, imageView);
        task.execute(URLString);

        return imageView;
    }

    public void setData(MovieObject[] movies) {
        myMovieData = movies;
        notifyDataSetChanged();
    }

    public class ImageQueryTask extends AsyncTask<String, Void, Bitmap> {

        private final WeakReference<ImageView> imageViewReference;
        private Context context;

        public ImageQueryTask(Context context, ImageView img) {
            imageViewReference = new WeakReference<ImageView>(img);
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            final ImageView imageView = imageViewReference.get();
            imageView.setImageBitmap(null);
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
                final ImageView imageView = imageViewReference.get();
                imageView.setImageBitmap(bmp);
            }
        }
    }

}
