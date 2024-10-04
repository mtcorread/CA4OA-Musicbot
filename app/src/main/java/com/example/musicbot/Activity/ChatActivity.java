package com.example.musicbot.Activity;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.musicbot.R;
import com.example.musicbot.database.MusicItem;
import com.example.musicbot.helpers.DeezerUIUpdateListener;
import com.example.musicbot.helpers.Message;
import com.example.musicbot.helpers.MessageAdapter;
import com.example.musicbot.helpers.MusicControlInterface;
import com.example.musicbot.helpers.SpeechRecognitionHelper;
import com.example.musicbot.helpers.TTSHelper;
import com.example.musicbot.helpers.DeezerHelper;
import com.example.musicbot.viewmodel.ChatViewModel;
import com.example.musicbot.databinding.ActivityChatBinding;
import com.example.musicbot.viewmodel.CustomViewModelFactory;
import com.example.musicbot.viewmodel.MusicDBViewModel;

import java.util.List;


public class ChatActivity extends BaseActivity implements SpeechRecognitionHelper.SpeechResultListener, DeezerUIUpdateListener, MusicControlInterface {

    private ChatViewModel chatViewModel;
    private MusicDBViewModel musicDBViewModel;
    private ActivityChatBinding binding;
    private MessageAdapter messageAdapter;
    private SpeechRecognizer speechRecognizer;
    private SpeechRecognitionHelper speechHelper;
    private TTSHelper ttsHelper;
    DeezerHelper deezerHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize data binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat);

        // Initialize DeezerHelper
        deezerHelper = new DeezerHelper(this); // Ensure DeezerHelper is initialized

        // Initialize ViewModel with DeezerUIUpdateListener and DeezerHelper
        chatViewModel = new ViewModelProvider(this, new CustomViewModelFactory(getApplication(), deezerHelper)).get(ChatViewModel.class);
        musicDBViewModel = new ViewModelProvider(this).get(MusicDBViewModel.class);


        // Set ViewModel in binding
        binding.setViewModel(chatViewModel);

        // Ensure LiveData is lifecycle aware
        binding.setLifecycleOwner(this);

        // Retrieve the variable from SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String _favGenre = sharedPref.getString("favGenre", null);

        // Pass the variable to the ViewModel
        chatViewModel.setFavGenre(_favGenre);

        // Set up RecyclerView
        messageAdapter = new MessageAdapter(chatViewModel.getMessageListLiveData().getValue(), this);
        binding.recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.recyclerView.setLayoutManager(layoutManager);

        // Observe message list changes
        chatViewModel.getMessageListLiveData().observe(this, messages -> {
            messageAdapter.setMessageList(messages);
            binding.recyclerView.scrollToPosition(messages.size() - 1);
        });

        // Observe scroll position changes
        chatViewModel.getScrollToPositionLiveData().observe(this, position -> {
            if (position != null) {
                binding.recyclerView.smoothScrollToPosition(position);
            }
        });

        // Observe loading state
        chatViewModel.getIsLoadingLiveData().observe(this, isLoading -> {
            binding.loadingText.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Observe button enablement state
        chatViewModel.getIsButtonsEnabledLiveData().observe(this, isEnabled -> {
            binding.sendBtn.setEnabled(isEnabled);
            binding.talkBtn.setEnabled(isEnabled);
        });

        // Observe clear input field state
        chatViewModel.getClearInputFieldLiveData().observe(this, clearInput -> {
            if (clearInput) {
                binding.messageEditText.setText("");
                chatViewModel.clearInputFieldDone(); // Notify ViewModel that input field has been cleared
            }
        });

        // Observe toast messages
        chatViewModel.getToastMessageLiveData().observe(this, toastMessage -> {
            if (toastMessage != null) {
                Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
            }
        });

        // Observe welcome view visibility state
        chatViewModel.getIsWelcomeVisibleLiveData().observe(this, isVisible -> {
            binding.welcomeText.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        });

        // Observe Skip button visibility state
        chatViewModel.getIsSkipBtnVisibleLiveData().observe(this, isVisible -> {
            binding.skipBtn.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        });

        // Initialize helpers that require Activity context
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechHelper = new SpeechRecognitionHelper(this, this, binding.listeningText);
        ttsHelper = new TTSHelper(this, null);

        // Observe UI BOT response
        chatViewModel.getUiBOTResponseLiveData().observe(this, response -> {
            if (response != null) {
                addToChatUI(response, Message.SENT_BY_BOT);
            }
        });

        // Observe UI USER response
        chatViewModel.getUiUSERResponseLiveData().observe(this, response -> {
            if (response != null) {
                addToChatUI(response, Message.SENT_BY_USER);
            }
        });

        // Observe music response and handle UI update and database insertion
        chatViewModel.getUiMUSICResponseLiveData().observe(this, musicData -> {
            if (musicData != null) {
                addMusicToChatUI(musicData);  // Update the chat UI with the music item

                // Insert the music data into the database
                MusicItem musicItem = new MusicItem();
                musicItem.setTitle(musicData.getTrackName());
                musicItem.setArtist(musicData.getArtistName());
                musicItem.setUrl(musicData.getMusicUrl());
                musicItem.setAlbumCoverUrl(musicData.getAlbumCoverUrl());

                musicDBViewModel.insert(musicItem);
            }
        });

        // Observe the LiveData event for removing the last message
        chatViewModel.getRemoveLastMessageLiveData().observe(this, shouldRemove -> {
            if (shouldRemove != null && shouldRemove) {
                removeChatUI();
            }
        });

        // Observe speakLiveData
        chatViewModel.getSpeakLiveData().observe(this, text -> {
            if (text != null && !text.isEmpty()) {
                chatViewModel.getLatchLiveData().observe(this, latch -> {
                    if (latch != null) {
                        ttsHelper.speakOut(text, latch);
                    } else {
                        ttsHelper.speakOut_simple(text);
                    }
                });
            }
        });

        // Observe the isSongPlayed LiveData
        chatViewModel.isSongPlayed.observe(this, isPlayed -> {
            if (isPlayed != null && isPlayed) {
                // Perform any action when a song has been played
                Log.d("ChatActivity", "A song has been played");
            }
        });

        // Set up button click listeners
        binding.sendBtn.setOnClickListener(v -> {
            chatViewModel.handleUserInput(binding.messageEditText.getText().toString().trim());
            binding.messageEditText.setText("");  // Clear the EditText after sending the message
        });

        binding.talkBtn.setOnClickListener(v -> {
            if (ttsHelper.isSpeaking()) {
                ttsHelper.stopSpeaking();
            }
            speechHelper.startListening();
        });
        binding.skipBtn.setOnClickListener(v -> {
            if (ttsHelper.isSpeaking()) {
                ttsHelper.stopSpeaking();
            }
            binding.recyclerView.getRecycledViewPool().clear();
            chatViewModel.removeLastChunkAndDisplayEntireMessage(chatViewModel.getTrimmedResponse(),
                    ()-> {
                            chatViewModel.showSong(chatViewModel.getCompleteResponse());
                            chatViewModel.displayExtractedQuestions(chatViewModel.getExtractedQuestions());
            });
        });

        // Check for audio recording permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            speechHelper.checkPermission();
        }

        // Call retrieveAndCreate method from ViewModel
        chatViewModel.retrieveAndCreate("asst_OJZkZLb9OiMTZrflC4HES2tr");


    }

    private void addToChatUI(String input, int sentBy) {
        if (!input.trim().isEmpty()) { // Ensure the input is not empty after trimming
            runOnUiThread(() -> {
                int insertPosition = chatViewModel.getMessageListLiveData().getValue().size();
                chatViewModel.getMessageListLiveData().getValue().add(new Message(input.trim(), sentBy));
                messageAdapter.notifyItemInserted(insertPosition);
                binding.recyclerView.smoothScrollToPosition(insertPosition);
            });
        }
    }

    private void removeChatUI() {
        List<Message> messageList = chatViewModel.getMessageListLiveData().getValue();
        if (messageList != null && !messageList.isEmpty()) {
            runOnUiThread(() -> {
                int removePosition = messageList.size() - 1;
                messageList.remove(removePosition); // Remove the last message
                messageAdapter.notifyItemRemoved(removePosition); // Notify the adapter about item removal
                if (!messageList.isEmpty()) {
                    binding.recyclerView.smoothScrollToPosition(messageList.size() - 1);
                }
            });
        }
    }
    private void addMusicToChatUI(DeezerHelper.MusicData musicData) {
        runOnUiThread(() -> {
            int insertPosition = chatViewModel.getMessageListLiveData().getValue().size();
            chatViewModel.getMessageListLiveData().getValue().add(new Message(musicData));
            messageAdapter.notifyItemInserted(insertPosition);
            binding.recyclerView.smoothScrollToPosition(insertPosition);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (deezerHelper != null) {
            deezerHelper.releaseMediaPlayer();
        }
        speechRecognizer.destroy();
        ttsHelper.shutdown();
    }


    protected void onPause(int position) {
        super.onPause();
        if (deezerHelper != null && deezerHelper.isMusicPlaying()) {
            deezerHelper.pauseMusic(position);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (deezerHelper != null) {
            deezerHelper.stopMusic();
            deezerHelper.releaseMediaPlayer(); // Release MediaPlayer resources
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        speechHelper.onRequestPermissionsResult(requestCode, grantResults);
    }

    @Override
    public void onSpeechResult(String text) {
        chatViewModel.handleUserInput(text);
    }

    @Override
    public void onPlaybackComplete(int position) {
        runOnUiThread(() -> {
            messageAdapter.setCurrentlyPlayingPosition(-1);
            messageAdapter.notifyItemChanged(position);
        });
    }

    public void onLastSongPlayed() {
        Log.d("ChatActivity", "onLastSongPlayed called, calling displayFeelingMessage");
        chatViewModel.displayFeelingMessage(chatViewModel.getCompleteResponse());
        chatViewModel.setSongPlayed(true);
    }

    @Override
    public void onMusicStarted(int position) {
        runOnUiThread(() -> messageAdapter.notifyItemChanged(position));
    }

    @Override
    public void onMusicStopped(int position) {
        runOnUiThread(() -> {
            if (position != -1) {
                messageAdapter.notifyItemChanged(position);
            }
            messageAdapter.notifyDataSetChanged();  // Refresh the entire list when music stops
        });
    }

    public void onMusicPaused(int position) {
        runOnUiThread(() -> {
            if (position != -1) {
                messageAdapter.notifyItemChanged(position);
                System.out.println("CA: onMusicPaused (entró a IF) . cpp= "+messageAdapter.getCurrentlyPlayingPosition() +", position = "+position);
            }
            messageAdapter.notifyDataSetChanged();  // Refresh the entire list when music pauses
            System.out.println("CA: onMusicPaused . cpp= "+messageAdapter.getCurrentlyPlayingPosition() +", position = "+position);

        });
    }

    @Override
    public void onMusicPlaybackReset() {
        runOnUiThread(() -> {
            // Update the UI to reflect that there is no current playback
            // This could include disabling playback controls, updating text, etc.
            Toast.makeText(this, "Playback state reset", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void playMusic(String url, int position) {
        System.out.println("Entrando a CA playMusic");

        // Ensure deezerHelper is properly initialized before using it
        if (deezerHelper != null) {
            int currentPlayingPosition = messageAdapter.getCurrentlyPlayingPosition();
            boolean isSamePosition = (currentPlayingPosition == position);

            if (isSamePosition && deezerHelper.isMusicPlaying()) {
                // Pause the currently playing music
                System.out.println("CPP = P, IsMusicPlaying");
                deezerHelper.pauseMusic(position);
                messageAdapter.setCurrentlyPlayingPosition(-1); // Indicates no music is currently playing
            } else if (isSamePosition) {
                // Resuming the same song that was paused
                System.out.println("CPP = P, MusicIsntPlaying");
                deezerHelper.playMusic(url, position); // Resumes music
                messageAdapter.setCurrentlyPlayingPosition(position);
            } else {
                // Switching to a different song or starting a new song
                System.out.println("CPP =/= P");
                deezerHelper.playMusic(url, position); // Plays new music
                messageAdapter.setCurrentlyPlayingPosition(position); // Update UI to reflect new song playing
            }
            System.out.println("Saliendo de CA playMusic");
            System.out.println("Previous cpp: " + currentPlayingPosition);
            System.out.println("Position: " + position);
            System.out.println("Current cpp sent to MA: " + messageAdapter.getCurrentlyPlayingPosition());

            messageAdapter.notifyDataSetChanged();
        } else {
            Log.e("ChatActivity", "DeezerHelper is not initialized");
        }
    }

    @Override
    public void pauseMusic(int position) {
        System.out.println("CA, pauseMusic CPP = " + messageAdapter.getCurrentlyPlayingPosition() + ", P = "+position);
        if (deezerHelper != null) {
            deezerHelper.pauseMusic(position);
            messageAdapter.setCurrentlyPlayingPosition(-1); // No item is playing now
            messageAdapter.notifyDataSetChanged();
        } else {
            Log.e("ChatActivity", "DeezerHelper is not initialized");
        }
    }


    public void stopMusic(int position) {
        if (deezerHelper != null) {
            deezerHelper.stopMusic();
            messageAdapter.setCurrentlyPlayingPosition(-1); // No item is playing now
            messageAdapter.notifyDataSetChanged();
        } else {
            Log.e("ChatActivity", "DeezerHelper is not initialized");
        }
    }



}