package com.example.android.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by adamzarn on 6/1/17.
 */

public class MovieObject implements Parcelable {

    private String id;
    private String title;
    private String release_date;
    private String vote_average;
    private String plot_synopsis;
    private String poster_path;

    public MovieObject(String id, String title, String release_date, String vote_average, String plot_synopsis, String poster_path) {
        this.id = id;
        this.title = title;
        this.release_date = release_date;
        this.vote_average = vote_average;
        this.plot_synopsis = plot_synopsis;
        this.poster_path = poster_path;
    }

    public MovieObject(Parcel parcel) {
        this.id = parcel.readString();
        this.title = parcel.readString();
        this.release_date = parcel.readString();
        this.vote_average = parcel.readString();
        this.plot_synopsis = parcel.readString();
        this.poster_path = parcel.readString();
    }

    public String getID() { return id; }

    public String getTitle() {
        return title;
    }

    public String getReleaseDate() {
        return release_date;
    }

    public String getVoteAverage() {
        return vote_average;
    }

    public String getPlotSynopsis() {
        return plot_synopsis;
    }

    public String getPosterPath() {
        return poster_path;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(release_date);
        dest.writeString(vote_average);
        dest.writeString(plot_synopsis);
        dest.writeString(poster_path);
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
