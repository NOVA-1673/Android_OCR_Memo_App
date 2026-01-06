package com.ex.realcv.DB.WordCard;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface WordDAO {

    @Query("SELECT * FROM phrases WHERE mode = :mode ORDER BY RANDOM() LIMIT :limit")
    List<WordEntity> getRandomByMode(String mode, int limit);

    @Query("SELECT * FROM phrases ORDER BY RANDOM() LIMIT :limit")
    List<WordEntity> getRandomAllRange(int limit);

    // 🔹 테스트 데이터 일괄 삽입
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<WordEntity> phrases);

    // 🔹 단건 삽입 (필요하면)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(WordEntity phrase);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<WordEntity> list);

    @Query("SELECT COUNT(*) FROM phrases WHERE mode = :mode")
    int countByMode(String mode);

    @Query("SELECT COUNT(*) FROM phrases")
    int countAll();

}
