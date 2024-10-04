package com.example.musicbot.helpers;

public interface MusicControlInterface {
    void playMusic(String url, int position);
    void pauseMusic(int position);
    void stopMusic(int position);

}
