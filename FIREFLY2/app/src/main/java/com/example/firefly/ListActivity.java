package com.example.firefly;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class ListActivity extends AppCompatActivity implements
        RecyclerAdapterListActivity.ListActivityTrackListOnClickListener,
        RecyclerAdapterListActivity.ListActivityTrackListOptionOnClickListener,
        MusicService.OnTrackCompletionListener,
        MusicService.OnNotificationUpdateListener,
        ServiceConnection
{
    TextView listName;
    TextView listCount;
    ImageView addButton;
    ImageView clearAll;
    ImageView backButton;
    RecyclerView recyclerView;
    MusicHelper musicHelper;
    RecyclerAdapterListActivity recyclerAdapterListActivity;
    LinearLayoutManager linearLayoutManager;
    ActivityResultLauncher<Intent> activityResultLauncher;
    LoadingDialog loadingDialog;

    ConstraintLayout bottomBar;
    ImageView bottomBarImage;
    ImageButton prev;
    ImageButton play;
    ImageButton next;
    TextView bottomBarTrackName;
    TextView bottomBarArtist;


    int listIndex;
    boolean changed;

    boolean mBound;
    MusicService musicService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listIndex=getIntent().getIntExtra("listIndex",-1);
        musicHelper=MusicHelper.getInstance();
        setContentView(R.layout.activity_list);
        mBound=false;
        changed=false;
       listName=findViewById(R.id.listActivityListName);
       listCount=findViewById(R.id.listActivityTrackCount);
       addButton=findViewById(R.id.listActivityAddButton);
       clearAll=findViewById(R.id.listActivityClearAll);
       backButton=findViewById(R.id.listActivityBackButton);

       recyclerView=findViewById(R.id.listActivityRecyclerView);

       if(listIndex!=-1){
           if(musicHelper.getCustomList()!=null){

               if(listIndex<musicHelper.getCustomList().size()){


                   musicHelper.sortTrackList(musicHelper.getCustomList().get(listIndex));

                   listName.setText(musicHelper.getCustomList().get(listIndex).getName());

                   recyclerAdapterListActivity=new RecyclerAdapterListActivity(this,musicHelper.getCustomList().get(listIndex),this,this);
                   recyclerView.setAdapter(recyclerAdapterListActivity);
                   linearLayoutManager=new LinearLayoutManager(this);
                   recyclerView.setLayoutManager(linearLayoutManager);


               }
           }
       }

        bottomBar=findViewById(R.id.listActivityBottomBar);
        prev=findViewById(R.id.listActivityPrevButton);
        play=findViewById(R.id.listActivityPlayButton);
        next=findViewById(R.id.listActivityNextButton);
        bottomBarImage=findViewById(R.id.listActivityBottomImage);
        bottomBarTrackName=findViewById(R.id.listActivityBottomTrackName);
        bottomBarArtist=findViewById(R.id.listActivityBottomArtistName);

        bottomBar.setOnClickListener(view -> {
            Intent intent=new Intent(ListActivity.this,PlayScreenActivity.class);
            startActivity(intent);
        });


        play.setOnClickListener(view -> {
            onPlayClicked();
        });
        prev.setOnClickListener(view -> {
            onPrevClicked();
        });
        next.setOnClickListener(view -> {
            onNextClicked();
        });

       backButton.setOnClickListener(view -> {
           finishActivity(changed);
       });

       addButton.setOnClickListener(view -> {
           if(listIndex!=-1){
               Intent intent=new Intent(ListActivity.this,AddToListTrackSelectActivity.class);
               intent.putExtra("listIndexSelectTracks",listIndex);
               activityResultLauncher.launch(intent);
           }
       });

       clearAll.setOnClickListener(view -> {
           if(listIndex!=-1 && musicHelper!=null){
               try{
                   musicHelper.getCustomList().get(listIndex).getTracks().clear();
                   if(recyclerView!=null && recyclerAdapterListActivity!=null){
                       recyclerAdapterListActivity.notifyDataSetChanged();
                       listCount.setText(musicHelper.getCustomList().get(listIndex).getTracks().size() +" tracks");
                       musicHelper.save(this);

                   }
               }catch (Exception e){
                   e.printStackTrace();
               }
           }
       });
        activityResultLauncher=registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if(result==null || result.getData()==null){
                            return;
                        }
                        boolean b=result.getData().getBooleanExtra("isAddToList",false);
                        //list add screen
                        if(result.getResultCode()==27){
                            if(b){

                                if(recyclerAdapterListActivity!=null && listIndex!=-1 && musicHelper!=null){
                                    AddThread addThread=new AddThread(musicHelper,musicHelper.getCustomList().get(listIndex));
                                    addThread.start();
                                    listCount.setText(musicHelper.getCustomList().get(listIndex).getTracks().size() +" tracks");
                                    musicHelper.save(ListActivity.this);
                                    changed=true;

                                }
                            }
                        }

                    }
                }
        );
        loadingDialog=new LoadingDialog(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishActivity(changed);
    }

    @Override
    protected void onStop() {
        super.onStop();
        changed=false;
        if(musicService!=null){
            if(musicService.onNotificationUpdateListener==this){
                musicService.onNotificationUpdateListener=null;
            }
            if(musicService.onTrackCompletionListener==this){
                musicService.onTrackCompletionListener=null;
            }
        }
    }



    class AddThread extends Thread{
        MusicHelper musicHelper;
        TrackList trackList;
        AddThread(MusicHelper musicHelper,TrackList trackList){
            this.musicHelper=musicHelper;
            this.trackList=trackList;
        }

        @Override
        public void run() {
            super.run();
            runOnUiThread(() -> loadingDialog.showDialog("loading"));
            if(musicHelper!=null && trackList!=null){
                musicHelper.sortTrackList(trackList);
            }
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(recyclerAdapterListActivity!=null && listIndex!=-1 && musicHelper!=null){
                        musicHelper.sortTrackList(musicHelper.getCustomList().get(listIndex));
                        recyclerAdapterListActivity.notifyDataSetChanged();
                    }
                }
            });
            runOnUiThread(() -> loadingDialog.dismissDialog());
        }
    }

    private void finishActivity(boolean b){
        Intent resultIntent = new Intent();
        resultIntent.putExtra("is_updated", b);
        setResult(17, resultIntent);
        finishAfterTransition();
    }


    //main trackList Clicked
    @Override
    public void onListClicked(int position) {
        Toast.makeText(this, "list clicked", Toast.LENGTH_SHORT).show();

        if(mBound && musicService!=null){

            if(musicHelper!=null && musicHelper.getCustomList()!=null && musicHelper.getCustomList().get(listIndex)!=null && listIndex!=-1 ){
                if(musicHelper.getActiveTracklist().equals(musicHelper.getCustomList().get(listIndex))==false){
                    musicHelper.setActiveTracklist(musicHelper.getCustomList().get(listIndex));
                }
                if(position<musicHelper.getCustomList().get(listIndex).getTracks().size()){
                    Music music=musicHelper.getCustomList().get(listIndex).getTracks().get(position);

                    if(music!=null){
                        updateMainTrackList(musicHelper.findActiveTrackListIndex());
                        musicService.switchTrack(music);
                        setBottomBar(music);
                        if(musicHelper!=null && play!=null){
                            if(musicHelper.isPlaying()){
                                play.setImageResource(R.drawable.pause);
                            }else{
                                play.setImageResource(R.drawable.play);
                            }
                        }
                        updateMainTrackList(musicHelper.findActiveTrackListIndex());
                    }
                }

            }
        }
    }


    private void updateMainTrackList(int position){
        if(position==-1){
            return;
        }
        if(recyclerView!=null && recyclerAdapterListActivity!=null){
            recyclerAdapterListActivity.notifyItemChanged(position);
        }
    }

    //option clicked
    @Override
    public void onRemoveClicked(int position) {
        Toast.makeText(this, "remove", Toast.LENGTH_SHORT).show();
        boolean isRemoved=musicHelper.removeFromList(listIndex,position);
        if(isRemoved){
            if(recyclerView!=null && recyclerAdapterListActivity!=null){
                recyclerAdapterListActivity.notifyItemRemoved(position);
                musicHelper.save(this);
                if(this.listCount!=null){
                    this.listCount.setText(musicHelper.getCustomList().get(listIndex).getTracks().size() +" tracks");
                }
                changed=true;
            }
        }
    }

    @Override
    public void onShareClicked(int position) {
        Music music;
        try{
            music=musicHelper.getCustomList().get(listIndex).getTracks().get(position);
        }catch (Exception e){
            return;
        }
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("audio/*");
        share.putExtra(Intent.EXTRA_STREAM, music.getUri());
        startActivity(Intent.createChooser(share, "Share Sound File"));
    }

    @Override
    public void onDetailClicked(int position) {

    }


    public void setBottomBar(Music music){
        if(music!=null){
            bottomBarTrackName.setText(music.getTrackName());
            bottomBarArtist.setText(music.getArtistName());
            Long albumID=music.getAlbum_id();
            Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
            Uri imageUri = Uri.withAppendedPath(sArtworkUri, String.valueOf(albumID));
            Glide.with(this).load(imageUri).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.music_icon).into(bottomBarImage);
            bottomBarImage.setClipToOutline(true);
        }
    }
    //service methods
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        MusicService.MyBinder myBinder= (MusicService.MyBinder) iBinder;
        musicService=myBinder.getService();
        musicService.setOnTrackCompletionListener(this);
        musicService.setOnNotificationUpdateListener(this);
        setBottomBar(musicHelper.getActiveTrack());
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        musicService=null;

    }


    public void onPlayClicked(){
        if(mBound && musicService!=null){
            updateMainTrackList(musicHelper.findActiveTrackListIndex());
            musicService.playButtonClicked();
            if(musicHelper.isPlaying()){
                play.setImageResource(R.drawable.pause);
            }else{
                play.setImageResource(R.drawable.play);
            }
            updateMainTrackList(musicHelper.findActiveTrackListIndex());
        }
    }
    public void onPrevClicked(){
        if(mBound && musicService!=null){
            updateMainTrackList(musicHelper.findActiveTrackListIndex());
            musicService.prevButtonClicked();
            setBottomBar(musicHelper.getActiveTrack());
            play.setImageResource(R.drawable.pause);
            updateMainTrackList(musicHelper.findActiveTrackListIndex());
        }
    }
    public void onNextClicked(){
        if(mBound && musicService!=null){
            updateMainTrackList(musicHelper.findActiveTrackListIndex());
            musicService.nextButtonClicked();
            setBottomBar(musicHelper.getActiveTrack());
            play.setImageResource(R.drawable.pause);
            updateMainTrackList(musicHelper.findActiveTrackListIndex());
        }
    }

    @Override
    public void onTrackCompleted() {
        //onNextClicked();
        setBottomBar(musicHelper.getActiveTrack());
    }


    @Override
    protected void onPause() {
        unbindService(this);
        mBound=false;
        super.onPause();

    }

    @Override
    protected void onResume() {
        Intent intent=new Intent(this,MusicService.class);
        mBound=true;
        bindService(intent,this,BIND_AUTO_CREATE);
        super.onResume();
        if(musicHelper!=null && play!=null){
            if(musicHelper.isPlaying()){
                play.setImageResource(R.drawable.pause);
            }else{
                play.setImageResource(R.drawable.play);
            }
        }
        if(listCount!=null && musicHelper!=null){
            try {
                listCount.setText(musicHelper.getCustomList().get(listIndex).getTracks().size() +" tracks");
                if(recyclerView!=null && recyclerAdapterListActivity!=null){
                    recyclerAdapterListActivity.notifyDataSetChanged();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    @Override
    public boolean onNotificationPrevClicked() {
        if(mBound && musicService!=null){
            onPrevClicked();
            return true;
        }
        return false;
    }

    @Override
    public boolean onNotificationPlayClicked() {
        if(mBound && musicService!=null){
            onPlayClicked();
            return true;
        }
        return false;
    }

    @Override
    public boolean onNotificationNextClicked() {
        if(mBound && musicService!=null){
            onNextClicked();
            return true;
        }
        return false;
    }


}