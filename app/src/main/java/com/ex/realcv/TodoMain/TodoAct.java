package com.ex.realcv.TodoMain;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;

import com.ex.realcv.FaceActivity;
import com.ex.realcv.MainActivity;
import com.ex.realcv.R;

import org.opencv.android.OpenCVLoader;

public class TodoAct extends MainActivity {


    private CustomGLSurfaceView glView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.active_todomain);

        glView = findViewById(R.id.glView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        glView.onResume();  // OpenGL 렌더링 다시 시작
    }

    @Override
    protected void onPause() {
        super.onPause();
        glView.onPause();  // OpenGL 렌더링 중지
    }

}
