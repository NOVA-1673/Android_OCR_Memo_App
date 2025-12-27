package com.ex.realcv.WordCard;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

import com.ex.realcv.MainActivity;
import com.ex.realcv.R;

public class CardBase extends MainActivity {


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wordcard_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        initNavigation();

    }


    private void initNavigation(){

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

}
