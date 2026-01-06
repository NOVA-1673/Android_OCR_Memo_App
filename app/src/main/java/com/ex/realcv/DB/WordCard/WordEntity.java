package com.ex.realcv.DB.WordCard;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "phrases")
public class WordEntity {

    @PrimaryKey
    @NonNull
    public String id;

    @ColumnInfo(name = "mode")
    @NonNull
    public String mode;  // "DAILY", "BUSINESS", ...

    @ColumnInfo(name = "jp_text")
    @NonNull
    public String jpText;

    @ColumnInfo(name = "explain_text")
    public String explainText; // nullable OK

    @ColumnInfo(name = "tts_text")
    public String ttsText;     // nullable OK

    @ColumnInfo(name = "difficulty")
    public int difficulty; // 1~5 or 1~10

    public WordEntity(@NonNull String id,
                             @NonNull String mode,
                             @NonNull String jpText,
                             String explainText,
                             String ttsText,
                             int difficulty) {
        this.id = id;
        this.mode = mode;
        this.jpText = jpText;
        this.explainText = explainText;
        this.ttsText = ttsText;
        this.difficulty = difficulty;
    }
}
