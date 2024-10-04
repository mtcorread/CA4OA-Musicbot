package com.example.musicbot.Activity;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;

import com.example.musicbot.database.MusicDatabase;
import com.example.musicbot.database.MusicItem;
import com.example.musicbot.database.MusicItemDAO;
import com.example.musicbot.helpers.SharedPrefsUtil;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Clear the database
        clearDatabase();
        clearFavGenreForDemo(this);

    }

    private void clearDatabase() {
        AsyncTask.execute(() -> {
            MusicDatabase db = MusicDatabase.getDatabase(getApplicationContext());
            MusicItemDAO dao = db.musicItemDao();
            db.musicItemDao().deleteAll();
            dao.resetAutoIncrement();

            //db.musicItemDao().insert(new MusicItem("Rough", "GFRIEND", "https://cdn-preview-5.dzcdn.net/stream/c-5b894446904e544eeeff521cb5a23bbe-1.mp3", "https://e-cdns-images.dzcdn.net/images/cover/cfc676e73cfade3ca8647ef3e918f2c2/250x250-000000-80-0-0.jpg"));
            //db.musicItemDao().insert(new MusicItem("Lion Heart", "Girls' Generation", "https://cdn-preview-e.dzcdn.net/stream/c-ea93925c14de39427c7b35903a3919b8-5.mp3", "https://e-cdns-images.dzcdn.net/images/cover/af8b453bc2a15a9b337a9916ecdea348/250x250-000000-80-0-0.jpg"));

        });
    }

    private void clearFavGenreForDemo(Context context) {
        SharedPrefsUtil.clearFavGenre(context);
    }

}
