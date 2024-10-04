package com.example.musicbot.helpers;

import android.media.MediaPlayer;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DeezerHelper {

    private DeezerUIUpdateListener uiUpdateListener;

    private OkHttpClient client;
    private MediaPlayer mediaPlayer;
    private int currentlyPlayingPosition = -1;
    private int lastRecommendedPosition = -1; // No song suggested initially
    private int pausedPosition = 0; // Keeps track of the paused position


    private boolean isLastSongPlayed = false, isPaused = false;

    public DeezerHelper(DeezerUIUpdateListener uiUpdateListener) {
        this.uiUpdateListener = uiUpdateListener;
        client = new OkHttpClient();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(mp -> handlePlaybackCompletion());
    }


    private void handlePlaybackCompletion() {
        if (currentlyPlayingPosition != -1) {
            if (uiUpdateListener != null) {
                uiUpdateListener.onPlaybackComplete(currentlyPlayingPosition);
            }
            resetMediaPlayer(); // Reset the MediaPlayer state
            currentlyPlayingPosition = -1;  // Reset position after notifying
        }
    }

    public void recommendedSong_position(int position) {
        System.out.println("SUGGEST SONG is Last Song Played true: " + isLastSongPlayed);

        this.lastRecommendedPosition = position;  // Update the last suggested song position
        this.isLastSongPlayed = false;  // Reset the flag every time a new song is suggested
        Log.d("DeezerHelper", "Suggesting song at position: " + position);

    }

    public boolean isMusicPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public void playMusic(String url, int position) {
        System.out.println("DH PlayMusic: CPP = "+currentlyPlayingPosition+", p= "+position);
        if (currentlyPlayingPosition == position && isPaused) {
            // Resuming the same song that was paused
            mediaPlayer.seekTo(pausedPosition); // Resume from last paused position
            mediaPlayer.start();
            isPaused = false;
            uiUpdateListener.onMusicStarted(position);
        } else {
            // Stopping any currently playing song
            if (currentlyPlayingPosition != -1 && currentlyPlayingPosition != position) {
                stopMusic(); // Stop and reset the current media player
                uiUpdateListener.onMusicStopped(currentlyPlayingPosition);
            }

            // Start new song or restart the same song if it was switched earlier
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(url);
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(mp -> {
                    mediaPlayer.start();
                    currentlyPlayingPosition = position;
                    pausedPosition = 0; // Reset paused position
                    isPaused = false;
                    uiUpdateListener.onMusicStarted(position);

                    if (position == lastRecommendedPosition && !isLastSongPlayed) {
                        isLastSongPlayed = true;
                        uiUpdateListener.onLastSongPlayed();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    public void stopMusic() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();

            // Notify the UI listener that music has stopped
            if (currentlyPlayingPosition != -1) {
                if (uiUpdateListener != null) {
                    uiUpdateListener.onMusicStopped(currentlyPlayingPosition);
                }
                currentlyPlayingPosition = -1;  // Reset the playing position
            }

            // Reset playback state
            pausedPosition = 0; // Reset paused position
            isPaused = false;   // Reset paused state
        }
    }

    public void pauseMusic(int position) {
        Log.d("DeezerHelper", "Pause requested on media player.");
        System.out.println("DH. pauseMusic. ==== CPP ="+ currentlyPlayingPosition+ ", position = " + position);
        if (mediaPlayer.isPlaying() && currentlyPlayingPosition == position) {
            mediaPlayer.pause();
            isPaused = true;
            pausedPosition = mediaPlayer.getCurrentPosition(); // Save the current playback position
            Log.d("DeezerHelper", "Media player paused at position: " + pausedPosition);

            if (uiUpdateListener != null) {
                uiUpdateListener.onMusicPaused(currentlyPlayingPosition);
            }
        }
    }

    public boolean isLastSongPlayed() {
        return isLastSongPlayed;
    }

    public void resetLastSongPlayed() {
        isLastSongPlayed = false;
    }


    private void resetMediaPlayer() {
        mediaPlayer.reset();
    }

    public int getCurrentlyPlayingPosition() {
        return currentlyPlayingPosition;
    }

    public void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop(); // Stop the music before releasing
            }
            mediaPlayer.release(); // Release resources
            mediaPlayer = null;
        }
    }


    public void musicSearch(String message, final MusicCallback callback) {
        String songArtist = extractSongArtist(message);
        System.out.println("Song and Artist are: " + songArtist);

        if (songArtist != null) {
            String url = "https://deezerdevs-deezer.p.rapidapi.com/search?q=" + songArtist;
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("X-RapidAPI-Key", "yourapikey")
                    .addHeader("X-RapidAPI-Host", "host")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();

                        // Extract music URL and music data
                        String musicUrl = extractMusicUrl(responseBody);
                        MusicData musicData = extractMusicData(responseBody);

                        if (musicUrl != null && musicData != null) {
                            callback.onSuccess(musicUrl, musicData);
                        } else {
                            callback.onFailure("Failed to parse music data");
                        }
                    } else {
                        callback.onFailure("Request failed with code: " + response.code());
                    }
                }
            });
        } else {
            callback.onFailure("Failed to extract song or artist from message");
        }
    }

    private String extractMusicUrl(String responseBody) {
        try {
            JSONObject jsonObject = new JSONObject(responseBody);
            JSONArray dataArray = jsonObject.getJSONArray("data");
            if (dataArray.length() > 0) {
                JSONObject firstResult = dataArray.getJSONObject(0);
                return firstResult.getString("preview");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String extractSongArtist(String message) {
        String songPattern = "<Song>\\s*(.*?)\\s*</Song>";
        String artistPattern = "<Artist>\\s*(.*?)\\s*</Artist>";

        Pattern songRegex = Pattern.compile(songPattern);
        Pattern artistRegex = Pattern.compile(artistPattern);

        Matcher songMatcher = songRegex.matcher(message);
        Matcher artistMatcher = artistRegex.matcher(message);

        String song = null;
        String artist = null;

        if (songMatcher.find()) {
            song = songMatcher.group(1);
        }

        if (artistMatcher.find()) {
            artist = artistMatcher.group(1);
        }

        if (song != null && artist != null) {
            return song + " " + artist;
        }

        return null;
    }

    private MusicData extractMusicData(String responseBody) {
        try {
            JSONObject jsonObject = new JSONObject(responseBody);
            JSONArray dataArray = jsonObject.getJSONArray("data");
            if (dataArray.length() > 0) {
                JSONObject firstResult = dataArray.getJSONObject(0);

                // Extracting the relevant fields from the response
                String musicUrl = firstResult.getString("preview");
                String trackName = firstResult.getString("title_short");
                String artistName = firstResult.getJSONObject("artist").getString("name");
                String albumCoverUrl = firstResult.getJSONObject("album").getString("cover_medium");

                return new MusicData(musicUrl, trackName, artistName, albumCoverUrl);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public interface MusicCallback {
        void onSuccess(String musicUrl, MusicData musicData);

        void onFailure(String errorMessage);
    }


    public static class MusicData {
        private String musicUrl;
        private String trackName;
        private String artistName;
        private String albumCoverUrl;
        private String id;  // Unique identifier for each MusicData instance

        public MusicData(String musicUrl, String trackName, String artistName, String albumCoverUrl) {
            this.musicUrl = musicUrl;
            this.trackName = trackName;
            this.artistName = artistName;
            this.albumCoverUrl = albumCoverUrl;
            this.id = UUID.randomUUID().toString();  // Generate a unique ID
        }

        public String getMusicUrl() {
            return musicUrl;
        }

        public String getTrackName() {
            return trackName;
        }

        public String getArtistName() {
            return artistName;
        }

        public String getAlbumCoverUrl() {
            return albumCoverUrl;
        }

        public String getId() {
            return id;
        }
    }

}






