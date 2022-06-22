package com.example.firefly;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RecyclerAdapterAddToListTrackSelect extends RecyclerView.Adapter<RecyclerAdapterAddToListTrackSelect.ViewHolder> implements Filterable {

    Context context;
    MusicHelper musicHelper;
    AddToListCommunicator addToListCommunicator;
    TrackList searchList;
    List<Boolean> checkedList;
    public RecyclerAdapterAddToListTrackSelect(Context context ,TrackList trackList,AddToListCommunicator addToListCommunicator) {
        this.context=context;
        musicHelper=MusicHelper.getInstance();
        this.searchList=new TrackList("searchList");
        checkedList=new ArrayList<>();
        this.addToListCommunicator=addToListCommunicator;
        fillSearchList();
        notifyDataSetChanged();
    }

    private Filter filter=new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<Music> tmp=new ArrayList<>();
            if(charSequence==null || charSequence.length()==0){
               tmp.addAll(musicHelper.getMainList().getTracks());
            }else{
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
            fillCheckList(false);
            if(addToListCommunicator!=null){
                addToListCommunicator.onSizeChanged(searchList.getTracks().size());
            }
            notifyDataSetChanged();
        }
    };



    @NonNull
    @Override
    public RecyclerAdapterAddToListTrackSelect.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.add_to_list_track_select_track_row,parent,false);
        ViewHolder viewHolder=new ViewHolder(view);
        return  viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapterAddToListTrackSelect.ViewHolder holder, int position) {
        holder.trackName.setText(searchList.getTracks().get(position).getTrackName());
        holder.artistName.setText(searchList.getTracks().get(position).getArtistName());
        boolean isChecked=false;
        if(checkedList.get(position)!=null){
            isChecked=checkedList.get(position);
        }
        if(holder.checkBox.isChecked()!=isChecked){
            holder.checkBox.toggle();
        }
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
        CheckBox checkBox;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image=itemView.findViewById(R.id.mainTrackRowAlbumArt);
            trackName=itemView.findViewById(R.id.mainTrackRowTrackName);
            artistName=itemView.findViewById(R.id.mainTrackRowArtistName);
            checkBox=itemView.findViewById(R.id.rowCheckBox);

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(checkedList.get(getAdapterPosition())!=null){
                        checkedList.set(getAdapterPosition(),b);
                        if(addToListCommunicator!=null){

                            addToListCommunicator.onCheckboxClicked(getSelectedSize());
                        }
                    }
                }
            });
            itemView.setOnClickListener(view ->
                    checkBox.toggle()
            );


        }
    }


    public void fillSearchList(){
        if(musicHelper==null || musicHelper.getMainList()==null || musicHelper.getMainList().getTracks()==null){
            return;
        }
        searchList.getTracks().clear();
        searchList.getTracks().addAll(musicHelper.getMainList().getTracks());
        fillCheckList(false);
    }


    public void fillCheckList(boolean b){
        checkedList.clear();
        if(searchList==null){
            return;
        }
        int size=searchList.getTracks().size();
        for(int i=0;i<size;i++){
            checkedList.add(b);
        }
    }

    public interface AddToListCommunicator{
        void onCheckboxClicked(int selected);
        void onSizeChanged(int size);
    }

    public int getSelectedSize(){
        int size=0;
        int listSize=checkedList.size();
        for(int i=0;i<listSize;i++){
            if(checkedList.get(i)){
                size++;
            }
        }
        return size;
    }

}
