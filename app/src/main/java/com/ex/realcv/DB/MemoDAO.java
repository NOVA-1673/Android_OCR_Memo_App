package com.ex.realcv.DB;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MemoDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(MemoEntity memo);

    //하드 삭제
    @Query("DELETE FROM memos WHERE id = :id")
    void deleteById(String id);

    //소프트 삭제
    @Query("UPDATE memos SET deleted_at = :ts, updated_at = :ts WHERE id = :id")
    void softDelete(String id, long ts);

    //소프트 삭제에서 복구
    @Query("UPDATE memos SET deleted_at = null, updated_at = :ts WHERE id = :id")
    void restoreMemo(String id, long ts);

    // 삭제 안 된 메모만 최신순
    @Query("SELECT * FROM memos WHERE deleted_at IS NULL ORDER BY updated_at DESC")
    List<MemoEntity> getActiveMemos();


    @Query("UPDATE memos SET text = :text, updated_at = :ts WHERE id = :id")
    void updateText(String id, String text, long ts);
    @Query("UPDATE memos SET done = :done, updated_at = :ts WHERE id = :id")
    void updateDone(String id, boolean done, long ts);


    //------------조회------------
    //soft 삭제 메모 리스트
    @Query("SELECT * FROM memos WHERE deleted_at IS NOT NULL ORDER BY updated_at DESC")
    List<MemoEntity> getSoftDeletedMemos();

    // 완료 여부 필터 예시
    @Query("SELECT * FROM memos WHERE deleted_at IS NULL AND done = :done ORDER BY updated_at DESC")
    List<MemoEntity> getActiveMemosByDone(boolean done);

    // 전체(삭제 포함)
    @Query("SELECT * FROM memos ORDER BY updated_at DESC")
    List<MemoEntity> getAll();

    //id로 검색
    @Query("SELECT * FROM memos WHERE id = :id LIMIT 1")
    MemoEntity getById(String id);

}
