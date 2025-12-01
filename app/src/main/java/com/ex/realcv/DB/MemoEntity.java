package com.ex.realcv.DB;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;

@Entity(tableName = "memo")
public class MemoEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String content;

    @ColumnInfo(name = "folder_id")
    public Integer folderId;

    @ColumnInfo(name = "created_at")
    public Date createdAt;

    @ColumnInfo(name = "updated_at")
    public Date updatedAt;
}