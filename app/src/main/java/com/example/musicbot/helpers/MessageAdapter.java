package com.example.musicbot.helpers;

import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicbot.R;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Message> messages;
    private MusicControlInterface musicControls;
    private int currentlyPlayingPosition = -1; // Default to no item playing


    public MessageAdapter(List<Message> messages, MusicControlInterface musicControls) {
        this.messages = messages;
        this.musicControls = musicControls;
    }

    public void setCurrentlyPlayingPosition(int position) {
        this.currentlyPlayingPosition = position;
        notifyDataSetChanged();  // Notify to refresh the list, reflecting the change
    }
    public int getCurrentlyPlayingPosition() {
        return currentlyPlayingPosition;// Notify to refresh the list, reflecting the change
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == Message.MUSIC_ITEM) {
            View view = inflater.inflate(R.layout.music_player, parent, false);
            return new MusicViewHolder(view, musicControls);  // Pass DeezerHelper instance
        } else {
            View view = inflater.inflate(R.layout.chat_item, parent, false);
            return new MyViewHolder(view);
        }
    }




    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        switch (holder.getItemViewType()) {
            case Message.MUSIC_ITEM:
                if (holder instanceof MusicViewHolder) {
                    MusicViewHolder musicHolder = (MusicViewHolder) holder;
                    DeezerHelper.MusicData musicData = message.getMusicData(); // Assuming you're storing music data like this
                    boolean isPlaying = (currentlyPlayingPosition == position);
                    System.out.println("onBindViewHolder... CPP = " + currentlyPlayingPosition + " P= "+position);

                    musicHolder.bind(musicData, isPlaying, musicControls, position);  // Make sure to pass the current play status
                }
                break;
            case Message.SENT_BY_USER:
                // Handle user-sent messages
                if (holder instanceof MyViewHolder) {
                    MyViewHolder myHolder = (MyViewHolder) holder;
                    myHolder.rightChatView.setVisibility(View.VISIBLE);
                    myHolder.leftChatView.setVisibility(View.GONE);
                    myHolder.rightTextView.setText(message.getContent());
                    myHolder.robotIcon.setVisibility(View.GONE);
                }
                break;
            case Message.SENT_BY_BOT:
                // Handle bot-sent messages
                if (holder instanceof MyViewHolder) {
                    MyViewHolder myHolder = (MyViewHolder) holder;
                    myHolder.leftChatView.setVisibility(View.VISIBLE);
                    myHolder.rightChatView.setVisibility(View.GONE);
                    myHolder.leftTextView.setText(message.getContent());
                    myHolder.robotIcon.setVisibility(View.VISIBLE);
                }
                break;
        }


    }


    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void setMessageList(List<Message> messageList) {
        this.messages = messageList;
        notifyDataSetChanged(); // Notify the adapter that the data has changed
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder {
        LinearLayout leftChatView, rightChatView;
        TextView leftTextView, rightTextView;
        ImageView robotIcon;  // Icon for the bot, visible only on bot messages

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            leftChatView = itemView.findViewById(R.id.left_chat_view);  // View for bot messages
            rightChatView = itemView.findViewById(R.id.right_chat_view);  // View for user messages
            leftTextView = itemView.findViewById(R.id.left_chat_text_view);  // Text view for bot messages
            rightTextView = itemView.findViewById(R.id.right_chat_text_view);  // Text view for user messages
            robotIcon = itemView.findViewById(R.id.robot_image_view);  // Bot icon, only in bot messages
        }
    }


    public class MusicViewHolder extends RecyclerView.ViewHolder {
        ImageView albumCover;
        TextView trackName, artistName;
        ImageButton togglePlayPauseButton; // Single button for toggling play and pause

        public MusicViewHolder(View itemView, MusicControlInterface musicControls) {
            super(itemView);
            albumCover = itemView.findViewById(R.id.album_cover);
            trackName = itemView.findViewById(R.id.track_name);
            artistName = itemView.findViewById(R.id.artist_name);
            togglePlayPauseButton = itemView.findViewById(R.id.play_pause_button); // Assume this ID in your XML
        }

        void bind(DeezerHelper.MusicData musicData, boolean isPlaying, MusicControlInterface musicControls, int position) {
            Picasso.get().load(musicData.getAlbumCoverUrl()).into(albumCover);
            trackName.setText(musicData.getTrackName());
            artistName.setText(musicData.getArtistName());

            // Set the button icon based on whether the music is currently playing
            togglePlayPauseButton.setImageResource(isPlaying ? R.drawable.baseline_pause_24 : R.drawable.baseline_play_arrow_24);

            togglePlayPauseButton.setOnClickListener(v -> {

                if (isPlaying) {
                    System.out.println("togglePPBtn: isPlaying=true, pause music at: "+position);
                    musicControls.pauseMusic(position);  // Call to pause the music
                } else {
                    System.out.println("togglePPBtn: isPlaying=false, playing music at: "+position);
                    musicControls.playMusic(musicData.getMusicUrl(), position);  // Call to play the music
                }
                // The state change will be handled and UI updated by the activity that implements the music controls
            });
        }
    }

}



