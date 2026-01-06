package com.ex.realcv.WordCard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

import com.ex.realcv.MainActivity;
import com.ex.realcv.MemoMain.MemoBase;
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

        findViewById(R.id.cardDaily).setOnClickListener(v ->{
                startActivity(new Intent(this, CardView.class));
        });
        findViewById(R.id.cardBusiness).setOnClickListener(v -> Log.d("LearnView" , "cardBusiness click"));
        findViewById(R.id.cardExam).setOnClickListener(v -> Log.d("LearnView" , "cardExam click"));

        findViewById(R.id.cardKana).setOnClickListener(v -> Log.d("LearnView" , "cardKana click"));
        findViewById(R.id.cardKanji).setOnClickListener(v -> Log.d("LearnView" , "cardKanji click"));





    }

}
