package com.example.sustaindubai;

public class NewsArticle {
    // These names must match the API *exactly*
    private String title;
    private String description;
    private String urlToImage;
    private String url;

    // Getters so the app can read the data
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return urlToImage;
    }

    public String getArticleUrl() {
        return url;
    }
}