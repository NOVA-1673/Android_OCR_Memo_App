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
import java.util.List;
import java.util.UUID;

public class FileMemoRepository implements RepositoryFunc{


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


    public FileMemoRepository(Context ctx) {
       //prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        file = new File(ctx.getFilesDir(), FILE_NAME);
        Log.d("repo test", "test here");
        this.cache.addAll(load());
    }

    // ---------- 조회 ----------
    public synchronized List<Memo> activeMemo() {
        List<Memo> list = new ArrayList<>();
        for (Memo n : cache) if (n.getDeletedAt() == null) list.add(n);
        return list;
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

    @Override public synchronized  void save(List<Memo> list) {
        // 간단 버전 (원자적 쓰기까지 하려면 temp 파일로 쓰고 rename)
        /*try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            gson.toJson(list, listType, bw);
        } catch (IOException e) {
            Log.e(TAG, "save failed", e);
        }*/

        File dir = file.getParentFile();
        File tmp = new File(dir, file.getName()+".tmp");

        try (FileOutputStream fos = new FileOutputStream(tmp);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter bw = new BufferedWriter(osw)) {

           /* long t0 = android.os.SystemClock.uptimeMillis();
            boolean isMain = android.os.Looper.getMainLooper().isCurrentThread();
            Log.d(TAG, "SAVE start | thread=" + Thread.currentThread().getName()
                    + " | main=" + isMain + " | count=" + list.size());*/

            gson.toJson(list,listType, bw);
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

    @Override public Memo add(String text) {
        Memo m = new Memo(UUID.randomUUID().toString(), text, false);
        List<Memo> list = load();
        list.add(0, m);
        save(list);
        return m;
    }

    public void updateText(String id, String text){
        List<Memo> list = load();
        for (Memo m : list) {
            if (m.id.equals(id)) { m.text = text; break; }
        }
        save(list);
    }

    @Override public void delete(String id) {
        List<Memo> list = load();
        list.removeIf(x -> x.id.equals(id));
        save(list);
    }
}
