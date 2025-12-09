package com.example.sustaindubai;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        newsContainer = view.findViewById(R.id.news_container);

        // Start fetching news immediately when screen loads
        fetchNews();

        return view;
    }

    private void fetchNews() {
        // 1. Build the Retrofit "Telephone"
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://newsapi.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // 2. Create the API Service
        NewsApiService apiService = retrofit.create(NewsApiService.class);

        // 3. Make the call (Search for "Sustainability" in "Dubai")
        // We use "sustainability OR dubai" to get more results
        Call<NewsResponse> call = apiService.getSustainabilityNews("environment OR recycling OR renewable energy OR sustainability OR UAE", API_KEY);
        call.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // We got data! Now let's put it on the screen.
                    displayNews(response.body());
                } else {
                    Toast.makeText(getContext(), "Failed to get news: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("NewsFragment", "Error fetching news", t);
            }
        });
    }

    private void displayNews(NewsResponse newsResponse) {
        // Clear any old data
        // newsContainer.removeAllViews(); // Optional: keep the title header

        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (NewsArticle article : newsResponse.getArticles()) {
            // 1. Inflate the "Cookie Cutter" template (item_news.xml)
            View cardView = inflater.inflate(R.layout.item_news, newsContainer, false);

            // 2. Find the text/image spots INSIDE that specific card
// ✅ NEW IDs matching the modern XML
            ImageView image = cardView.findViewById(R.id.imgNews);       // Changed from news_image
            TextView title = cardView.findViewById(R.id.tvTitle);        // Changed from news_title
            TextView desc = cardView.findViewById(R.id.tvDescription);   // Changed from news_description

            // 3. Fill them with data
            title.setText(article.getTitle());
            desc.setText(article.getDescription());

            // 4. Load the image using Glide
            if (article.getImageUrl() != null) {
                Glide.with(this)
                        .load(article.getImageUrl())
                        .into(image);
            }
            cardView.setOnClickListener(v -> {
                String articleUrl = article.getArticleUrl();
                if (articleUrl != null) {
                    // Open the link in the phone's browser (Chrome/Safari)
                    android.content.Intent browserIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(articleUrl));
                    startActivity(browserIntent);
                } else {
                    Toast.makeText(getContext(), "No link available for this article", Toast.LENGTH_SHORT).show();
                }
            });

            // 5. Add the finished card to the main screen
            newsContainer.addView(cardView);
        }
    }
}