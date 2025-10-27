package com.ex.realcv.MemoMain;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;


import com.ex.realcv.MainActivity;
import com.ex.realcv.R;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ex.realcv.TodoMain.TodoAct;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MemoBase extends MainActivity {

    // 특정 메모 핀 기능
    // 시크릿 모드 메모
    // 다크 모드
    // 예쁜 글씨체
    // 글씨 에디팅 -
    //메모 곂쳐지면 폴더 
    // 캐치 & 릴리즈로 메모의 문장을 (메모장 안에서 체크리스트끼리의 순서를 바꿀수있게)
    //휴지통
    // 메모 템플릿
    // 메모 폴더
    // 검색 기능 제목 내용 다 되게 & 파일이 들어간 메모만 검색도 가능하게
    // 메모 속에 pdf나 파일 upload를 통해 관련 데이터 관리 가능하게
        //-> 관련된 웹, 파일 바로 연결 가능하게
    //배경 꾸밀수있게
    //백업 -> sns 계정
    //고양
    //메모 바로가기 위젯



    private MemoAdapter adapter;

    private FileMemoRepository repo;
    private final Gson gson = new Gson();
    private static final String KEY = "memos_json";
    private static final String TAG = "MemoBase";

    enum ZoomLevel { OVERVIEW, TIME_FOCUS, DETAIL }
    private ZoomLevel zoom = ZoomLevel.DETAIL;

    ///////////test
    float x1,x2,y1,y2;


    private final ExecutorService io = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setName("Memo-IO");   // ← 로그에서 보이도록 이름 설정
        return t;
    });
    //private final ExecutorService io = Executors.newSingleThreadExecutor();
    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memo_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        RecyclerView rv = findViewById(R.id.rvMemo);
        LinearLayoutManager lm = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        lm.setReverseLayout(false);
        lm.setStackFromEnd(false);
        rv.setLayoutManager(lm);

        //저장소
        repo = new FileMemoRepository(getApplicationContext());
        //어답터 세팅
        //adapter = new MemoAdapter(list -> repo.save(list));
        adapter = new MemoAdapter(list -> {
           /* Log.d(TAG, "onChanged() called on thread=" + Thread.currentThread().getName()
                    + " | scheduling save(size=" + list.size() + ")");*/
            io.execute(() -> {
              /*  Log.d(TAG, "save runnable running on thread=" + Thread.currentThread().getName());*/
                repo.save(list);
            });
        });
        rv.setAdapter(adapter);
        adapter.setItems(repo.activeMemo());



        // 어댑터 생성 후…
        // L3 Layer
        adapter.setOnItemClick((position, memo) -> {
            MemoDialog dlg = MemoDialog.newInstance(memo.text); // 기존 내용 채워서 열기

            dlg.setListener(newText -> {
                if (newText.isEmpty() || newText.equals(memo.text)) return; // 변경 없음/빈 값 무시
                adapter.update(position, newText);   // 화면 갱신
                repo.updateText(memo.id, newText); // Repository 쓰면 저장
            });
            dlg.show(getSupportFragmentManager(), "memo_edit");
        });

        //L2 Layer
       /* focusApt = new MemoFocusAdapter((pos, item) -> {
            // 썸네일 클릭 시 상세로 전환(or 해당 날짜 필터 등)
            setZoom(ZoomLevel.DETAIL);
            // 필요하면 스크롤/필터 적용
        });*/

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> {
            MemoDialog dlg = MemoDialog.newInstance(null);
            dlg.setListener(text -> {
                if (!text.isEmpty()) {
                    adapter.add(new Memo(UUID.randomUUID().toString(), text, false));
                    rv.post(() -> {
                        // 바로 위로 끌어올리기(둘 중 하나)
                        lm.scrollToPositionWithOffset(0, 0); // 정확히 맨 위
                        rv.smoothScrollToPosition(0);     // 부드럽게 스크롤
                    });
                }
                // 여기서 SharedPreferences/DB 저장도 함께 수행하면 됨
            });
            dlg.show(getSupportFragmentManager(), "memo_edit");
        });

        // 스와이프로 삭제
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, 0) {

            @Override public boolean onMove(RecyclerView rv, RecyclerView.ViewHolder vh, RecyclerView.ViewHolder t) {
                return false;
            }

            // 항목별로 스와이프 가능/불가능을 결정
            @Override public int getMovementFlags(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh) {
                int pos = vh.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return 0;

                // Memo 모델에 체크 여부가 있다고 가정 (e.g., boolean checked)
                Memo item = adapter.getItem(pos); // 필요시 adapter에서 꺼내는 메서드 구현
                boolean canSwipe = (item != null && item.done); // 또는 item.checked

                int swipeFlags = canSwipe ? (ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) : 0;
                return makeMovementFlags(0, swipeFlags);
            }

            @Override public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                adapter.remove(vh.getBindingAdapterPosition());
            }

            // (선택) 스와이프 임계값을 좀 더 빡세게
            // @Override public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) { return 0.5f; }
        });
        helper.attachToRecyclerView(rv);


        ///////menu
        //Log.d("ccccc", "cccccc");
        // 일반 메뉴로 이동
        findViewById(R.id.ChangeDomain).setOnClickListener(v -> {
            startActivity(new Intent(this, TodoAct.class));
        });
        
        //Layer 관련

        findViewById(R.id.ChangeLayer).setOnClickListener(v -> {
            Log.d("Layer!!!", "Current Layer is : " + zoom);
            // 간단 순환: DETAIL → TIME_FOCUS → OVERVIEW → DETAIL …
            ZoomLevel next =
                    (zoom == ZoomLevel.DETAIL)     ? ZoomLevel.TIME_FOCUS :
                            (zoom == ZoomLevel.TIME_FOCUS) ? ZoomLevel.OVERVIEW   :
                                    ZoomLevel.DETAIL;
           // setZoom(next);
        });


        // 저장된 목록 불러오기
        //adapter.setItems(repo.load());

    }

    public boolean onTouchEvent(MotionEvent touchevent){

        switch(touchevent.getAction()){
            case MotionEvent.ACTION_DOWN:
                x1 = touchevent.getX();
                y1 = touchevent.getY();
                break;
            case MotionEvent.ACTION_UP:
                x2 = touchevent.getX();
                y2 = touchevent.getY();
                if( x1 < x2 ) {
                    Intent i = new Intent(MemoBase.this, MainActivity.class);
                    startActivity(i);
                }
                break;

        }
        return false;

    }

    @Override protected void onDestroy() {
        super.onDestroy();
        io.shutdown(); // 누수 방지
    }



   /* void setZoom(ZoomLevel z) {

        zoom = z;

        //View rvOverview = findViewById(R.id.rvOverview);
        //View rvTimeFocus = findViewById(R.id.panelTimeFocus);
        View panelListMemo = findViewById(R.id.panelList);

        //rvOverview.setVisibility(View.GONE);
       // rvTimeFocus.setVisibility(View.GONE);
        panelListMemo.setVisibility(View.GONE);

        switch (z) {
         //   case OVERVIEW:    rvOverview.setVisibility(View.VISIBLE); break;
          //  case TIME_FOCUS:  rvTimeFocus.setVisibility(View.VISIBLE); break;
            case DETAIL:      panelListMemo.setVisibility(View.VISIBLE); break;
        }

    }*/

/*    private void showAddDialog() {
        EditText et = new EditText(this);
        et.setHint("메모 내용");
        et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        new AlertDialog.Builder(this)
                .setTitle("메모 추가")
                .setView(et)
                .setPositiveButton("추가", (d, w) -> {
                    String t = et.getText().toString().trim();
                    if (!t.isEmpty()) adapter.add(new Memo(UUID.randomUUID().toString(), t, false));
                })
                .setNegativeButton("취소", null)
                .show();
    }*/


}
