package com.example.sustaindubai;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NewsApiService {

    @GET("v2/everything")
    Call<NewsResponse> getSustainabilityNews(
            @Query("q") String query,       // What we search for (e.g., "Sustainability")
            @Query("apiKey") String apiKey  // Your password
    );
}