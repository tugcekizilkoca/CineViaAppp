package com.example.cineviaapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HomeScreen extends AppCompatActivity {

    private final String API_KEY = "6899ac0ff0fb9dbbf05efbec9c8187b2";
    private final String[] categories = {"Türk Yapımı", "Top 10", "Aksiyon", "Komedi", "Bilim Kurgu"};
    private final String[] categoryEndpoints = {
            "&with_origin_country=TR",
            "&sort_by=popularity.desc",
            "&with_genres=28",
            "&with_genres=35",
            "&with_genres=878"
    };

    ImageButton btnProfile, btnLists, btnSinemalar, btnChatbot;
    Button btnfilmoner;
    LinearLayout sectionContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        btnProfile = findViewById(R.id.profileButton);
        btnLists = findViewById(R.id.btnLists);
        btnSinemalar = findViewById(R.id.btnSinemalar);
        btnChatbot = findViewById(R.id.btnChatbot);
        btnfilmoner = findViewById(R.id.btnfilmoner);
        sectionContainer = findViewById(R.id.sectionContainer);

        btnProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        btnChatbot.setOnClickListener(view -> {
            Intent intent = new Intent(HomeScreen.this, ChatBot.class);
            startActivity(intent);
        });

        btnfilmoner.setOnClickListener(view -> {
            Intent intent = new Intent(HomeScreen.this, DuyguAnaliz.class);
            startActivity(intent);
        });

        btnLists.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(HomeScreen.this, v);
            MenuInflater inflater = popup.getMenuInflater();

            popup.getMenu().add("Daha Sonra İzle");
            popup.getMenu().add("Favoriler");

            SharedPreferences sharedPreferences = getSharedPreferences("MoviePrefs", MODE_PRIVATE);
            String favoriteMovieTitle = sharedPreferences.getString("favorite_movie", null);

            if (favoriteMovieTitle != null) {
                popup.getMenu().add("Favori Film: " + favoriteMovieTitle);
            }

            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();

                if (title.equals("Favoriler")) {
                    startActivity(new Intent(HomeScreen.this, FavoritesActivity.class));
                } else if (title.startsWith("Favori Film")) {
                    Intent intent = new Intent(HomeScreen.this, MovieDetail.class);
                    intent.putExtra("title", favoriteMovieTitle);
                    startActivity(intent);
                } else if (title.equals("Daha Sonra İzle")) {
                    startActivity(new Intent(HomeScreen.this, WatchLaterActivity.class));
                }

                return true;
            });

            popup.show();
        });

        btnSinemalar.setOnClickListener(v -> startActivity(new Intent(HomeScreen.this, NearbyCinemas.class)));

        for (int i = 0; i < categories.length; i++) {
            fetchAndDisplayCategory(categories[i], categoryEndpoints[i]);
        }
    }

    // Çoklu sayfa verisi çekme
    private void fetchAndDisplayCategory(String categoryTitle, String query) {
        RequestQueue queue = Volley.newRequestQueue(this);
        JSONArray combinedResults = new JSONArray();
        fetchPagesRecursive(queue, categoryTitle, query, 1, combinedResults);
    }

    private void fetchPagesRecursive(RequestQueue queue, String categoryTitle, String query, int page, JSONArray combinedResults) {
        if (page > 20) {
            addMovieSection(categoryTitle, combinedResults);
            return;
        }

        String url = "https://api.themoviedb.org/3/discover/movie?api_key=" + API_KEY +
                "&language=tr-TR&page=" + page + query;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");
                        for (int i = 0; i < results.length(); i++) {
                            combinedResults.put(results.getJSONObject(i));
                        }
                        fetchPagesRecursive(queue, categoryTitle, query, page + 1, combinedResults);
                    } catch (JSONException e) {
                        Log.e("ParseError", e.getMessage());
                    }
                },
                error -> Log.e("APIError", error.toString())
        );

        queue.add(jsonObjectRequest);
    }

    private void addMovieSection(String title, JSONArray movies) {
        LinearLayout sectionLayout = new LinearLayout(this);
        sectionLayout.setOrientation(LinearLayout.VERTICAL);
        sectionLayout.setPadding(0, dpToPx(16), 0, dpToPx(16));

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextSize(20);
        titleView.setTextColor(getResources().getColor(android.R.color.white));
        titleView.setPadding(dpToPx(8), 0, 0, dpToPx(8));
        sectionLayout.addView(titleView);

        HorizontalScrollView scrollView = new HorizontalScrollView(this);
        scrollView.setHorizontalScrollBarEnabled(false);

        LinearLayout rowLayout = new LinearLayout(this);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        rowLayout.setPadding(dpToPx(8), 0, dpToPx(8), 0);

        for (int i = 0; i < Math.min(movies.length(), 200); i++) { // Gerekirse burada sınırı arttırabilirsin
            try {
                JSONObject movie = movies.getJSONObject(i);

                int id = movie.getInt("id");
                String posterPath = movie.getString("poster_path");
                String fullPoster = "https://image.tmdb.org/t/p/w500" + posterPath;
                String titleTxt = movie.getString("title");
                String overview = movie.getString("overview");

                final int fId = id;
                final String fTitle = titleTxt;
                final String fOver = overview;
                final String fPath = posterPath;

                ImageView poster = new ImageView(this);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dpToPx(120), dpToPx(180));
                layoutParams.setMargins(dpToPx(4), 0, dpToPx(4), 0);
                poster.setLayoutParams(layoutParams);
                poster.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Glide.with(this).load(fullPoster).into(poster);

                poster.setOnClickListener(view -> {
                    Intent intent = new Intent(HomeScreen.this, MovieDetail.class);
                    intent.putExtra("movie_id", fId);
                    intent.putExtra("title", fTitle);
                    intent.putExtra("overview", fOver);
                    intent.putExtra("poster_path", fPath);
                    startActivity(intent);
                });

                rowLayout.addView(poster);
            } catch (JSONException e) {
                Log.e("MovieError", e.getMessage());
            }
        }

        scrollView.addView(rowLayout);
        sectionLayout.addView(scrollView);
        sectionContainer.addView(sectionLayout);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
