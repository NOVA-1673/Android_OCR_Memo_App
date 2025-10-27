package com.ex.realcv.MemoMain;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Memo {
    public String id;
    public String text;
    public boolean done;
    private Long deletedAt;

    public long timestamp;

    public Memo(String id, String text, boolean done) {
        this(id, text, done, System.currentTimeMillis()); // 기본값: 지금 시간
    }

    public Memo(String id, String text, boolean done, long timestamp) {
        this.id = id;
        this.text = text;
        this.done = done;
        this.timestamp = timestamp;
        this.deletedAt = null;
    }

    // ✅ 시간 포맷을 보기 좋게 반환 (UI 표시용)
    public String getFormattedTime() {
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    public Long getDeletedAt() { return deletedAt; }
}
