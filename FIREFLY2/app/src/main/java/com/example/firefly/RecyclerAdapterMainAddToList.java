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

public class RecyclerAdapterMainAddToList extends RecyclerView.Adapter<RecyclerAdapterMainAddToList.ViewHolder> {
    MusicHelper musicHelper;
    AddToListClickListener addToListClickListener;
    Context context;

    public RecyclerAdapterMainAddToList(Context context,AddToListClickListener addToListClickListener) {
        this.context = context;
        this.addToListClickListener=addToListClickListener;
        musicHelper=MusicHelper.getInstance();
    }

    @NonNull
    @Override
    public RecyclerAdapterMainAddToList.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.add_to_list_row,parent,false);
        RecyclerAdapterMainAddToList.ViewHolder viewHolder=new RecyclerAdapterMainAddToList.ViewHolder(view);
        return  viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapterMainAddToList.ViewHolder holder, int position) {
        TrackList tracks=musicHelper.getCustomList().get(position);
        switch (position){
            case 0:
                holder.listName.setTextColor(ContextCompat.getColor(context, R.color.foreground_1));
                break;
            default:
                holder.listName.setTextColor(ContextCompat.getColor(context, R.color.primary_text_color));
                break;
        }
        holder.listName.setText(tracks.getName());



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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            listImage=itemView.findViewById(R.id.addTolistRowImage);
            listName=itemView.findViewById(R.id.addTolistRowListName);
            itemView.setOnClickListener(view -> {
               if(addToListClickListener!=null){
                   addToListClickListener.onClickMainList(getAdapterPosition());
               }
            });

        }
    }

    public interface AddToListClickListener{
        void onClickMainList(int position);
    }
}
