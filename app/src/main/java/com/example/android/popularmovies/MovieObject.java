package com.example.android.popularmovies;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by adamzarn on 6/1/17.
 */

public class MovieObject implements Parcelable {

    private String id;
    private String title;
    private String releaseDate;
    private String voteAverage;
    private String plotSynopsis;
    private String posterPath;
    private Bitmap poster;

    public MovieObject(String id, String title, String releaseDate, String voteAverage, String plotSynopsis, String posterPath, Bitmap poster) {
        this.id = id;
        this.title = title;
        this.releaseDate = releaseDate;
        this.voteAverage = voteAverage;
        this.plotSynopsis = plotSynopsis;
        this.posterPath = posterPath;
        this.poster = poster;
    }

    public MovieObject(Parcel parcel) {
        this.id = parcel.readString();
        this.title = parcel.readString();
        this.releaseDate = parcel.readString();
        this.voteAverage = parcel.readString();
        this.plotSynopsis = parcel.readString();
        this.posterPath = parcel.readString();
        this.poster = (Bitmap) parcel.readValue(Bitmap.class.getClassLoader());
    }

    public String getID() { return id; }

    public String getTitle() {
        return title;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getVoteAverage() {
        return voteAverage;
    }

    public String getPlotSynopsis() {
        return plotSynopsis;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public Bitmap getPoster() { return poster; }

    public void setPoster(Bitmap poster) {
        this.poster = poster;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(releaseDate);
        dest.writeString(voteAverage);
        dest.writeString(plotSynopsis);
        dest.writeString(posterPath);
        dest.writeValue(poster);
    }

    public static final Parcelable.Creator<MovieObject> CREATOR
            = new Parcelable.Creator<MovieObject>() {

        public MovieObject createFromParcel(Parcel in) {
            return new MovieObject(in);
        }

        public MovieObject[] newArray(int size) {
            return new MovieObject[size];
        }
    };

}
