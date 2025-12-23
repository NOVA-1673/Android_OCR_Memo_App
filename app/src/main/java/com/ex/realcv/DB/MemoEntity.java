package com.ex.realcv.DB;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import androidx.annotation.NonNull;

import java.util.Date;

@Entity(tableName = "memos")
public class MemoEntity {

    @PrimaryKey
    @NonNull
    public String id;

    @ColumnInfo(name = "text")
    public String text;

    @ColumnInfo(name = "done")
    public Boolean done;

    @ColumnInfo(name = "deleted_at")
    public Long deletedAt;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    // ✅ Room이 사용하는 기본 생성자
    public MemoEntity() {
    }
    public MemoEntity(@NonNull String id, String text, boolean done,
                      Long deleteAt, long updateAt, long createAt){

        this.id = id;
        this.text = text;
        this.done = done;
        this.deletedAt = deleteAt;
        this.updatedAt = updateAt;
        this.createdAt = createAt;


    }

}