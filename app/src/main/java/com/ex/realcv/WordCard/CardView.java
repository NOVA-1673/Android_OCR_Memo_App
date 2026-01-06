package com.ex.realcv.WordCard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.ex.realcv.DB.WordCard.ProgressHost;
import com.ex.realcv.R;

public class CardView extends AppCompatActivity implements ProgressHost {


    private ProgressBar progressBar;
    private TextView tvProgress;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wordcard_card_activity);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        tvProgress = findViewById(R.id.tvProgress);
        progressBar = findViewById(R.id.progressBar);


        initNavigation();

        String mode = getIntent().getStringExtra("extra_mode");
        if (mode == null) mode = "DAILY";

        if (savedInstanceState == null) {
            Bundle args = new Bundle();
            args.putString("extra_mode", mode);

            CardFragment fragment = new CardFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.wordCardContainer, fragment)
                    .commit();
        }

    }

    private void initNavigation(){

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

    }

    @Override
    public void onProgressChanged(int current, int total) {
        tvProgress.setText("진행도 " + current + " / " + total);

        if (total <= 0) {
            progressBar.setMax(1);
            progressBar.setProgress(0);
            return;
        }

        progressBar.setMax(total);
        progressBar.setProgress(current,true);

    }
}
