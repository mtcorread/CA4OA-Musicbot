package com.example.musicbot.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MusicItemDAO {
    @Insert
    void insert(MusicItem musicItem);

    @Query("SELECT * FROM music_items")
    LiveData<List<MusicItem>> getAllMusicItems();

    @Query("DELETE FROM music_items")
    void deleteAll();

    @Query("DELETE FROM sqlite_sequence WHERE name = 'music_items'")
    void resetAutoIncrement();
}
