package com.ex.realcv.MemoMain;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;


import com.ex.realcv.Func.ResultCall;
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

import javax.xml.transform.Result;

public class MemoBase extends MainActivity {


    private MemoAdapter adapter;
    private RecyclerView rv;
    private BlocksAdapter BlockAdapter;

    private RepositoryFunc repo;
    private RoomMemoRepository RoomRepo;
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


    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memo_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        rv = findViewById(R.id.rvMemo);
        LinearLayoutManager lm = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        lm.setReverseLayout(false);
        lm.setStackFromEnd(false);
        rv.setLayoutManager(lm);

        //저장소
        //repo = new FileMemoRepository(getApplicationContext());
        repo = new RoomMemoRepository((getApplicationContext()));

        //스와이프 삭제 초기 설정
        swipeHelperMode = setTouchHelper(1);
        swipeHelperMode.attachToRecyclerView(rv);


        adapter = new MemoAdapter(new MemoAdapter.Listener() {
            @Override public void onToggleDone(Memo m, boolean checked) {
                if (showingTrash) {
                    showRestoreConfirmDialog(MemoBase.this, m.getId());
                    return;
                } else {
                    repo.toggleDone(m.getId(), checked); // repo 내부에서 diskIO
                }

                // ✅ 조회를 콜백으로 받기
                if (showingTrash) {
                    repo.softDeletedMemo(result -> runOnUiThread(() -> {
                        if (result instanceof ResultCall.Success) {
                            @SuppressWarnings("unchecked")
                            List<Memo> fresh = ((ResultCall.Success<List<Memo>>) result).data;
                            submitSafely(fresh);
                        } else if (result instanceof ResultCall.Error) {
                            Throwable e = ResultCall.getError(result);
                            Log.e("MemoBase Error", "softDeleteMemo error : ", e);
                            // 필요하면 토스트/스낵바 등
                        }
                    }));
                } else {
                    repo.activeMemo(result -> runOnUiThread(() -> {
                        if (result instanceof ResultCall.Success) {
                            @SuppressWarnings("unchecked")
                            List<Memo> fresh = ((ResultCall.Success<List<Memo>>) result).data;
                            submitSafely(fresh);
                        } else if (result instanceof ResultCall.Error) {
                            Throwable e = ResultCall.getError(result);
                            Log.e("MemoBase Error", "activeMemo error : ", e);

                        }
                    }));
                }
            }
            @Override public void onItemClick(Memo m) {

                // 1) 메모별 고유 결과키
                String resultKey = "memo_result_" + m.id;

                // 2) 결과 리스너 등록 (중복 등록 방지용으로 먼저 clear 가능하면 더 좋음)
                getSupportFragmentManager().setFragmentResultListener(
                        resultKey,
                        MemoBase.this,
                        (reqKey, bundle) -> {

                            @SuppressWarnings("unchecked")
                            ArrayList<BlockMemo> blocks =
                                    (ArrayList<BlockMemo>) bundle.getSerializable("blocks");
                            if (blocks == null) return;

                            // 3) 저장은 repo 내부 스레드에서 처리되게
                            /*repo.updateBlocks(m.id, blocks, unused -> {
                                // 4) 저장 끝나면 리스트 갱신
                                refreshList();
                            });*/
                            repo.updateBlocks(m.id, blocks, result -> runOnUiThread(() -> {
                                if (result instanceof ResultCall.SuccessCall) {
                                    refreshList();
                                }
                            }));
                        }
                );

                repo.loadBlocks(m.id, result -> {
                    if (result instanceof ResultCall.Success) {
                        @SuppressWarnings("unchecked")
                        ArrayList<BlockMemo> initial =
                                ((ResultCall.Success<ArrayList<BlockMemo>>) result).data;

                        runOnUiThread(() -> {
                            MemoBlockDialog
                                    .newInstance(initial, resultKey)
                                    .show(getSupportFragmentManager(), "memo_edit");
                        });

                    } /*else if (result instanceof ResultCall.Error) {
                        Throwable e = ((ResultCall.Error<?>) result).error;
                        Log.e("loadBlocks", "fail", e);

                        // 실패 시에도 빈 화면으로 열고 싶다면(선택):
                        runOnUiThread(() -> {
                            ArrayList<BlockMemo> fallback = new ArrayList<>();
                            fallback.add(BlockMemo.para(""));
                            MemoBlockDialog
                                    .newInstance(fallback, resultKey)
                                    .show(getSupportFragmentManager(), "memo_edit");
                        });
                    }*/
                });



            }


            @Override public void onStartDrag(RecyclerView.ViewHolder holder) {
                swipeHelperMode.startDrag(holder);
            }
        });

        rv.setAdapter(adapter);
        adapter.setTrashMode(false);
        //adapter.setItems(repo.activeMemo());
        //adapter.submitList(repo.activeMemo());
        refreshList();



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
                    MemoBase.this,
                    (reqKey, bundle) -> {
                        @SuppressWarnings("unchecked")
                        ArrayList<BlockMemo> blocks =
                                (ArrayList<BlockMemo>) bundle.getSerializable("blocks");
                        if (blocks == null) return;

                        // ✅ (선택) "진짜 빈 내용"이면 저장 안 함
                        if (isBlocksEmpty(blocks)) return;

                        // ✅ 저장은 repo가 비동기로 처리
                        repo.addBlocks(blocks, result -> {

                            if(result instanceof ResultCall.SuccessCall)
                            {
                                // 저장 완료 후 리스트 갱신
                                refreshList();
                            }else if (result instanceof ResultCall.Error) {
                                Throwable e = ResultCall.getError(result);
                                Log.e("MemoBase Error", "addBlock error : ", e);
                            }
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

            // ✅ 1) UI는 즉시 반영 (메인스레드)
            ImageView icon = (ImageView) v;
            int color = showingTrash
                    ? ContextCompat.getColor(this, android.R.color.holo_red_dark)
                    : ContextCompat.getColor(this, android.R.color.black);
            icon.setColorFilter(color);

            // ✅ 2) 스와이프 헬퍼 교체도 즉시
            if (swipeHelperMode != null) {
                swipeHelperMode.attachToRecyclerView(null); // detach
            }
            swipeHelperMode = setTouchHelper(showingTrash ? 2 : 1);
            swipeHelperMode.attachToRecyclerView(rv);

            // ✅ 3) 데이터만 repo 콜백으로 갱신
            if (showingTrash) {
                repo.softDeletedMemo(result ->
                        runOnUiThread(() -> submitResult(result, "softDeletedMemo")));
            } else {
                repo.activeMemo(result ->
                        runOnUiThread(() -> submitResult(result, "activeMemo")));
            }
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
                @Override public boolean isItemViewSwipeEnabled() { return true; }

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

                    ArrayList<Memo> finalOrder = new ArrayList<>(dragTemp);
                    dragTemp = null;

                    adapter.submitList(finalOrder);

                    //repo.reorder(ids(finalOrder));
                }

                @Override public int getMovementFlags(@NonNull RecyclerView rv,
                                                      @NonNull RecyclerView.ViewHolder vh) {
                    int pos = vh.getBindingAdapterPosition();
                    if (pos == RecyclerView.NO_POSITION) return 0;

                    Memo item = adapter.itemAt(pos);

                    int dragFlags = 0;
                    int swipeFlags = 0;

                    if (type == 1) { // 일반 모드
                        dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                        swipeFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                    } else if (type == 2) { // 휴지통 모드
                        dragFlags = 0;
                        swipeFlags = ItemTouchHelper.LEFT; // 예: 왼쪽만
                    }
                    return makeMovementFlags(dragFlags, swipeFlags);
                   }

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

                    if (inTrash) {
                        // ✅ 영구 삭제
                        repo.hardDelete(id, result -> {
                            if(result instanceof ResultCall.SuccessCall){
                                refreshList();
                            }else if (result instanceof ResultCall.Error) {
                                Throwable e = ResultCall.getError(result);
                                Log.d("MemoBase", "hardDelete Error occur", e);

                            }
                        });
                    } else {
                        // ✅ 소프트 삭제(휴지통 이동)
                        repo.softDelete(id, unused -> refreshList());

                        repo.softDelete(id, result -> {
                            if(result instanceof ResultCall.SuccessCall){
                                refreshList();
                            }else if (result instanceof ResultCall.Error) {
                                Throwable e = ResultCall.getError(result);
                                Log.d("MemoBase", "softDelete Error occur", e);
                            }
                        });

                    }
                })
                .setNegativeButton("취소", (dialog, which) -> {
                    adapter.notifyItemChanged(adapterPos);
                })
                .setOnCancelListener(d -> {
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
                    // ✅ repo가 비동기로 처리, 완료 후 휴지통 목록 갱신
                    repo.restore(id, result -> {
                        if (result instanceof ResultCall.SuccessCall) {
                            refreshList();
                        } else if (result instanceof ResultCall.Error) {
                            Throwable e = ResultCall.getError(result);
                            Log.d("MemoBase", "restore Error occur", e);
                        }
                    });
                })
                .setNegativeButton("취소", (dialog, which) -> {
                    int p = findAdapterPosById(id);
                    if (p != RecyclerView.NO_POSITION) adapter.notifyItemChanged(p);
                })
                .setOnCancelListener(d -> {
                    int p = findAdapterPosById(id);
                    if (p != RecyclerView.NO_POSITION) adapter.notifyItemChanged(p);
                })
                .show();
    }

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

    private void submitSafely(List<Memo> list) {
        if (isFinishing() || isDestroyed()) return;
        if (pendingSubmit != null) uiHandler.removeCallbacks(pendingSubmit);
        pendingSubmit = () -> adapter.submitList(new ArrayList<>(list));
        uiHandler.post(pendingSubmit); // 100ms 이내 중복 호출 무시
    }

    //HELPER
    private void refreshList() {
        if (showingTrash) {
            repo.softDeletedMemo(result -> {
                if (isFinishing() || isDestroyed()) return;
                runOnUiThread(() -> submitResult(result, "softDeletedMemo"));
            });
        } else {
            repo.activeMemo(result ->{
                if (isFinishing() || isDestroyed()) return;
                runOnUiThread(() -> submitResult(result, "activeMemo"));
                }
            );
        }
    }

    private void refreshTrashList() {
        Log.d("trash", "refreshTrashList()");
        repo.softDeletedMemo(result ->
                runOnUiThread(() -> submitResult(result, "softDeletedMemo")));
    }

    private boolean isBlocksEmpty(ArrayList<BlockMemo> blocks) {
        if (blocks == null || blocks.isEmpty()) return true;

        for (BlockMemo b : blocks) {
            if (b == null) continue;
            if (b.text != null && !b.text.trim().isEmpty()) return false;
        }
        return true;
    }

    private void submitResult(ResultCall<List<Memo>> result, String tag) {
        if (result instanceof ResultCall.Success) {
            @SuppressWarnings("unchecked")
            List<Memo> fresh =
                    ((ResultCall.Success<List<Memo>>) result).data;
            submitSafely(fresh);

        } else if (result instanceof ResultCall.Error) {
            Throwable e = ResultCall.getError(result);
            Log.e(tag, "load failed", e);
            // 필요 시 Toast / Snackbar
        }
    }

    @Override
    protected void onDestroy() {

        if (pendingSubmit != null) uiHandler.removeCallbacks(pendingSubmit);
        pendingSubmit = null;

        rv.setAdapter(null);                  // (선택) adapter가 Activity 캡처할 때 도움
        swipeHelperMode.attachToRecyclerView(null); // (선택) 터치헬퍼 해제

        super.onDestroy();
    }

}
