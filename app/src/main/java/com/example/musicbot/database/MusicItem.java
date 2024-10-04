package com.example.musicbot.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "music_items")
public class MusicItem {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String artist;
    private String url;
    private String albumCoverUrl; // New field for album image URL

    public MusicItem() {
        // No-argument constructor required by Room
    }

    public MusicItem(String title, String artist, String url, String albumCoverUrl) {
        this.title = title;
        this.artist = artist;
        this.url = url;
        this.albumCoverUrl = albumCoverUrl;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAlbumCoverUrl() {
        return albumCoverUrl;
    }

    public void setAlbumCoverUrl(String albumImageUrl) {
        this.albumCoverUrl = albumImageUrl;
    }
}

