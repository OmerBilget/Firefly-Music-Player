package com.example.firefly;

import android.content.Context;
import android.net.Uri;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class RecyclerAdapterSearchMain extends RecyclerView.Adapter<RecyclerAdapterSearchMain.ViewHolder> implements Filterable {

    Context context;
    MusicHelper musicHelper;
    SearchListOnClickListener searchListOnClickListener;
    TrackList searchList;

    public RecyclerAdapterSearchMain(Context context,SearchListOnClickListener searchListOnClickListener) {
        this.context=context;
        musicHelper=MusicHelper.getInstance();
        this.searchListOnClickListener=searchListOnClickListener;
        searchList=new TrackList("searchList");
    }

    private Filter filter=new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<Music> tmp=new ArrayList<>();
            if(charSequence==null || charSequence.length()==0){
                tmp.clear();
            }else{
                tmp.clear();
                String filterPattern=charSequence.toString().toLowerCase().trim();
                if(musicHelper!=null && musicHelper.getMainList()!=null && musicHelper.getMainList().getTracks()!=null){
                    int size=musicHelper.getMainList().getTracks().size();
                    for(int i=0;i<size;i++){
                        Music music=musicHelper.getMainList().getTracks().get(i);
                        if(music.getTrackName().toLowerCase().contains(filterPattern) || music.getArtistName().toLowerCase().contains(filterPattern)){
                            tmp.add(music);
                        }
                    }
                }

            }
            FilterResults results=new FilterResults();
            results.values=tmp;
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            searchList.getTracks().clear();
            searchList.getTracks().addAll((Collection<? extends Music>) filterResults.values);
            notifyDataSetChanged();
        }
    };



    @NonNull
    @Override
    public RecyclerAdapterSearchMain.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.main_track_row,parent,false);
        ViewHolder viewHolder=new ViewHolder(view);
        return  viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapterSearchMain.ViewHolder holder, int position) {
        holder.trackName.setText(searchList.getTracks().get(position).getTrackName());
        holder.artistName.setText(searchList.getTracks().get(position).getArtistName());
        Long albumID=searchList.getTracks().get(position).getAlbum_id();
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
        return searchList.getTracks().size();
    }

    @Override
    public Filter getFilter() {
        return filter;
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
            //moreOptions.setVisibility(View.INVISIBLE);
            itemView.setOnClickListener(view -> {
                if(searchListOnClickListener!=null){
                    TrackList searched=new TrackList("searched");
                    searched.getTracks().addAll(searchList.getTracks());
                    searchListOnClickListener.onSearchListClicked(getAdapterPosition(),searched);
                }
            });


        }
    }

    public interface SearchListOnClickListener{
        void onSearchListClicked(int position,TrackList trackList);
    }

    private void fillSearchList(){
        if(musicHelper==null || musicHelper.getMainList()==null || musicHelper.getMainList().getTracks()==null){
            return;
        }
        searchList.getTracks().clear();
        searchList.getTracks().addAll(musicHelper.getMainList().getTracks());
    }

}
