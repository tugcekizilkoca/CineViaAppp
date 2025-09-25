package com.example.cineviaapp;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WatchLaterActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    WatchLaterAdapter adapter;
    List<Movie> watchLaterMovies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_later);

        recyclerView = findViewById(R.id.watchLaterRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        SharedPreferences prefs = getSharedPreferences("WatchLaterPrefs", MODE_PRIVATE);
        Map<String, ?> allEntries = prefs.getAll();

        watchLaterMovies = new ArrayList<>();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String title = entry.getKey();

            // Placeholder değerler — istersen prefs'ten gerçek verileri de çekebilirsin
            int movieId = 0;
            String genre = "Bilim Kurgu";
            String posterUrl = "https://via.placeholder.com/70x90.png?text=Poster";
            String overview = "Açıklama yok";
            String posterPath = "/placeholder.jpg";

            watchLaterMovies.add(new Movie(movieId, title, genre, posterUrl, overview, posterPath));
        }

        adapter = new WatchLaterAdapter(this, watchLaterMovies);
        recyclerView.setAdapter(adapter);
    }
}
