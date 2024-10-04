package com.example.musicbot.helpers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicbot.R;
import com.example.musicbot.database.MusicItem;
import com.squareup.picasso.Picasso;

import java.util.List;


public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    private List<MusicItem> songList;
    private OnItemClickListener onItemClickListener;

    public SongAdapter(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setSongList(List<MusicItem> songList) {
        this.songList = songList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        MusicItem song = songList.get(position);
        holder.textViewTitle.setText(song.getTitle());
        holder.textViewArtist.setText(song.getArtist());
        Picasso.get()
                .load(song.getAlbumCoverUrl())
                .into(holder.imageViewAlbum);

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(song);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (songList != null) ? songList.size() : 0;
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle, textViewArtist;
        ImageView imageViewAlbum;

        SongViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewArtist = itemView.findViewById(R.id.textViewArtist);
            imageViewAlbum = itemView.findViewById(R.id.imageViewAlbum);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(MusicItem song);
    }
}
