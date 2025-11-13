package com.ex.realcv.MemoMain;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Memo {
    public String id;
    public String text;
    public boolean done;
    public Long deletedAt;
    public long updatedAt;

    public long createdAt;


    public Memo(String id, String text, boolean done) {
        this(id, text, done, System.currentTimeMillis()); // 기본값: 지금 시간
    }

    public Memo(String id, String text, boolean done, long timestamp) {
        this.id = id;
        this.text = text;
        this.done = done;
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
        this.deletedAt = null;
    }

    // ✅ 시간 포맷을 보기 좋게 반환 (UI 표시용)
    public String getFormattedTime() {
        Date date = new Date(createdAt);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(date);
    }
    public String getFormattedTime(long d) {
        Date date = new Date(d);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    public String getText(){  return text;  }

    // --- getter/setter ---
    public String getId() { return id; }
    //public String getTitle() { return title; }
   // public String getBody() { return body; }
   // public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public String getUpdatedAtByString() { return getFormattedTime(updatedAt);}
    public boolean isDone() { return done; }
    public Long getDeletedAt() { return deletedAt; }

   // public void setTitle(String title) { this.title = title; touch(); }
    //public void setBody(String body) { this.body = body; touch(); }
    public void setDone(boolean done) { this.done = done; touch(); }
    public void setDeletedAt(Long t) { deletedAt = t; touch(); }

    private void touch() { this.updatedAt = System.currentTimeMillis(); }
}
