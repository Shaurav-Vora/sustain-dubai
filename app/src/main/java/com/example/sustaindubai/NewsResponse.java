//package com.example.sustaindubai;
//
//import java.util.List;
//
//public class NewsResponse {
//    private String status;
//    private int totalResults;
//    private List<Article> articles;
//
//    public String getStatus() { return status; }
//    public List<Article> getArticles() { return articles; }
//}

package com.example.sustaindubai;

import java.util.List;

public class NewsResponse {
    // This list holds all the articles we just defined
    private List<NewsArticle> articles;

    public List<NewsArticle> getArticles() {
        return articles;
    }
}