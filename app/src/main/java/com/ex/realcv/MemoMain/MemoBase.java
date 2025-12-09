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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.ImageView;

//아이콘 색 바꾸기
import androidx.core.content.ContextCompat;

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
    private BlocksAdapter BlockAdapter;

    private FileMemoRepository repo;
    private final Gson gson = new Gson();
    private static final String KEY = "memos_json";
    private static final String TAG = "MemoBase";

    enum ZoomLevel { OVERVIEW, TIME_FOCUS, DETAIL }
    private ZoomLevel zoom = ZoomLevel.DETAIL;

    ///////////test
    float x1,x2,y1,y2;

    //다중 클릭 시 렉 유발 제거를 위한
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingSubmit;

    //swipe helper
    private ItemTouchHelper swipeHelperMode;
    private ArrayList<Memo> dragTemp;
    private boolean showingTrash  =false;


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

        //스와이프 삭제 초기 설정
        swipeHelperMode = setTouchHelper(1);
        swipeHelperMode.attachToRecyclerView(rv);

        //어답터 세팅
        //adapter = new MemoAdapter(list -> repo.save(list));
       /* adapter = new MemoAdapter(list -> {
           *//* Log.d(TAG, "onChanged() called on thread=" + Thread.currentThread().getName()
                    + " | scheduling save(size=" + list.size() + ")");*//*
            io.execute(() -> {
              *//*  Log.d(TAG, "save runnable running on thread=" + Thread.currentThread().getName());*//*
                repo.save(list);
            });
        });*/
        adapter = new MemoAdapter(new MemoAdapter.Listener() {
            @Override public void onToggleDone(Memo m, boolean checked) {
                if (showingTrash) {
                    // 휴지통 모드: 팝업 띄우고 확인 시 복구
                    showRestoreConfirmDialog(MemoBase.this, m.getId());
                    return;
                }
                io.execute(() -> {
                    if (showingTrash) {
                        // 🗑 휴지통 모드일 때 → 복구
                        repo.restore(m.getId());
                    } else {
                        // ✅ 일반 모드일 때 → 완료/미완료 토글
                        repo.toggleDone(m.getId(), checked);
                    }         // 미세 동작
                   // List<Memo> fresh = repo.activeMemo();     // 캐시에서 조회(필터/정렬 포함)
                    //runOnUiThread(() -> submitSafely(fresh));
                    List<Memo> fresh = showingTrash
                            ? repo.softDeletedMemo()
                            : repo.activeMemo();
                    submitSafely(fresh);
                });
            }
            @Override public void onItemClick(Memo m) {
                // 상세/편집 진입 등
               /* MemoDialog dlg = MemoDialog.newInstance(m.text); // 기존 내용 채워서 열기

                dlg.setListener(newText -> {
                    if (newText.isEmpty() || newText.equals(m.text)) return; // 변경 없음/빈 값 무시
                    //adapter.update(position, newText);   // 화면 갱신
                    io.execute(() -> {
                        repo.updateText(m.id, newText);
                        //List<Memo> fresh = repo.activeMemo();
                        //submitSafely(fresh); // 이미 UI 핸들러로 post됨
                        List<Memo> fresh = showingTrash
                                ? repo.softDeletedMemo()
                                : repo.activeMemo();
                        submitSafely(fresh);
                    });

                });
                dlg.show(getSupportFragmentManager(), "memo_edit");*/
                ArrayList<BlockMemo> initial = repo.loadBlocks(m.id);

                // 2) 메모별로 고유 결과키 생성
                String resultKey = "memo_result_" + m.id;

                // 3) 결과 리스너(한 번만 받도록 등록)
                getSupportFragmentManager().setFragmentResultListener(resultKey, MemoBase.this, (reqKey, bundle) -> {
                    @SuppressWarnings("unchecked")
                    ArrayList<BlockMemo> blocks = (ArrayList<BlockMemo>) bundle.getSerializable("blocks");
                    if (blocks == null) return;

                    io.execute(() -> {
                        // 4) 블록 저장 (문자열 필드에 BLOCKS_JSON:...로 저장)
                        repo.updateBlocks(m.id, blocks);

                        List<Memo> fresh = showingTrash
                                ? repo.softDeletedMemo()
                                : repo.activeMemo();
                        submitSafely(fresh);
                    });
                });

                // 5) 다이얼로그 띄우기 (결과키와 초기 블록 전달)
                MemoBlockDialog
                        .newInstance(initial, resultKey)   // ← resultKey를 같이 넘김
                        .show(getSupportFragmentManager(), "memo_edit");
            }

            @Override public void onStartDrag(RecyclerView.ViewHolder holder) {
                swipeHelperMode.startDrag(holder);
            }
        });
        rv.setAdapter(adapter);
        adapter.setTrashMode(false);
        //adapter.setItems(repo.activeMemo());
        adapter.submitList(repo.activeMemo());



        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> {
            // 1) 초기 빈 블록
            ArrayList<BlockMemo> initial = new ArrayList<>();
            initial.add(BlockMemo.para("")); // 완전 빈 화면

            // 2) 결과 키 (새 메모는 임시 키로 충분)
            String resultKey = "memo_new_result_" + System.currentTimeMillis();

            // 3) 결과 수신 리스너
            getSupportFragmentManager().setFragmentResultListener(
                    resultKey,
                    MemoBase.this, // ← 액티비티(=LifecycleOwner)
                    (reqKey, bundle) -> {
                        @SuppressWarnings("unchecked")
                        ArrayList<BlockMemo> blocks = (ArrayList<BlockMemo>) bundle.getSerializable("blocks");
                        if (blocks == null ) return; // 완전 빈 내용은 저장 안 함

                        io.execute(() -> {
                            repo.addBlocks(blocks); // ← 블록 그대로 저장 (BLOCKS_JSON:... 형태)
                            List<Memo> fresh = showingTrash ? repo.softDeletedMemo() : repo.activeMemo();
                            submitSafely(fresh);
                        });
                    }
            );

            // 4) 다이얼로그 띄우기
            MemoBlockDialog.newInstance(initial, resultKey)
                    .show(getSupportFragmentManager(), "memo_new");
        });




        ///////menu
        //Log.d("ccccc", "cccccc");
        // 일반 메뉴로 이동
        findViewById(R.id.ChangeDomain).setOnClickListener(v -> {
            startActivity(new Intent(this, TodoAct.class));
        });

        findViewById(R.id.BackToMain).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
        });
        
        //휴지통 관련

        findViewById(R.id.ChangeLayer).setOnClickListener(v -> {
            showingTrash = !showingTrash;
            adapter.setTrashMode(showingTrash);

            io.execute(() -> {
                List<Memo> fresh = showingTrash
                        ? repo.softDeletedMemo()
                        : repo.activeMemo();

                runOnUiThread(() -> {
                    // 🔴 휴지통 아이콘 색 전환
                    ImageView icon = (ImageView) v;
                    int color = showingTrash
                            ? ContextCompat.getColor(this, android.R.color.holo_red_dark)
                            : ContextCompat.getColor(this, android.R.color.black);
                    icon.setColorFilter(color);
                    swipeHelperMode.attachToRecyclerView(null); // 기존 detach
                    submitSafely(fresh);
                    swipeHelperMode = setTouchHelper(showingTrash ? 2 : 1); // 새 helper 생성
                    swipeHelperMode.attachToRecyclerView(rv);
                });
            });
        });


        // 저장된 목록 불러오기
        //adapter.setItems(repo.load());

    }

    // 드래그 시작 전에 현재 리스트를 복사
    private void ensureDragTemp() {
        if (dragTemp == null) dragTemp = new ArrayList<>(adapter.getCurrentList());
    }

    private ItemTouchHelper setTouchHelper(int type){

        if(type == 1){
            //active
            return new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                    ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                    ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT){

                @Override public boolean isLongPressDragEnabled() { return false; }
                @Override public boolean isItemViewSwipeEnabled() { return false; }

                @Override public boolean onMove(@NonNull RecyclerView rv,
                                                @NonNull RecyclerView.ViewHolder vh,
                                                @NonNull RecyclerView.ViewHolder t) {

                    Log.d("drag","test");

                    ensureDragTemp();
                    int fromPos = vh.getBindingAdapterPosition();
                    int toPos = t.getBindingAdapterPosition();



                    Collections.swap(dragTemp, fromPos, toPos);

                    // 화면 애니메이션만 갱신 (ListAdapter에도 호출 OK)
                    rv.getAdapter().notifyItemMoved(fromPos, toPos);
                    return true;
                }

                @Override public void clearView(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh) {
                    super.clearView(rv, vh);
                    if (dragTemp == null) return;

                    // 최종 순서를 DiffUtil로 반영 (이때만 계산)
                    ArrayList<Memo> finalOrder = new ArrayList<>(dragTemp);
                    dragTemp = null;
                    adapter.submitList(finalOrder);   // UI와 내부 currentList 동기화

                    // 영구 저장
                    io.execute(() -> repo.reorder(ids(finalOrder)));
                }

                @Override public int getMovementFlags(@NonNull RecyclerView rv,
                                                      @NonNull RecyclerView.ViewHolder vh) {
                    int pos = vh.getBindingAdapterPosition();
                    if (pos == RecyclerView.NO_POSITION) return 0;

                    Memo item = adapter.itemAt(pos);
                   /* boolean canSwipe = (item != null && item.isDone());
                    return makeMovementFlags(0, canSwipe
                            ? (ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT)
                            : 0);*/
                    final int drag = (type == 1) ? (ItemTouchHelper.UP | ItemTouchHelper.DOWN) : 0;
                    final int swipe = (type == 1) ? (ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) : 0;
                    return makeMovementFlags(drag, swipe);
                   }
               /* @Override public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                    int pos = vh.getBindingAdapterPosition();
                    if (pos == RecyclerView.NO_POSITION) return;
                    Memo m = adapter.getCurrentList().get(pos);

                    io.execute(() -> {
                        repo.softDelete(m.getId());          // 소프트 삭제
                        submitSafely(repo.activeMemo());     // UI 갱신
                    });
                }*/

                @Override public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                    final int pos = vh.getBindingAdapterPosition();
                    if (pos == RecyclerView.NO_POSITION) return;

                    // 스와이프된 아이템의 안전한 식별값만 캡쳐(포지션은 변할 수 있음)
                    final Memo swiped = adapter.getCurrentList().get(pos);
                    final String id = adapter.getCurrentList().get(pos).getId();
                    // 모드별 메시지/액션
                    final boolean inTrash = showingTrash;
                    final String title   = inTrash ? "영구 삭제" : "삭제";
                    final String message = inTrash ? "이 메모를 영구 삭제할까요? 복구할 수 없습니다."
                            : "이 메모를 휴지통으로 보낼까요?";

                    showDeleteConfirmDialog(MemoBase.this, id, pos, showingTrash);
                }

            });
        }else {
            //Trash
            return new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                    ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

                @Override public boolean onMove(@NonNull RecyclerView rv,
                                                @NonNull RecyclerView.ViewHolder vh,
                                                @NonNull RecyclerView.ViewHolder t) {
                    return false;
                }

                @Override public int getMovementFlags(@NonNull RecyclerView rv,
                                                      @NonNull RecyclerView.ViewHolder vh) {
                    // 휴지통에서는 모든 항목 스와이프 가능
                    return makeMovementFlags(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
                }

                @Override public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                    final int pos = vh.getBindingAdapterPosition();
                    if (pos == RecyclerView.NO_POSITION) return;
                    final String id = adapter.getCurrentList().get(pos).getId();

                    showDeleteConfirmDialog(MemoBase.this, id, pos, showingTrash);
                }
            });
        }

    }

    private void showDeleteConfirmDialog(Context ctx, String id, int adapterPos, boolean inTrash) {
        final String title   = inTrash ? "영구 삭제" : "삭제";
        final String message = inTrash
                ? "이 메모를 영구 삭제할까요? 복구할 수 없습니다."
                : "이 메모를 휴지통으로 보낼까요?";

        new AlertDialog.Builder(ctx)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(inTrash ? "삭제" : "휴지통", (dialog, which) -> {
                    io.execute(() -> {
                        if (inTrash) {
                            repo.hardDelete(id);
                            Log.d("delete " , " test " ) ;
                            submitSafely(repo.softDeletedMemo());
                        } else {
                            repo.softDelete(id);
                            submitSafely(repo.activeMemo());
                        }
                    });
                })
                .setNegativeButton("취소", (dialog, which) -> {
                    // 스와이프 복원
                    adapter.notifyItemChanged(adapterPos);
                })
                .setOnCancelListener(d -> {
                    // 바깥 터치/백키로 닫힌 경우도 복원
                    adapter.notifyItemChanged(adapterPos);
                })
                .show();
    }

    // 어댑터의 현재 리스트에서 id로 포지션 찾기 (취소 시 원복용)
    private int findAdapterPosById(String id) {
        List<Memo> list = adapter.getCurrentList();
        for (int i = 0; i < list.size(); i++) {
            if (id.equals(list.get(i).getId())) return i;
        }
        return RecyclerView.NO_POSITION;
    }

    private void showRestoreConfirmDialog(Context ctx, String id) {
        new AlertDialog.Builder(ctx)
                .setTitle("복구")
                .setMessage("이 메모를 복구하시겠습니까?")
                .setPositiveButton("복구", (dialog, which) -> {
                    io.execute(() -> {
                        repo.restore(id);                    // deletedAt=null
                        submitSafely(repo.softDeletedMemo()); // 휴지통 목록 갱신 유지
                    });
                })
                .setNegativeButton("취소", (dialog, which) -> {
                    int p = findAdapterPosById(id);
                    if (p != RecyclerView.NO_POSITION) adapter.notifyItemChanged(p); // 체크 원복
                })
                .setOnCancelListener(d -> {
                    int p = findAdapterPosById(id);
                    if (p != RecyclerView.NO_POSITION) adapter.notifyItemChanged(p); // 바깥터치/백키 시 원복
                })
                .show();
    }
    ///////0ld
    // 스와이프로 삭제
  /*  ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, 0) {

        @Override public boolean onMove(RecyclerView rv, RecyclerView.ViewHolder vh, RecyclerView.ViewHolder t) {
            return false;
        }

        // 항목별로 스와이프 가능/불가능을 결정
        public int getMovementFlags(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh) {
            int pos = vh.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return 0;

            // ✅ ListAdapter에는 getItem(pos)가 이미 내장되어 있음
            Memo item = adapter.itemAt(pos);
            boolean canSwipe = (item != null && item.done);

            int swipeFlags = canSwipe ? (ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) : 0;
            return makeMovementFlags(0, swipeFlags);
        }

        @Override public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
            //adapter.remove(vh.getBindingAdapterPosition());
            Memo m = adapter.getCurrentList().get(vh.getBindingAdapterPosition());
            io.execute(() -> {
                repo.softDelete(m.getId());
                List<Memo> fresh = repo.activeMemo();
                runOnUiThread(() -> submitSafely(fresh));
            });
        }

        // (선택) 스와이프 임계값을 좀 더 빡세게
        // @Override public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) { return 0.5f; }
    });*/

    private List<String> ids(List<Memo> list) {
        ArrayList<String> out = new ArrayList<>(list.size());
        for (Memo m : list) out.add(m.getId());
        return out;
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

    private void submitSafely(List<Memo> list) {
        if (pendingSubmit != null) uiHandler.removeCallbacks(pendingSubmit);
        pendingSubmit = () -> adapter.submitList(new ArrayList<>(list));
        uiHandler.postDelayed(pendingSubmit, 200); // 100ms 이내 중복 호출 무시
    }

}
