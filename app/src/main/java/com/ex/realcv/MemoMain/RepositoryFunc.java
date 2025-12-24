package com.ex.realcv.MemoMain;

import com.ex.realcv.Func.ResultCall;

import java.util.ArrayList;
import java.util.List;

public interface RepositoryFunc {

    public interface Callback<T> {
        void onResult(ResultCall<T> result);
    }


    List<Memo> load();
    //void save(List<Memo> list);
    Memo add(String text);
    void delete(String id);

    //--------실사용--------

    //---조회---
    public void activeMemo(RoomMemoRepository.Callback<List<Memo>> cb);
    public void softDeletedMemo(RoomMemoRepository.Callback<List<Memo>> cb);
    //---삭제/복구--
    public void softDelete(String id, Callback<Void> cb);
    public void hardDelete(String id, Callback<Void> cb);
    public void restore(String id, Callback<Void> cb);
    //---수정/삽입---
    public void updateText(String id, String newText);
    public void toggleDone(String id, boolean done);
    public void addBlocks(ArrayList<BlockMemo> blocks, Callback<Void> cb);
    public void loadBlocks(String id, Callback<ArrayList<BlockMemo>> cb);
    public void updateBlocks(String id, ArrayList<BlockMemo> blocks, Callback<Void> cb);

}
