package com.example.android.popularmovies;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

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

    public ArrayList<MovieObject> getItems() {
        return new ArrayList<>(Arrays.asList(myMovieData));
    }

    public void setItems(ArrayList<MovieObject> arrayList) {
        myMovieData = arrayList.toArray(new MovieObject[arrayList.size()]);
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

        MovieObject currentMovie = getItem(position);
        Bitmap poster = currentMovie.getPoster();

        int x = getScreenWidth()/2;
        Double yDouble = x*1.5027;
        int y = yDouble.intValue();

        if (poster != null) {
            imageView = new ImageView(context);
            Bitmap resizedPoster = Bitmap.createScaledBitmap(
                    poster, x, y, false);
            imageView.setImageBitmap(resizedPoster);
            imageView.setLayoutParams(new GridView.LayoutParams(x, y));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(0, 0, 0, 0);
            return imageView;
        }

        if (convertView == null) {
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(x, y));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(0, 0, 0, 0);
        } else {
            imageView = (ImageView) convertView;
        }

        String currentMoviePoster;
        currentMoviePoster = currentMovie.getPosterPath();

        String URLString = context.getResources().getString(R.string.poster_path_base_url) + currentMoviePoster;
        ImageQueryTask task = new ImageQueryTask(context, imageView, position);

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
        private final int position;

        public ImageQueryTask(Context context, ImageView img, int position) {
            imageViewReference = new WeakReference<ImageView>(img);
            this.context = context;
            this.position = position;
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
                if (imageView != null) {
                    imageView.setImageBitmap(bmp);
                }
                myMovieData[position].setPoster(bmp);
            }
        }
    }

}