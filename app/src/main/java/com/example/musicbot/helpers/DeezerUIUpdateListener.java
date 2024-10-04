package com.example.musicbot.helpers;

public interface DeezerUIUpdateListener {

    void onPlaybackComplete(int position);
    void onLastSongPlayed();
    void onMusicStarted(int position);
    void onMusicStopped(int position);
    void onMusicPaused(int position);
    void onMusicPlaybackReset();  // New method for when the playback state is reset

}
