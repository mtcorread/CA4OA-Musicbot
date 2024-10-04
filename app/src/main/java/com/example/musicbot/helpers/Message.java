package com.example.musicbot.helpers;

public class Message {
    public static final int SENT_BY_BOT = 1;
    public static final int SENT_BY_USER = 2;
    public static final int MUSIC_ITEM = 3;

    private String content;
    private int type;
    private DeezerHelper.MusicData musicData;  // Hold music data

    public Message(String content, int type) {
        this.content = content;
        this.type = type;
    }

    public Message(DeezerHelper.MusicData musicData) {
        this.musicData = musicData;
        this.type = MUSIC_ITEM;
    }

    public String getContent() {
        return content;
    }

    public int getType() {
        return type;
    }

    public DeezerHelper.MusicData getMusicData() {
        return musicData;
    }
}
