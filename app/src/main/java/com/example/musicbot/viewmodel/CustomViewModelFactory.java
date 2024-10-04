package com.example.musicbot.viewmodel;

import android.app.Application;
import android.support.annotation.NonNull;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.musicbot.helpers.DeezerHelper;
import com.example.musicbot.helpers.DeezerUIUpdateListener;

public class CustomViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;
    private final DeezerHelper deezerHelper;

    public CustomViewModelFactory(Application application, DeezerHelper deezerHelper) {
        this.application = application;
        this.deezerHelper = deezerHelper;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ChatViewModel.class)) {
            return (T) new ChatViewModel(application, deezerHelper);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}

