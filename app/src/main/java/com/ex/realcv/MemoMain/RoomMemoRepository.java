package com.ex.realcv.MemoMain;

import android.content.Context;

import com.ex.realcv.DB.AppDatabase;
import com.ex.realcv.DB.MemoDAO;
import com.ex.realcv.DB.MemoEntity;
import com.ex.realcv.DB.MemoMapper;
import com.ex.realcv.Func.ResultCall;
import com.google.gson.Gson;

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



    // ------------- 조회 -------------
    @Override
    public void activeMemo(Callback<List<Memo>> cb){
        diskIO.execute(() ->{

            try{
                List<MemoEntity> entities = dao.getActiveMemos();
                List<Memo> memos = mapToDomainList(entities);
                cb.onResult(new ResultCall.Success<>(memos));
            }catch (Exception e) {
                cb.onResult(new ResultCall.Error<>(e));
            }

        });
    }

    public void softDeletedMemo(Callback<List<Memo>> cb){
        if( cb == null ) return;
        diskIO.execute(() -> {

            try{
                List<MemoEntity> entities = dao.getSoftDeletedMemos();
                List<Memo> memos = mapToDomainList(entities);
                cb.onResult(new ResultCall.Success<>(memos));
            }catch (Exception e) {
                cb.onResult(new ResultCall.Error<>(e));
            }
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
            try{
                dao.restoreMemo(id,ts);
                cb.onResult(ResultCall.SuccessCall.INSTANCE);
            }catch (Exception e) {
                cb.onResult(new ResultCall.Error<>(e));
            }
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

        try{
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

                cb.onResult(ResultCall.SuccessCall.INSTANCE);
            });
        } catch(Exception e){
            cb.onResult(new ResultCall.Error<>(e));
        }

    }

    @Override
    public void loadBlocks(String id, Callback<ArrayList<BlockMemo>> cb) {

        if( cb == null) return;

        if( id == null || id.trim().isEmpty()){
            cb.onResult(new ResultCall.Success<>(new ArrayList<>()));
            return;
        }

        diskIO.execute(() -> {
            try {
                MemoEntity e = dao.getById(id);

                // ✅ 메모가 없으면 "성공 + 빈 리스트"
                if (e == null || e.text == null) {
                    cb.onResult(new ResultCall.Success<>(new ArrayList<>()));
                    return;
                }

                String t = e.text;

                ArrayList<BlockMemo> result;
                if (isBlocksPayload(t)) {
                    result = decodeBlocks(t);
                } else {
                    result = plainToBlocks(t);
                }

                cb.onResult(new ResultCall.Success<>(result));
            } catch (Exception ex) {
                cb.onResult(new ResultCall.Error<>(ex));
            }
        });

    }

    @Override
    public void updateBlocks(String id, ArrayList<BlockMemo> blocks, Callback<Void> cb) {

        if( id == null || id.trim().isEmpty()){
            cb.onResult(ResultCall.SuccessCall.INSTANCE);
            return;
        }

        if(blocks == null){
            cb.onResult(ResultCall.SuccessCall.INSTANCE);
            return;
        }

        final String payload = encodeBlocks(blocks); // BLOCKS_JSON:...

        diskIO.execute(() -> {
            long now = System.currentTimeMillis();

            // ✅ 기존 메모의 text + updated_at만 갱신
            dao.updateText(id, payload, now);

            cb.onResult(ResultCall.SuccessCall.INSTANCE); // 완료 신호
        });

    }

    //-----------UTILS
    private boolean isBlocksPayload(String text) {
        return text != null && text.startsWith(BLOCKS_PREFIX);
    }

    private String encodeBlocks(List<BlockMemo> blocks) {
        return BLOCKS_PREFIX + gson.toJson(blocks);
    }

    private ArrayList<BlockMemo> decodeBlocks(String payload) {
        try {
            String json = payload.substring(BLOCKS_PREFIX.length());
            java.lang.reflect.Type t =
                    new com.google.gson.reflect.TypeToken<ArrayList<BlockMemo>>(){}.getType();
            ArrayList<BlockMemo> list = gson.fromJson(json, t);
            return (list != null) ? list : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private ArrayList<BlockMemo> plainToBlocks(String text) {
        ArrayList<BlockMemo> out = new ArrayList<>();
        if (text == null || text.isEmpty()) { out.add(BlockMemo.para("")); return out; }

        String[] lines = text.split("\n", -1);
        for (String ln : lines) {
            if (ln.startsWith("- [ ] ")) {
                out.add(BlockMemo.todo(ln.substring(6), false));
            } else if (ln.startsWith("- [x] ") || ln.startsWith("- [X] ")) {
                out.add(BlockMemo.todo(ln.substring(6), true));
            } else {
                out.add(BlockMemo.para(ln));
            }
        }
        return out;
    }
}
