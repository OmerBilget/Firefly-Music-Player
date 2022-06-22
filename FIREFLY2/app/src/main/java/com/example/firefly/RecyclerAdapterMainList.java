package com.example.firefly;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerAdapterMainList extends RecyclerView.Adapter<RecyclerAdapterMainList.ViewHolder> {
    MusicHelper musicHelper;
    MainListClickListener mainListClickListener;
    Context context;

    public RecyclerAdapterMainList(Context context,MainListClickListener mainListClickListener) {
        this.context = context;
        this.mainListClickListener=mainListClickListener;
        musicHelper=MusicHelper.getInstance();
    }

    @NonNull
    @Override
    public RecyclerAdapterMainList.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row,parent,false);
        RecyclerAdapterMainList.ViewHolder viewHolder=new RecyclerAdapterMainList.ViewHolder(view);
        return  viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapterMainList.ViewHolder holder, int position) {
        TrackList tracks=musicHelper.getCustomList().get(position);
        switch (position){
            case 0:
                holder.listName.setTextColor(ContextCompat.getColor(context, R.color.foreground_1));
                holder.listCount.setTextColor(ContextCompat.getColor(context, R.color.foreground_1));
                break;
            default:
                holder.listName.setTextColor(ContextCompat.getColor(context, R.color.primary_text_color));
                holder.listCount.setTextColor(ContextCompat.getColor(context, R.color.secondary_text_color));
                break;
        }
        holder.listName.setText(tracks.getName());
        holder.listCount.setText(tracks.getTracks().size() +" tracks");


    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return musicHelper.getCustomList().size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView listImage;
        TextView listName;
        TextView listCount;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            listImage=itemView.findViewById(R.id.listRowImage);
            listName=itemView.findViewById(R.id.listRowListName);
            listCount=itemView.findViewById(R.id.listRowTrackCount);
            itemView.setOnClickListener(view -> {
                if(mainListClickListener!=null){
                    mainListClickListener.onClickMainList(getAdapterPosition());
                }
            });
            itemView.setOnLongClickListener(view -> {
                if(mainListClickListener!=null && getAdapterPosition()!=0){
                    mainListClickListener.onLongClickMainList(getAdapterPosition());
                }
                return true;
            });
        }
    }

    public interface MainListClickListener{
        void onClickMainList(int position);
        void onLongClickMainList(int position);
    }
}
