package com.ex.realcv.TodoMain;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;

import com.ex.realcv.FaceActivity;
import com.ex.realcv.MainActivity;
import com.ex.realcv.MemoMain.FileMemoRepository;
import com.ex.realcv.MemoMain.Memo;
import com.ex.realcv.MemoMain.MemoBase;
import com.ex.realcv.R;

import org.opencv.android.OpenCVLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TodoAct extends MainActivity {


    private CustomGLSurfaceView glView;

    //메모 관련 저장 데이터 불러오기
    private FileMemoRepository repo;

    //-----for UI
    // UI 변수 선언만
    private ImageButton btnAdd;
    private LinearLayout fabMenu;
    //-----

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.active_todomain);

        glView = findViewById(R.id.glView);
        //데이터 넘겨 주기
        repo = new FileMemoRepository(getApplicationContext());
        ExecutorService io = Executors.newSingleThreadExecutor();


        List<Memo> memos = repo.load();
        if (memos == null) memos = new ArrayList<>();

        List<Memo> finalMemos = memos;
        runOnUiThread(() -> {
            glView.setMemos(finalMemos);   // UI 스레드에서 호출해도 내부에서 queueEvent 처리
        });

        // setContentView 이후에 초기화
        btnAdd = findViewById(R.id.btnAdd);
        fabMenu = findViewById(R.id.fabMenu);


        //메뉴 팝업 버튼 +버튼
        btnAdd.setOnClickListener(v -> {
            if (fabMenu.getVisibility() == View.GONE) {
                fabMenu.setVisibility(View.VISIBLE);
                fabMenu.setAlpha(0f);
                fabMenu.animate().alpha(1f).setDuration(300).start();
            } else {
                fabMenu.animate().alpha(0f).setDuration(300).withEndAction(() ->
                        fabMenu.setVisibility(View.GONE)
                ).start();
            }
        });

        // 일반 메뉴로 이동
        findViewById(R.id.ChangeDomain).setOnClickListener(v -> {
            startActivity(new Intent(this, MemoBase.class));
        });

        // 새로운 메모 행성 만들기
        ImageButton newMemoPlanet = findViewById(R.id.AddMemoPlanet);
        newMemoPlanet.setOnClickListener(v -> {
            // 1. 카메라 이동 (우주로 좀 더 전진)
            Log.d("TodoAct", "➕ AddMemoPlanet 왜????버튼 클릭됨!");
            glView.MoveToNewArea();
        });
    }

    /*@Override
    protected void onResume() {
        super.onResume();
        glView.onResume();  // OpenGL 렌더링 다시 시작
    }

    @Override
    protected void onPause() {
        super.onPause();
        glView.onPause();  // OpenGL 렌더링 중지
    }*/

}
