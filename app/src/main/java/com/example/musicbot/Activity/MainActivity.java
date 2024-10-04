package com.example.musicbot.Activity;

import static com.example.musicbot.helpers.PopupUtil.showGenreSelectionPopup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.example.musicbot.R;
import com.example.musicbot.databinding.ActivityMainBinding;
import com.example.musicbot.helpers.PopupUtil;
import com.example.musicbot.helpers.SharedPrefsUtil;
import com.example.musicbot.viewmodel.GenreSelectionViewModel;
import com.example.musicbot.viewmodel.MainViewModel;

public class MainActivity extends BaseActivity {
    private MainViewModel mainViewModel;
    private ActivityMainBinding binding;
    private GenreSelectionViewModel genreSelectionViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize data binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // Initialize ViewModel
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        genreSelectionViewModel = new ViewModelProvider(this).get(GenreSelectionViewModel.class);

        // Set ViewModel in binding
        binding.setViewModel(mainViewModel);

        // Ensure LiveData is lifecycle aware
        binding.setLifecycleOwner(this);

        // Observe genre selection changes
        genreSelectionViewModel.getSelectedGenre().observe(this, genre -> {
            if (genre != null) {
                SharedPrefsUtil.storeVariableInSharedPreferences(this, "favGenre", genre);
            }
        });

        mainViewModel.navigateToSettings.observe(this, navigate -> {
            if (navigate != null && navigate) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                mainViewModel.doneNavigatingToSettings(); // Reset navigation state
            }
        });

        mainViewModel.navigateToChat.observe(this, navigate -> {
            if (navigate != null && navigate) {
                startActivity(new Intent(MainActivity.this, ChatActivity.class));
                mainViewModel.doneNavigatingToChat(); // Reset navigation state
            }
        });

        mainViewModel.navigateToSongList.observe(this, navigate -> {
            if (navigate != null && navigate) {
                startActivity(new Intent(MainActivity.this, SongListActivity.class));
                mainViewModel.doneNavigatingToSongList(); // Reset navigation state
            }
        });

        // Show the genre selection popup only if the genre is not set
        /*if (!SharedPrefsUtil.isFavGenreSet(this)) {
            PopupUtil.showGenreSelectionPopup(this, genreSelectionViewModel);
        }*/
    }

}