package com.example.musicbot.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.musicbot.database.MusicItem;

public class SongListViewModel extends ViewModel {
    private final MutableLiveData<MusicItem> selectedSong = new MutableLiveData<>();

    public LiveData<MusicItem> getSelectedSong() {
        return selectedSong;
    }

    public void selectSong(MusicItem song) {
        selectedSong.setValue(song);
    }
}

