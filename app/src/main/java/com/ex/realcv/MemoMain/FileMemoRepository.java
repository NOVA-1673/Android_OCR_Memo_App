package com.ex.realcv.MemoMain;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class FileMemoRepository implements RepositoryFunc{




    private static final String TAG = "FileMemoRepository";
    private static final String FILE_NAME = "memos.json";

    private static final String TAGS = "FileMemoRepo";

    private final File file;

    /*private static final String PREFS = "memo";
    private static final String KEY = "memos_json";
    private final SharedPreferences prefs;*/
    private final Gson gson = new Gson();
    private final Type listType = new TypeToken<ArrayList<Memo>>(){}.getType();

    private final List<Memo> cache = new ArrayList<>();

    private final ExecutorService diskIO = Executors.newSingleThreadExecutor();

    private static class MemoEntry{
        final int index;
        final Memo memo;
        MemoEntry(int index, Memo memo){
            this.index = index;
            this.memo = memo;
        }
    }


    public FileMemoRepository(Context ctx) {
       //prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        file = new File(ctx.getFilesDir(), FILE_NAME);
        this.cache.addAll(load());
    }

    // ---------- 조회 ----------
    public synchronized List<Memo> activeMemo() {
        List<Memo> list = new ArrayList<>();
        for (Memo n : cache) if (n.getDeletedAt() == null) list.add(n);
        return list;
    }

    public synchronized List<Memo> softDeletedMemo() {
        List<Memo> list = new ArrayList<>();
        for (Memo n : cache) if (n.getDeletedAt() != null) list.add(n);
        return list;
    }

    // ---------- 소프트 삭제/복원 ----------
    public synchronized void softDelete(String id) {
        Memo n = find(id);
        if (n != null && n.getDeletedAt() == null) {
            n.setDeletedAt(System.currentTimeMillis());
            n.setDone(false);
            diskIO.execute(this::persist);
        }
    }
    // ---------- 하드 삭제 ----------
    public synchronized void hardDelete(String id) {
        for (Iterator<Memo> it = cache.iterator(); it.hasNext(); ) {
            if (it.next().getId().equals(id)) { it.remove(); break; }
        }
        diskIO.execute(this::persist);
    }

    public synchronized void emptyTrash() {
        cache.removeIf(n -> n.getDeletedAt() != null);
        persist();
    }
    // ------------------------------

    public synchronized void restore(String id) {
        Memo n = find(id);
        if (n != null && n.getDeletedAt() != null) {
            n.setDeletedAt(null);
            diskIO.execute(this::persist);
        }
    }

    public synchronized List<Memo> trashNotes() {
        List<Memo> list = new ArrayList<>();
        for (Memo n : cache) if (n.getDeletedAt() != null) list.add(n);
        return list;
    }


    // ---------- 내부 유틸 ----------
    private Memo find(String id) {
        for (Memo n : cache) if (n.getId().equals(id)) return n;
        return null;
    }
    private MemoEntry findWithIndex(String id) {
        for (int i = 0; i < cache.size(); i++) {
            Memo m = cache.get(i);
            if (m.getId().equals(id)) {
                return new MemoEntry(i, m);
            }
        }
        return null;
    }

    @Override
    public synchronized List<Memo> load() {
        if (!file.exists()) return new ArrayList<>();
        Log.d("repo test", "test here2");
        long t0 = android.os.SystemClock.uptimeMillis();
        boolean isMain = android.os.Looper.getMainLooper().isCurrentThread();


        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            Log.d(TAG, "LOAD start | thread=" + Thread.currentThread().getName() + " | main=" + isMain);

            StringBuilder sb = new StringBuilder();
            char[] buf = new char[4096];
            int n;
            while ((n = br.read(buf)) != -1) sb.append(buf, 0, n);
            String json = sb.toString().trim();

            Log.d(TAG, "LOAD end   | dur=" + (android.os.SystemClock.uptimeMillis()-t0) + "ms"
                    + " | bytes=" + (file.exists()? file.length(): 0));


            return json.isEmpty() ? new ArrayList<>() : gson.fromJson(json, listType);


        } catch (IOException e) {
            Log.e(TAG, "load failed", e);
            return new ArrayList<>();
        }
    }

    public synchronized  void persist() {

        File dir = file.getParentFile();
        File tmp = new File(dir, file.getName()+".tmp");

        try (FileOutputStream fos = new FileOutputStream(tmp);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter bw = new BufferedWriter(osw)) {

            gson.toJson(cache, bw);
            bw.flush();

            boolean moved = false;

            if(!moved){
                if(!tmp.renameTo(file)){
                    bw.close();
                    throw new IOException("rename failed");
                }
            }

           /* Log.d(TAG, "SAVE end   | dur=" + (android.os.SystemClock.uptimeMillis()-t0) + "ms"
                    + " | path=" + file.getAbsolutePath()
                    + " | bytes=" + file.length());*/

        } catch (IOException e) {
            tmp.delete();
            Log.e(TAG, "save failed", e);
        }

    }

    /*private void persist() {
        try {
            String json = gson.toJson(cache);
            Files.write(file.toPath(), json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    @Override public synchronized Memo add(String text) {
        if (text == null || text.trim().isEmpty()) return null;

        Memo m = new Memo(UUID.randomUUID().toString(), text.trim(), false);
        m.createdAt = System.currentTimeMillis();
        m.updatedAt = m.createdAt;

        // ✅ cache 리스트에 바로 추가 (load() 불필요)
        cache.add(0, m);

        // ✅ 디스크 저장은 백그라운드에서
        diskIO.execute(this::persist);

        return m;
    }

    public synchronized void updateText(String id, String newText){

        if (newText == null || newText.isEmpty()) return;
        newText = newText.trim();

        int idx = -1;
        Memo old = null;
        for (int i = 0; i < cache.size(); i++) {
            if (cache.get(i).id.equals(id)) { idx = i; old = cache.get(i); break; }
        }
        if (old == null) return;
        if (Objects.equals(old.text, newText)) return;

        // 불변처럼: 새 객체로 교체
        Memo neo = new Memo(old.id, newText, old.done);
        neo.createdAt = old.createdAt;
        neo.updatedAt = System.currentTimeMillis();
        neo.deletedAt = old.deletedAt;

        cache.set(idx, neo);   // ← 인플레이스 수정 대신 교체
        diskIO.execute(this::persist);
    }

    public synchronized void toggleDone(String id, boolean done) {
        /*Memo n = find(id);
        if (n != null) { n.setDone(done); persist(); }*/
        MemoEntry entry = findWithIndex(id);
        if (entry == null) return;
        cache.set(entry.index, new Memo(entry.memo.getId(), entry.memo.text, done));
    }

    @Override
    public void addBlocks(ArrayList<BlockMemo> blocks, Callback<Void> cb) {

    }

    @Override
    public void loadBlocks(String id, Callback<ArrayList<BlockMemo>> cb) {

    }

    @Override
    public void updateBlocks(String id, ArrayList<BlockMemo> blocks, Callback<Void> cb) {

    }

    @Override public void delete(String id) {
        List<Memo> list = load();
        list.removeIf(x -> x.id.equals(id));
        diskIO.execute(this::persist);
    }

    @Override
    public void activeMemo(Callback<List<Memo>> cb) {

    }

    @Override
    public void softDeletedMemo(Callback<List<Memo>> cb) {

    }

    @Override
    public void softDelete(String id, Callback<Void> cb) {

    }

    @Override
    public void hardDelete(String id, Callback<Void> cb) {

    }

    // ====== [A] 문자열에 블록 JSON을 래핑/언래핑하기 위한 포맷 ======
    private static final String BLOCKS_PREFIX = "BLOCKS_JSON:"; // 문자열 시작에 붙는 시그니처

    public synchronized Memo addBlocks(ArrayList<BlockMemo> blocks) {
        if (blocks == null) return null;
        String payload = encodeBlocks(blocks); // "BLOCKS_JSON:..."
        return add(payload);                   // 기존 add(String text) 재사용 (persist는 내부에서)
    }
    private String encodeBlocks(List<BlockMemo> blocks) {
        return BLOCKS_PREFIX + gson.toJson(blocks);
    }

    private boolean isBlocksPayload(String text) {
        return text != null && text.startsWith(BLOCKS_PREFIX);
    }

    private ArrayList<BlockMemo> decodeBlocks(String payload) {
        try {
            String json = payload.substring(BLOCKS_PREFIX.length());
            java.lang.reflect.Type t = new com.google.gson.reflect.TypeToken<ArrayList<BlockMemo>>(){}.getType();
            ArrayList<BlockMemo> list = gson.fromJson(json, t);
            return (list != null) ? list : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    // ====== [B] 평문을 블록 리스트로 변환(초기 마이그레이션/백워드 호환용) ======
    private ArrayList<BlockMemo> plainToBlocks(String text) {
        ArrayList<BlockMemo> out = new ArrayList<>();
        if (text == null || text.isEmpty()) { out.add(BlockMemo.para("")); return out; }

        // 마크다운풍 체크박스 접두 해석: "- [ ] " / "- [x] "
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

    // 필요시 블록을 다시 평문으로 내보내는 유틸 (기존 화면/검색 등에서 활용)
    private String blocksToPlain(List<BlockMemo> blocks) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < blocks.size(); i++) {
            BlockMemo b = blocks.get(i);
            if (b.type == BlockMemo.Type.TODO) {
                sb.append(b.checked ? "- [x] " : "- [ ] ").append(b.text);
            } else {
                sb.append(b.text);
            }
            if (i < blocks.size()-1) sb.append('\n');
        }
        return sb.toString();
    }

    public synchronized void updateBlocks(String id, ArrayList<BlockMemo> blocks) {
        if (blocks == null) return;
        String payload = encodeBlocks(blocks); // "BLOCKS_JSON:...." 형태로 래핑
        updateText(id, payload);               // 기존 updateText 재사용 → persist 백그라운드 실행
    }

    public synchronized ArrayList<BlockMemo> loadBlocks(String id) {
        Memo n = find(id);
        if (n == null) return new ArrayList<>();

        String t = n.text;
        if (isBlocksPayload(t)) return decodeBlocks(t); // 이미 블록 저장된 메모
        return plainToBlocks(t);                         // 과거 평문 메모도 바로 변환
    }

    public synchronized void reorder(List<String> idsInOrder) {
        if (idsInOrder == null || idsInOrder.isEmpty()) return;

        // id → Memo 매핑
        HashMap<String, Memo> map = new HashMap<>();
        for (Memo m : cache) map.put(m.getId(), m);

        // 새 순서로 재구성 (없는 id는 무시)
        ArrayList<Memo> reordered = new ArrayList<>(cache.size());
        for (String id : idsInOrder) {
            Memo m = map.remove(id);
            if (m != null) reordered.add(m);
        }
        // 혹시 누락된 항목(휴지통/필터 등)이 있으면 뒤에 붙이기
        if (!map.isEmpty()) reordered.addAll(map.values());

        cache.clear();
        cache.addAll(reordered);
        diskIO.execute(this::persist);
    }
}
