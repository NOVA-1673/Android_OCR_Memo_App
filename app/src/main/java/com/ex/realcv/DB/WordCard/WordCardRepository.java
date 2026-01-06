package com.ex.realcv.DB.WordCard;

import com.ex.realcv.DB.WordCard.WordEntity;
import com.ex.realcv.DB.WordCard.WordDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class WordCardRepository {

    public interface Callback<T> {
        void onResult(T data);
        void onError(Throwable e);
    }

    private final WordDAO dao;
    private final ExecutorService diskIO = Executors.newSingleThreadExecutor();

    public WordCardRepository(WordDAO dao) {
        this.dao = dao;
    }

    // ✅ 테스트 데이터가 없을 때만 삽입
    public void seedIfEmpty() {
        diskIO.execute(() -> {
            try {
                if (dao.countAll() == 0) {
                    dao.insertAll(makeTestPhrases());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void loadSession(String mode, int limit, Callback<List<WordEntity>> cb) {
        diskIO.execute(() -> {
            try {
                List<WordEntity> list = dao.getRandomAllRange(limit);
                cb.onResult(list);
            } catch (Exception e) {
                cb.onError(e);
            }
        });
    }

    private List<WordEntity> makeTestPhrases() {
        List<WordEntity> list = new ArrayList<>();

        list.add(new WordEntity(
                UUID.randomUUID().toString(),
                "DAILY",
                "おはようございます",
                "좋은 아침입니다 (정중)",
                "おはようございます",
                1
        ));

        list.add(new WordEntity(
                UUID.randomUUID().toString(),
                "BUSINESS",
                "お世話になっております",
                "신세를 지고 있습니다(비즈니스 인사)",
                "おせわになっております",
                1
        ));

        list.add(new WordEntity(
                UUID.randomUUID().toString(),
                "EXAM",
                "彼は毎日図書館へ行く",
                "그는 매일 도서관에 간다",
                "かれはまいにちとしょかんへいく",
                1
        ));

        return list;
    }

}
