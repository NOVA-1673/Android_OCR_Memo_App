package com.ex.realcv.DB;


import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

@Database(entities = {MemoEntity.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract MemoDAO memoDao();

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
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }

        }
        return INSTACNCE;
    }

}
