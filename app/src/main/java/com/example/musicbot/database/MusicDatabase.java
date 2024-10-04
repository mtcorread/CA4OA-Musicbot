package com.example.musicbot.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

@Database(entities = {MusicItem.class}, version = 2, exportSchema = false)
public abstract class MusicDatabase extends RoomDatabase {
    private static volatile MusicDatabase INSTANCE;

    public abstract MusicItemDAO musicItemDao();

    public static MusicDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (MusicDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    MusicDatabase.class, "music_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

