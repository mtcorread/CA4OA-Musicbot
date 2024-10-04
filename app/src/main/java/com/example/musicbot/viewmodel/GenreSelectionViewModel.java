package com.example.musicbot.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class GenreSelectionViewModel extends ViewModel {

    private final MutableLiveData<String> selectedGenre = new MutableLiveData<>();

    public void selectGenre(String genre) {
        selectedGenre.setValue(genre);
    }

    public LiveData<String> getSelectedGenre() {
        return selectedGenre;
    }
}

