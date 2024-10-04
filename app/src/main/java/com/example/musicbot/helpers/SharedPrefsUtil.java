package com.example.musicbot.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsUtil {
    private static final String[] GENRES = {"Pop", "Rock", "Hip-Hop", "Country", "Jazz", "Classical", "R&B", "Reggae", "Latin", "K-Pop"};

    public interface OnGenreSelectedListener {
        void onGenreSelected(String genre);
    }

    public static void showFavoriteGenreDialog(Activity activity, OnGenreSelectedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Select your favourite music genre")
                .setItems(GENRES, (dialog, which) -> {
                    String selectedGenre = GENRES[which];
                    storeVariableInSharedPreferences(activity, "favGenre", selectedGenre);
                    if (listener != null) {
                        listener.onGenreSelected(selectedGenre);
                    }
                })
                .setCancelable(false)
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public static void storeVariableInSharedPreferences(Context context, String key, String value) {
        SharedPreferences sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        System.out.println("Key: " + key + "; Value: " + value);
        editor.apply();
    }

    public static String getVariableFromSharedPreferences(Context context, String key) {
        SharedPreferences sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        return sharedPref.getString(key, null);
    }

    public static boolean isFavGenreSet(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        return sharedPref.contains("favGenre") && sharedPref.getString("favGenre", null) != null;
    }

    public static void clearFavGenre(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove("favGenre");
        editor.apply();
    }
}
