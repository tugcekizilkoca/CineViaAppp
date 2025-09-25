package com.example.cineviaapp;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FavoritesActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    FavoritesAdapter adapter;
    List<Movie> favoriteMovies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);  // XML dosya adı doğru mu kontrol et

        recyclerView = findViewById(R.id.favoritesRecyclerView);  // ID XML ile uyumlu
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        SharedPreferences prefs = getSharedPreferences("MoviePrefs", MODE_PRIVATE);
        Map<String, ?> allEntries = prefs.getAll();

        favoriteMovies = new ArrayList<>();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String title = entry.getKey();

            int movieId = 0;
            String genre = "Bilim Kurgu";
            String posterPath = "/placeholder.jpg";
            String overview = "Açıklama yok";
            String posterUrl = "https://via.placeholder.com/70x90.png?text=Poster";

            favoriteMovies.add(new Movie(movieId, title, genre, posterUrl, overview, posterPath));
        }

        adapter = new FavoritesAdapter(this, favoriteMovies);
        recyclerView.setAdapter(adapter);
    }
}

