//package com.example.sustaindubai;
//
//public class Article {
//    // Fields matching the JSON response
//    private String title;
//    private String description;
//    private String url;
//    private String urlToImage;
//    private String publishedAt;
//    private Source source;
//
//    // Getters
//    public String getTitle() { return title; }
//    public String getDescription() { return description; }
//    public String getUrl() { return url; }
//    public String getUrlToImage() { return urlToImage; }
//    public String getPublishedAt() { return publishedAt; }
//
//    // Helper to safely get source name
//    public String getSourceName() {
//        return source != null ? source.name : "News";
//    }
//
//    // Inner class for the "source": { "id": "...", "name": "..." } part
//    public static class Source {
//        private String name;
//    }
//}

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