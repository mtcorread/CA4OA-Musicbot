package com.example.musicbot.helpers;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.example.musicbot.R;
import com.example.musicbot.viewmodel.GenreSelectionViewModel;

import java.util.HashMap;
import java.util.Map;


public class PopupUtil {

    private static final String TAG = "PopupUtil";

    public static void showGenreSelectionPopup(final Activity activity, final GenreSelectionViewModel genreSelectionViewModel) {
        if (activity.isFinishing() || activity.isDestroyed()) {
            Log.d(TAG, "Activity is finishing or destroyed, cannot show popup");
            return; // Ensure the activity is not finishing or destroyed
        }

        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.genre_selection_popup, activity.findViewById(android.R.id.content), false);
        Log.d(TAG, "Popup view inflated");

        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.MATCH_PARENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        Log.d(TAG, "PopupWindow created");

        // Map of view IDs and corresponding genres
        Map<Integer, String> genreMap = new HashMap<>();
        genreMap.put(R.id.cardPop, "Pop");
        genreMap.put(R.id.cardRock, "Rock");
        genreMap.put(R.id.cardHipHop, "Hip-Hop");
        genreMap.put(R.id.cardCountry, "Country");
        genreMap.put(R.id.cardJazz, "Jazz");
        genreMap.put(R.id.cardClassical, "Classical");
        genreMap.put(R.id.cardRB, "R&B");
        genreMap.put(R.id.cardReggae, "Reggae");
        genreMap.put(R.id.cardLatin, "Latin");
        genreMap.put(R.id.cardKpop, "No preference");

        // Set click listeners for each genre card
        for (Map.Entry<Integer, String> entry : genreMap.entrySet()) {
            View cardView = popupView.findViewById(entry.getKey());
            if (cardView != null) {
                cardView.setOnClickListener(v -> {
                    // Handle the genre selection
                    genreSelectionViewModel.selectGenre(entry.getValue());
                    popupWindow.dismiss();
                });
            }
        }

        new Handler(Looper.getMainLooper()).post(() -> {
            if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
                popupWindow.showAtLocation(activity.findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
            } else {
                Log.d(TAG, "Activity state changed, cannot show popup");
            }
        });

        // Handle dismissal with background click
        popupView.setOnTouchListener((v, event) -> {
            popupWindow.dismiss();
            return true;
        });
    }
}


