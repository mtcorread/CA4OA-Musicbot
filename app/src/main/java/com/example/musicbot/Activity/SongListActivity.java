package com.example.musicbot.Activity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicbot.R;
import com.example.musicbot.database.MusicItem;
import com.example.musicbot.helpers.SongAdapter;
import com.example.musicbot.viewmodel.MusicDBViewModel;
import com.example.musicbot.viewmodel.SongListViewModel;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SongListActivity extends BaseActivity {
    private MusicDBViewModel musicDBViewModel;
    private SongListViewModel songListViewModel;
    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    private TextView emptyMessageTextView;
    private MediaPlayer mediaPlayer;
    private TextView songName, artistName;
    private ImageButton playPauseButton;
    private ImageView imageViewAlbum;
    private CardView musicPlayerContainer; // Add reference to the CardView
    private boolean isPlaying = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);

        // Initialize UI elements
        songName = findViewById(R.id.track_name);
        artistName = findViewById(R.id.artist_name);
        playPauseButton = findViewById(R.id.play_pause_button);
        imageViewAlbum = findViewById(R.id.album_cover);
        musicPlayerContainer = findViewById(R.id.music_player_container);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        songListViewModel = new ViewModelProvider(this).get(SongListViewModel.class);
        SongAdapter songAdapter = new SongAdapter(song -> songListViewModel.selectSong(song));
        recyclerView.setAdapter(songAdapter);

        emptyMessageTextView = findViewById(R.id.emptyMessageTextView);

        musicDBViewModel = new ViewModelProvider(this).get(MusicDBViewModel.class);
        musicDBViewModel.getAllMusicItems().observe(this, musicItems -> {
            if (musicItems == null || musicItems.isEmpty()) {
                emptyMessageTextView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyMessageTextView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                songAdapter.setSongList(musicItems);
            }
        });

        // Observe the selected song
        songListViewModel.getSelectedSong().observe(this, song -> {
            if (song != null) {
                playSong(song);
            }
        });

        playPauseButton.setOnClickListener(v -> {
            if (isPlaying) {
                pauseSong();
            } else {
                resumeSong();
            }
        });

    }

    private void playSong(MusicItem song) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(song.getUrl());
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying = true;

            // Update UI
            songName.setText(song.getTitle());
            artistName.setText(song.getArtist());
            Picasso.get()
                    .load(song.getAlbumCoverUrl())
                    .into(imageViewAlbum);

            playPauseButton.setImageResource(R.drawable.baseline_pause_24);

            musicPlayerContainer.setVisibility(View.VISIBLE);

            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                playPauseButton.setImageResource(R.drawable.baseline_play_arrow_24);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void pauseSong() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.pause();
            isPlaying = false;
            playPauseButton.setImageResource(R.drawable.baseline_play_arrow_24);
        }
    }

    private void resumeSong() {
        if (mediaPlayer != null && !isPlaying) {
            mediaPlayer.start();
            isPlaying = true;
            playPauseButton.setImageResource(R.drawable.baseline_pause_24);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
