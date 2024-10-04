package com.example.musicbot.Activity;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.musicbot.R;
import com.example.musicbot.helpers.FontManager;
import com.example.musicbot.helpers.PopupUtil;
import com.example.musicbot.helpers.SharedPrefsUtil;
import com.example.musicbot.helpers.ThemeManager;
import com.example.musicbot.viewmodel.GenreSelectionViewModel;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends BaseActivity {
    private RadioGroup radioGroupFontSize;
    private SwitchMaterial themeToggleSwitch, ttsSwitch;
    TextView favGenreList, favGenreText;
    SharedPrefsUtil shPref;
    private SharedPreferences sharedPreferences;
    private GenreSelectionViewModel genreSelectionViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        themeToggleSwitch = findViewById(R.id.themeToggleSwitch);
        radioGroupFontSize = findViewById(R.id.radioGroupFontSize);
        ttsSwitch = findViewById(R.id.TTSToggleSwitch);
        favGenreList = findViewById(R.id.favGenreToggleLabel);
        favGenreText = findViewById(R.id.favGenreActualLabel);
        shPref = new SharedPrefsUtil();
        sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE);

        // Initialize ViewModel
        genreSelectionViewModel = new ViewModelProvider(this).get(GenreSelectionViewModel.class);

        // Observe selected genre changes
        genreSelectionViewModel.getSelectedGenre().observe(this, genre -> {
            if (genre != null) {
                shPref.storeVariableInSharedPreferences(this, "favGenre", genre);
                updateFavTitleText();
            }
        });

        updateFavTitleText();

        // Load saved preferences
        FontManager fontManager = FontManager.getInstance(this);
        String fontSize = fontManager.getSavedFontSize();

        if ("Large".equals(fontSize)) {
            radioGroupFontSize.check(R.id.radioButtonLarge);
        } else if ("ExtraLarge".equals(fontSize)) {
            radioGroupFontSize.check(R.id.radioButtonExtraLarge);
        } else {
            radioGroupFontSize.check(R.id.radioButtonNormal);
        }

        radioGroupFontSize.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioButtonLarge) {
                fontManager.setFontSize("Large");
            } else if (checkedId == R.id.radioButtonExtraLarge) {
                fontManager.setFontSize("ExtraLarge");
            } else {
                fontManager.setFontSize("Normal");
            }
        });

        // Load the saved TTS preference
        boolean isTtsEnabled = sharedPreferences.getBoolean("isTtsEnabled", true);
        ttsSwitch.setChecked(isTtsEnabled);

        // Set a listener to save the preference when the switch is toggled
        ttsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isTtsEnabled", isChecked);
            editor.apply();
        });

        ThemeManager themeManager = ThemeManager.getInstance(getApplicationContext());

        // Observe the LiveData to update the switch when the theme changes
        themeManager.getDarkThemeEnabled().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isDarkTheme) {
                themeToggleSwitch.setOnCheckedChangeListener(null);  // Remove the listener temporarily
                themeToggleSwitch.setChecked(isDarkTheme);
                themeToggleSwitch.setOnCheckedChangeListener(switchChangeListener);  // Add the listener back
            }
        });

        // Set the initial state of the switch
        boolean isDarkTheme = themeManager.getDarkThemeEnabled().getValue() != null && themeManager.getDarkThemeEnabled().getValue();
        themeToggleSwitch.setChecked(isDarkTheme);
        themeToggleSwitch.setOnCheckedChangeListener(switchChangeListener);  // Add the listener back

        favGenreList.setOnClickListener(v -> {
            showFavouriteGenrePopUp();
        });


    }

    private void showFavouriteGenrePopUp(){
        PopupUtil.showGenreSelectionPopup(this, genreSelectionViewModel);
    }

    // Define the listener separately
    private final CompoundButton.OnCheckedChangeListener switchChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            ThemeManager.getInstance(getApplicationContext()).toggleTheme();
        }
    };

    private void updateFavTitleText() {
        favGenreText.setText(shPref.getVariableFromSharedPreferences(this, "favGenre"));
    }

}
