package com.example.musicbot.repositories;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.musicbot.database.MusicDatabase;
import com.example.musicbot.database.MusicItem;
import com.example.musicbot.database.MusicItemDAO;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MusicRepository {
    private MusicItemDAO musicItemDao;
    private LiveData<List<MusicItem>> allMusicItems;
    private static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);
    private static final String TAG = "MusicRepository";

    public MusicRepository(Application application) {
        MusicDatabase db = MusicDatabase.getDatabase(application);
        musicItemDao = db.musicItemDao();
        allMusicItems = musicItemDao.getAllMusicItems();
    }

    public LiveData<List<MusicItem>> getAllMusicItems() {
        return allMusicItems;
    }

    public void insert(MusicItem musicItem) {
        databaseWriteExecutor.execute(() -> {
            Log.d(TAG, "Inserting music item: " + musicItem.getTitle());
            musicItemDao.insert(musicItem);
        });
    }
}
