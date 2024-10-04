package com.example.musicbot.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.musicbot.database.MusicItem;
import com.example.musicbot.repositories.MusicRepository;

import java.util.List;

public class MusicDBViewModel extends AndroidViewModel {
    private MusicRepository repository;
    private LiveData<List<MusicItem>> allMusicItems;
    private static final String TAG = "MusicDBViewModel";

    public MusicDBViewModel(Application application) {
        super(application);
        repository = new MusicRepository(application);
        allMusicItems = repository.getAllMusicItems();
    }

    public LiveData<List<MusicItem>> getAllMusicItems() {
        return allMusicItems;
    }

    public void insert(MusicItem musicItem) {
        Log.d(TAG, "Inserting music item into repository: " + musicItem.getTitle());
        repository.insert(musicItem);
    }
}
