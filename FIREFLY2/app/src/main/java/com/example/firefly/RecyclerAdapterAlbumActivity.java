package com.example.firefly;

import android.content.Context;
import android.net.Uri;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class RecyclerAdapterAlbumActivity extends RecyclerView.Adapter<RecyclerAdapterAlbumActivity.ViewHolder> {

    Context context;
    MusicHelper musicHelper;
    AlbumActivityCommunicator albumActivityCommunicator;
    TrackList trackList;


    public RecyclerAdapterAlbumActivity(Context context,TrackList trackList, AlbumActivityCommunicator albumActivityCommunicator) {
        this.context=context;
        musicHelper=MusicHelper.getInstance();
        this.trackList=trackList;
        this.albumActivityCommunicator=albumActivityCommunicator;
    }

    @NonNull
    @Override
    public RecyclerAdapterAlbumActivity.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.album_activity_track_row,parent,false);
        ViewHolder viewHolder=new ViewHolder(view);
        return  viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapterAlbumActivity.ViewHolder holder, int position) {
        holder.trackName.setText(trackList.getTracks().get(position).getTrackName());
        holder.duration.setText(trackList.getTracks().get(position).getTrackDuration());


    }


    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return trackList.getTracks().size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView trackName;
        TextView duration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            trackName=itemView.findViewById(R.id.albumActivityTrackRowTrackName);
            duration=itemView.findViewById(R.id.albumActivityTrackRowDuration);
            itemView.setOnClickListener(view -> {
                if(albumActivityCommunicator!=null){
                    albumActivityCommunicator.onTrackClicked(getAdapterPosition());
                }
            });

        }
    }

    public interface AlbumActivityCommunicator{
        void onTrackClicked(int position);
    }

}
