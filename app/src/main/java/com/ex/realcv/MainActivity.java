package com.ex.realcv;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;

import android.util.Log;
import org.opencv.android.OpenCVLoader;


import android.content.Intent;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

import com.ex.realcv.Camera.CameraMain;
import com.ex.realcv.MemoMain.MemoBase;
import com.ex.realcv.TodoMain.TodoAct;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        //full screen mode
        hideSystemBars();

        출처: https://tekken5953.tistory.com/2 [개발새발 - IT 기술블로그:티스토리]

        if (OpenCVLoader.initLocal()) {
            Log.d("OpenCV", "OpenCV loaded (local)");
        } else {
            Log.e("OpenCV", "OpenCV load failed");
        }

        findViewById(R.id.btnGetStart).setOnClickListener(v -> {
            startActivity(new Intent(this, MemoBase.class));
        });

        findViewById(R.id.btnCapture).setOnClickListener(v -> {
            startActivity(new Intent(this, CameraMain.class));
        });
      //  ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
      //      Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
       //     v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
      //      return insets;
      //  });
    }

    private void hideSystemBars() {
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            // Android 11+
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.systemBars()); // 상태바+내비게이션바
                controller.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                ); // 화면 가장자리에서 스와이프로 일시적으로 표시
            }
        } else {
            // Android 10 이하 (Deprecated API)
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
            );
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemBars();
    }
}