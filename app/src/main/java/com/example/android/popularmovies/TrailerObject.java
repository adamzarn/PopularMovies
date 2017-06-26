package com.example.android.popularmovies;

/**
 * Created by adamzarn on 6/25/17.
 */

public class TrailerObject {

    private String key;
    private String name;

    public TrailerObject(String key, String name) {
        this.key = key;
        this.name = name;
    }

    public String getKey() { return key; }

    public String getName() {
        return name;
    }
}
