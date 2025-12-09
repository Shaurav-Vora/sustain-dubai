package com.example.sustaindubai;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NewsFragment extends Fragment {

    // 🔴 REPLACE THIS WITH YOUR REAL KEY FROM THE WEBSITE!
    private static final String API_KEY = "04eb95d6b0214e3597d16682fa77fb86";

    private LinearLayout newsContainer;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);

        newsContainer = view.findViewById(R.id.news_container);
        progressBar = view.findViewById(R.id.progressBar);

        // Start fetching news immediately
        fetchNews();

        return view;
    }

    private void fetchNews() {
        // Show the spinner before we start
        progressBar.setVisibility(View.VISIBLE);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://newsapi.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        NewsApiService apiService = retrofit.create(NewsApiService.class);

        // Strict Eco Query
        String strictQuery = "climate OR environment OR recycling OR solar OR energy";

        Call<NewsResponse> call = apiService.getSustainabilityNews(strictQuery, API_KEY);
        call.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                // Hide spinner when data arrives
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    displayNews(response.body());
                } else {
                    Toast.makeText(getContext(), "Failed to get news: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                // Hide spinner even if it fails
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("NewsFragment", "Error fetching news", t);
            }
        });
    }

    private void displayNews(NewsResponse newsResponse) {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (NewsArticle article : newsResponse.getArticles()) {
            View cardView = inflater.inflate(R.layout.item_news, newsContainer, false);

            ImageView image = cardView.findViewById(R.id.imgNews);
            TextView title = cardView.findViewById(R.id.tvTitle);
            TextView desc = cardView.findViewById(R.id.tvDescription);
            TextView date = cardView.findViewById(R.id.tvDate); // Make sure to bind date/source if needed
            // TextView source = cardView.findViewById(R.id.tvSource);

            title.setText(article.getTitle());

            // Handle null description
            if (article.getDescription() != null) {
                desc.setText(article.getDescription());
            } else {
                desc.setVisibility(View.GONE);
            }

            if (article.getImageUrl() != null) {
                Glide.with(this)
                        .load(article.getImageUrl())
                        .into(image);
            }

            cardView.setOnClickListener(v -> {
                String articleUrl = article.getArticleUrl();
                if (articleUrl != null) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(articleUrl));
                    startActivity(browserIntent);
                } else {
                    Toast.makeText(getContext(), "No link available", Toast.LENGTH_SHORT).show();
                }
            });

            newsContainer.addView(cardView);
        }
    }
}