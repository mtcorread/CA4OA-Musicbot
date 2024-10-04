package com.example.musicbot.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {

    private MutableLiveData<Boolean> _navigateToChat = new MutableLiveData<>();
    private MutableLiveData<Boolean> _navigateToSettings = new MutableLiveData<>();
    private MutableLiveData<Boolean> _navigateToSongList = new MutableLiveData<>();


    public LiveData<Boolean> navigateToChat = _navigateToChat;
    public LiveData<Boolean> navigateToSettings = _navigateToSettings;
    public LiveData<Boolean> navigateToSongList = _navigateToSongList;


    public void onChatClicked() {
        _navigateToChat.setValue(true);
    }
    public void onSongListClicked() {
        _navigateToSongList.setValue(true);
    }


    public void onSettingsClicked() {
        _navigateToSettings.setValue(true);
    }


    // Reset methods to update the LiveData back to false

    public void doneNavigatingToChat() {
        _navigateToChat.setValue(false);
    }
    public void doneNavigatingToSongList() {
        _navigateToSongList.setValue(false);
    }


    public void doneNavigatingToSettings() {
        _navigateToSettings.setValue(false);
    }
}


