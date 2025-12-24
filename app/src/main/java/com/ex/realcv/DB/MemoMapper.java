package com.ex.realcv.DB;

import com.ex.realcv.MemoMain.Memo;

public class MemoMapper {
    public static MemoEntity toEntity(Memo m) {
        return new MemoEntity(
                m.id, m.text, m.done,
                m.deletedAt, m.updatedAt, m.createdAt
        );
    }

    public static Memo fromEntity(MemoEntity e) {
        Memo m = new Memo(e.id, e.text, e.done);
        m.createdAt = e.createdAt;
        m.updatedAt = e.updatedAt;
        m.deletedAt = e.deletedAt;
        return m;
    }
}
