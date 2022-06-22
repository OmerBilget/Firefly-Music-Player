package com.example.firefly;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class RecyclerAdapterMainAlbum extends RecyclerView.Adapter<RecyclerAdapterMainAlbum.Viewholder> {
    Context context;
    MusicHelper musicHelper;
    MainAlbumOnClickListener mainAlbumOnClickListener;
    public RecyclerAdapterMainAlbum(Context context,MainAlbumOnClickListener mainAlbumOnClickListener) {
        this.context = context;
        musicHelper=MusicHelper.getInstance();
        this.mainAlbumOnClickListener=mainAlbumOnClickListener;
    }

    @NonNull
    @Override
    public RecyclerAdapterMainAlbum.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.album_layout,parent,false);
        RecyclerAdapterMainAlbum.Viewholder viewHolder=new RecyclerAdapterMainAlbum.Viewholder(view);
        return  viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapterMainAlbum.Viewholder holder, int position) {
        holder.albumName.setText(musicHelper.getAlbums().get(position).getAlbumTracks().getName());
        holder.albumDetails.setText(musicHelper.getAlbums().get(position).getAlbumArtist()+"/"+ musicHelper.getAlbums().get(position).getAlbumTracks().getTracks().size() +" tracks");
        long albumID=musicHelper.getAlbums().get(position).getAlbum_ID();
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri imageUri = Uri.withAppendedPath(sArtworkUri, String.valueOf(albumID));
        Glide.with(context).load(imageUri).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.music_icon).into(holder.albumArt);
        holder.albumArt.setClipToOutline(true);

    }

    @Override
    public void onViewRecycled(@NonNull Viewholder holder) {
        super.onViewRecycled(holder);
        Glide.with(context).clear(holder.itemView);
    }

    @Override
    public int getItemCount() {
        return musicHelper.getAlbums().size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        ImageView albumArt;
        TextView albumName;
        TextView albumDetails;
        public Viewholder(@NonNull View itemView) {
            super(itemView);
            albumArt=itemView.findViewById(R.id.albumLayoutImage);
            albumName=itemView.findViewById(R.id.AlbumLayoutAlbumName);
            albumDetails=itemView.findViewById(R.id.albumLayoutAlbumDetails);
            itemView.setOnClickListener(view -> {
                if(mainAlbumOnClickListener!=null){
                    mainAlbumOnClickListener.onMainAlbumClicked(getAdapterPosition());
                }
            });
        }

    }

    public interface MainAlbumOnClickListener{
        void onMainAlbumClicked(int position);
    }
}
