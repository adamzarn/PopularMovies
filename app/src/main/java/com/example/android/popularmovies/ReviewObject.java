package com.example.android.popularmovies;

/**
 * Created by adamzarn on 6/26/17.
 */

public class ReviewObject {

    private String author;
    private String content;
    private String url;

    public ReviewObject(String author, String content, String url) {
        this.author = author;
        this.content = content;
        this.url = url;
    }

    public String getAuthor() { return author; }

    public String getContent() { return content; }

    public String getUrl() { return url; }
}
