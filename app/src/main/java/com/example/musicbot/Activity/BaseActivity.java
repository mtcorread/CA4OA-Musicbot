package com.example.musicbot.Activity;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.example.musicbot.R;
import com.example.musicbot.helpers.FontManager;
import com.example.musicbot.helpers.ThemeManager;


public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";
    private Boolean currentThemeIsDark;
    private String currentFontSize;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Get the current theme from ThemeManager and set it before observing changes
        currentThemeIsDark = ThemeManager.getInstance(this).getDarkThemeEnabled().getValue();
        setTheme(currentThemeIsDark ? R.style.Base_Theme_MusicBot_Dark : R.style.Base_Theme_MusicBot);

        ThemeManager.getInstance(this).getDarkThemeEnabled().observe(this, useDarkTheme -> {
            if (currentThemeIsDark != null && !currentThemeIsDark.equals(useDarkTheme)) {
                currentThemeIsDark = useDarkTheme;
                recreate();  // Recreate only if there's an actual change and not the initial setup
            } else {
                currentThemeIsDark = useDarkTheme;  // Initial setup without recreation
            }
        });

        ////////////////////////////////////////

        FontManager fontManager = FontManager.getInstance(this);
        currentFontSize = fontManager.getSavedFontSize();

        fontManager.getFontSizeLiveData().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String newFontSize) {
                if (!currentFontSize.equals(newFontSize)) {
                    currentFontSize = newFontSize;
                    recreate();  // Recreate activity to apply new font size
                }
            }
        });



    }

    protected void attachBaseContext(Context newBase) {
        FontManager fontManager = FontManager.getInstance(newBase);
        String fontSize = fontManager.getSavedFontSize();
        Context context = updateBaseContextFont(newBase, fontSize);
        super.attachBaseContext(context);
    }

    private Context updateBaseContextFont(Context context, String fontSize) {
        float scale;
        switch (fontSize) {
            case "Large":
                scale = 1.25f; // 25% larger
                break;
            case "ExtraLarge":
                scale = 1.5f; // 50% larger
                break;
            default:
                scale = 1.0f; // Normal
        }

        Configuration configuration = context.getResources().getConfiguration();
        configuration.fontScale = scale;
        return context.createConfigurationContext(configuration);
    }

}

