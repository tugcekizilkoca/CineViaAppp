package com.example.cineviaapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MovieDetail extends AppCompatActivity {

    private static final String API_KEY = "6899ac0ff0fb9dbbf05efbec9c8187b2";

    ImageView imagePoster;
    TextView  textTitle, textDetails, textMeta, textCast;
    CheckBox  checkBoxFavorite, chcDahasonra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        imagePoster      = findViewById(R.id.imagePoster);
        textTitle        = findViewById(R.id.textTitle);
        textDetails      = findViewById(R.id.textDetails);
        textMeta         = findViewById(R.id.textMeta);   // ⭐️ yeni
        textCast         = findViewById(R.id.textCast);   // ⭐️ yeni
        checkBoxFavorite = findViewById(R.id.chcFavoriler);
        chcDahasonra     = findViewById(R.id.chcDahasonra);

        // ───── Intent verileri ─────
        int    movieId    = getIntent().getIntExtra("movie_id", -1);
        String title      = getIntent().getStringExtra("title");
        String overview   = getIntent().getStringExtra("overview");
        String posterPath = getIntent().getStringExtra("poster_path");
        String imageUrl   = "https://image.tmdb.org/t/p/w500" + posterPath;

        // ───── UI’ye yerleştir ─────
        Picasso.get().load(imageUrl).into(imagePoster);
        textTitle.setText(title);
        textDetails.setText(overview);

        // ───── Favoriler / Daha Sonra İzle ayarı ─────
        SharedPreferences favPrefs = getSharedPreferences("MoviePrefs", MODE_PRIVATE);
        checkBoxFavorite.setChecked(favPrefs.getBoolean(title, false));
        checkBoxFavorite.setOnCheckedChangeListener((b, isChecked) -> {
            favPrefs.edit().putBoolean(title, isChecked).apply();
            Toast.makeText(this,
                    isChecked ? "Favorilere eklendi" : "Favorilerden çıkarıldı",
                    Toast.LENGTH_SHORT).show();
        });

        SharedPreferences laterPrefs = getSharedPreferences("WatchLaterPrefs", MODE_PRIVATE);
        chcDahasonra.setChecked(laterPrefs.contains(title));
        chcDahasonra.setOnCheckedChangeListener((b, isChecked) -> {
            SharedPreferences.Editor ed = laterPrefs.edit();
            if (isChecked) ed.putBoolean(title, true);
            else           ed.remove(title);
            ed.apply();
        });

        // ───── Film meta + oyuncu kadrosu TMDB’den çek ─────
        if (movieId != -1) fetchMovieExtra(movieId);
    }

    /** TMDB “/movie/{id}” ve “/movie/{id}/credits” çağrıları */
    private void fetchMovieExtra(int movieId) {
        RequestQueue q = Volley.newRequestQueue(this);

        // ▸ Detay (süre, yıl, türler)
        String urlDetails = "https://api.themoviedb.org/3/movie/" + movieId +
                "?api_key=" + API_KEY + "&language=tr-TR";
        q.add(new JsonObjectRequest(Request.Method.GET, urlDetails, null,
                obj -> {
                    try {
                        int    runtime   = obj.optInt("runtime");
                        String relDate   = obj.optString("release_date"); // yyyy-MM-dd
                        String year      = relDate.length() >= 4 ? relDate.substring(0,4) : "—";

                        JSONArray genresArr = obj.getJSONArray("genres");
                        StringBuilder gBuf = new StringBuilder();
                        for (int i=0;i<genresArr.length();i++){
                            if (i>0) gBuf.append(", ");
                            gBuf.append(genresArr.getJSONObject(i).getString("name"));
                        }
                        String meta = year + "  •  " + runtime + " dk  •  " + gBuf;
                        textMeta.setText(meta);
                    } catch (JSONException e) { e.printStackTrace(); }
                },
                err -> {}
        ));



        // ▸ Oyuncu kadrosu
        String urlCredits = "https://api.themoviedb.org/3/movie/" + movieId +
                "/credits?api_key=" + API_KEY + "&language=tr-TR";
        q.add(new JsonObjectRequest(Request.Method.GET, urlCredits, null,
                obj -> {
                    try {
                        JSONArray castArr = obj.getJSONArray("cast");
                        StringBuilder cBuf = new StringBuilder();
                        for (int i=0;i<Math.min(5, castArr.length()); i++){
                            if (i>0) cBuf.append(", ");
                            cBuf.append(castArr.getJSONObject(i).getString("name"));
                        }
                        textCast.setText("Oyuncular: " + cBuf);
                    } catch (JSONException e) { e.printStackTrace(); }
                },
                err -> {}
        ));
    }
}


