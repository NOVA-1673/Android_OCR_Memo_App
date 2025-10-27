package com.ex.realcv.MemoMain;

import java.util.List;

public interface RepositoryFunc {
    List<Memo> load();
    void save(List<Memo> list);
    Memo add(String text);
    void delete(String id);
}
