package com.ex.realcv.MemoMain;

import android.content.Context;

import com.ex.realcv.DB.AppDatabase;
import com.ex.realcv.DB.MemoDAO;
import com.ex.realcv.DB.MemoEntity;
import com.ex.realcv.DB.MemoMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RoomMemoRepository  implements RepositoryFunc {

    private final MemoDAO dao;
    private final ExecutorService diskIO = Executors.newSingleThreadExecutor();

    public RoomMemoRepository(Context context){
        AppDatabase db = AppDatabase.getInstance(context);
        this.dao = db.memoDao();
    }

    @Override
    public List<Memo> load() {
        return null;
    }

    @Override
    public Memo add(String text) {
        return null;
    }

    @Override
    public void delete(String id) {

    }

        // -------- 매핑 유틸 --------
    private List<Memo> mapToDomainList(List<MemoEntity> entities) {
        ArrayList<Memo> out = new ArrayList<>();
        for (MemoEntity e : entities) out.add(MemoMapper.fromEntity(e));
        return out;
    }

    private static final String BLOCKS_PREFIX = "BLOCKS_JSON:";
    private final com.google.gson.Gson gson = new com.google.gson.Gson();

    private String encodeBlocks(List<BlockMemo> blocks) {
        return BLOCKS_PREFIX + gson.toJson(blocks);
    }

    // ------------- 조회 -------------
    @Override
    public void activeMemo(Callback<List<Memo>> cb){
        diskIO.execute(() ->{
            List<MemoEntity> entities = dao.getActiveMemos();
            cb.onResult(mapToDomainList(entities));
        });
    }

    public void softDeletedMemo(Callback<List<Memo>> cb){
        diskIO.execute(() -> {
            List<MemoEntity> entities = dao.getSoftDeletedMemos(); // 아래 DAO에 추가할 거
            cb.onResult(mapToDomainList(entities));
        });
    }

    // ---------- 소프트 삭제/복원 ----------
    public void softDelete(String id, Callback<Void> cb){
        long ts = System.currentTimeMillis();
        diskIO.execute(() -> {
            dao.softDelete(id,ts);
        });
    }

    public void hardDelete(String id, Callback<Void> cb){
        diskIO.execute(() -> {
            dao.deleteById(id);
        });
    }

    public void restore(String id , Callback<Void> cb){
        long ts = System.currentTimeMillis();
        diskIO.execute(() -> {
            dao.restoreMemo(id,ts);
        });
    }

    // ---------- 추가/수정----------
   /* public Memo add(String text) {
        if (text == null || text.trim().isEmpty()) return null;

        long now = System.currentTimeMillis();
        Memo m = new Memo(UUID.randomUUID().toString(), text.trim(), false);
        m.createdAt = now;
        m.updatedAt = now;
        m.deletedAt = null;

        MemoEntity e = MemoMapper.toEntity(m);
        diskIO.execute(() -> dao.upsert(e));

        return m; // UI 즉시 반영용 (DB 반영은 백그라운드)
    }*/

    public void updateText(String id, String newText) {
        if (newText == null) return;
        String t = newText.trim();
        if (t.isEmpty()) return;

        long now = System.currentTimeMillis();
        diskIO.execute(() -> dao.updateText(id, t, now)); // 아래 DAO에 추가할 거
    }

    public void toggleDone(String id, boolean done) {
        long now = System.currentTimeMillis();
        diskIO.execute(() -> dao.updateDone(id, done, now)); // 아래 DAO에 추가할 거
    }

    @Override
    public void addBlocks(ArrayList<BlockMemo> blocks, Callback<Void> cb) {
        if (blocks == null) {
            if (cb != null) cb.onResult(null);
            return;
        }

        // ✅ blocks → "BLOCKS_JSON:...." 문자열로 인코딩
        final String payload = encodeBlocks(blocks); // 기존 유틸 재사용

        diskIO.execute(() -> {
            long now = System.currentTimeMillis();

            MemoEntity e = new MemoEntity();
            e.id = java.util.UUID.randomUUID().toString();
            e.text = payload;
            e.done = false;
            e.deletedAt = null;
            e.createdAt = now;
            e.updatedAt = now;

            // 정렬 컬럼을 쓰고 있다면(드래그 reorder용)
            // "새 메모가 위" 정렬이면 DESC로 조회할 거라 sortOrder=now 추천
            //e.sortOrder = now; // sort_order 컬럼을 도입했다면

            dao.upsert(e);

            if (cb != null) cb.onResult(null); // ⚠️ 콜백은 백그라운드에서 호출됨
        });
    }

    @Override
    public void loadBlocks(String id, Callback<ArrayList<BlockMemo>> cb) {

    }

    @Override
    public void updateBlocks(String id, ArrayList<BlockMemo> blocks, Callback<Void> cb) {

    }
}
