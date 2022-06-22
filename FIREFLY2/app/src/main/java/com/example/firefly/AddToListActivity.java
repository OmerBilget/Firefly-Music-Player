package com.example.firefly;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class AddToListActivity extends AppCompatActivity implements RecyclerAdapterMainAddToList.AddToListClickListener {
    ImageView backButton;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    RecyclerAdapterMainAddToList recyclerAdapterMainAddToList;
    MusicHelper musicHelper;
    int musicIndex;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        musicIndex=getIntent().getIntExtra("music_position",-1);
        setContentView(R.layout.activity_add_to_list);
        backButton=findViewById(R.id.addToListBackButton);
        recyclerView=findViewById(R.id.AddToListRecyclerView);
        recyclerAdapterMainAddToList=new RecyclerAdapterMainAddToList(this,this);
        recyclerView.setAdapter(recyclerAdapterMainAddToList);
        linearLayoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        musicHelper=MusicHelper.getInstance();
        backButton.setOnClickListener(view -> {
            finishActivity(false);
        });
    }

    @Override
    public void onClickMainList(int position) {
        boolean isUpdated=false;
        if(musicIndex!=-1){
            TrackList trackList=musicHelper.getCustomList().get(position);
            Music music=musicHelper.getMainList().getTracks().get(musicIndex);
            if(trackList!=null && music!=null){
                if(!musicHelper.isContainMusic(trackList,music)){
                    musicHelper.getCustomList().get(position).getTracks().add(music);
                    isUpdated=true;
                }
            }


        }
        finishActivity(isUpdated);

    }

    private void finishActivity(boolean b){
        Intent resultIntent = new Intent();
        resultIntent.putExtra("is_updated",b);
        setResult(12, resultIntent);
        finish();
    }
}