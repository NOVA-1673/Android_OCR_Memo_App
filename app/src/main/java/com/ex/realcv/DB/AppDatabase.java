package com.ex.realcv.DB;


import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import com.ex.realcv.DB.WordCard.WordDAO;
import com.ex.realcv.DB.WordCard.WordEntity;

@Database(entities = {MemoEntity.class, WordEntity.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract MemoDAO memoDao();
    public abstract WordDAO wordDao();

    private static volatile AppDatabase INSTACNCE;

    public static AppDatabase getInstance(Context context){
        if(INSTACNCE == null){
            synchronized (AppDatabase.class){
                if(INSTACNCE == null){
                    INSTACNCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "realcv.db"
                            )
                            //.createFromAsset("JapanPhrase01.db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTACNCE;
    }
}

