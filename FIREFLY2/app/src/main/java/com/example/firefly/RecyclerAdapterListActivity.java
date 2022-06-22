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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class RecyclerAdapterListActivity extends RecyclerView.Adapter<RecyclerAdapterListActivity.ViewHolder> {

    Context context;
    MusicHelper musicHelper;
    ListActivityTrackListOnClickListener listActivityTrackListOnClickListener;
    ListActivityTrackListOptionOnClickListener listActivityTrackListOptionOnClickListener;
    TrackList trackList;
    public RecyclerAdapterListActivity(Context context,TrackList trackList,ListActivityTrackListOnClickListener listActivityTrackListOnClickListener, ListActivityTrackListOptionOnClickListener listActivityTrackListOptionOnClickListener) {
        this.context=context;
        musicHelper=MusicHelper.getInstance();
        this.trackList=trackList;
        this.listActivityTrackListOnClickListener=listActivityTrackListOnClickListener;
        this.listActivityTrackListOptionOnClickListener=listActivityTrackListOptionOnClickListener;

    }

    @NonNull
    @Override
    public RecyclerAdapterListActivity.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.main_track_row,parent,false);
        ViewHolder viewHolder=new ViewHolder(view);
        return  viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapterListActivity.ViewHolder holder, int position) {
        holder.trackName.setText(trackList.getTracks().get(position).getTrackName());
        holder.artistName.setText(trackList.getTracks().get(position).getArtistName());
        if(context!=null){
            if(trackList.getTracks().get(position).equals(musicHelper.getActiveTrack())){
                holder.trackName.setTextColor(ContextCompat.getColor(context,R.color.foreground_1));
                holder.artistName.setTextColor(ContextCompat.getColor(context,R.color.foreground_2));
            }else{
                holder.trackName.setTextColor(ContextCompat.getColor(context,R.color.primary_text_color));
                holder.artistName.setTextColor(ContextCompat.getColor(context,R.color.secondary_text_color));
            }
        }


        Long albumID=trackList.getTracks().get(position).getAlbum_id();
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri imageUri = Uri.withAppendedPath(sArtworkUri, String.valueOf(albumID));
        Glide.with(holder.itemView).load(imageUri).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.music_icon).into(holder.image);

        holder.image.setClipToOutline(true);


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
        ImageView image;
        TextView trackName;
        TextView artistName;
        ImageView moreOptions;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image=itemView.findViewById(R.id.mainTrackRowAlbumArt);
            trackName=itemView.findViewById(R.id.mainTrackRowTrackName);
            artistName=itemView.findViewById(R.id.mainTrackRowArtistName);
            moreOptions=itemView.findViewById(R.id.mainTrackMoreImage);
            itemView.setOnClickListener(view -> {
                if(listActivityTrackListOnClickListener!=null){
                    listActivityTrackListOnClickListener.onListClicked(getAdapterPosition());
                }
            });

            moreOptions.setOnClickListener(view -> {
                ContextThemeWrapper contextThemeWrapper=new ContextThemeWrapper(context,R.style.BasePopupMenu);
                PopupMenu popupMenu=new PopupMenu(contextThemeWrapper,view);
                popupMenu.getMenuInflater().inflate(R.menu.list_activity_options_menu,popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(menuItem -> {
                   if(listActivityTrackListOptionOnClickListener!=null){
                       switch (menuItem.getItemId()){
                           case R.id.listActivityOptionsMenuRemove:
                               if(listActivityTrackListOptionOnClickListener!=null){
                                   listActivityTrackListOptionOnClickListener.onRemoveClicked(getAdapterPosition());
                               }
                               break;
                           case R.id.listActivityOptionsMenuShare:
                               if(listActivityTrackListOptionOnClickListener!=null){
                                   listActivityTrackListOptionOnClickListener.onShareClicked(getAdapterPosition());
                               }
                               break;
                           case R.id.listActivityTrackOptionsMenuDetails:
                               if(listActivityTrackListOptionOnClickListener!=null){
                                   listActivityTrackListOptionOnClickListener.onDetailClicked(getAdapterPosition());
                               }
                               break;
                       }
                   }
                    return false;
                });
            });


        }
    }

    public interface ListActivityTrackListOnClickListener{
        void onListClicked(int position);
    }

    public interface ListActivityTrackListOptionOnClickListener{
        void onRemoveClicked(int position);
        void onShareClicked(int position);
        void onDetailClicked(int position);
    }

}
