package com.example.firefly;

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

public class AlbumActivity extends AppCompatActivity implements
        RecyclerAdapterAlbumActivity.AlbumActivityCommunicator ,
        MusicService.OnTrackCompletionListener,
        MusicService.OnNotificationUpdateListener,
        ServiceConnection {
    ImageView backButton;
    TextView albumName;
    TextView albumArtist;
    ImageView albumArt;
    MusicHelper musicHelper;

    RecyclerView recyclerView;
    RecyclerAdapterAlbumActivity recyclerAdapterAlbumActivity;
    LinearLayoutManager linearLayoutManager;
    int albumIndex;

    ConstraintLayout bottomBar;
    ImageView bottomBarImage;
    ImageButton prev;
    ImageButton play;
    ImageButton next;
    TextView bottomBarTrackName;
    TextView bottomBarArtist;

    boolean mBound;
    MusicService musicService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        albumIndex=getIntent().getIntExtra("albumIndex",-1);
        backButton=findViewById(R.id.albumActivityBackButton);
        albumName=findViewById(R.id.AlbumActivityAlbumName);
        albumArtist=findViewById(R.id.albumActivityArtistName);
        albumArt=findViewById(R.id.albumActivityAlbumArt);
        musicHelper=MusicHelper.getInstance();

        mBound=false;
        recyclerView=findViewById(R.id.albumActivityRecyclerView);



        if(albumIndex!=-1){
            if(musicHelper.getAlbums()!=null && musicHelper.getAlbums().get(albumIndex)!=null){
                AlbumList albumList=musicHelper.getAlbums().get(albumIndex);
                albumName.setText(albumList.getAlbumTracks().getName());
                albumArtist.setText(albumList.getAlbumArtist());
                Long albumID=albumList.getAlbum_ID();
                Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                Uri imageUri = Uri.withAppendedPath(sArtworkUri, String.valueOf(albumID));
                Glide.with(this).load(imageUri).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.music_icon).into(albumArt);
                albumArt.setClipToOutline(true);
            }



            recyclerAdapterAlbumActivity=new RecyclerAdapterAlbumActivity(this,musicHelper.getAlbums().get(albumIndex).getAlbumTracks(),this);
            recyclerView.setAdapter(recyclerAdapterAlbumActivity);
            linearLayoutManager=new LinearLayoutManager(this);
            recyclerView.setLayoutManager(linearLayoutManager);
        }
        backButton.setOnClickListener(view -> {
            finishAct(true);
        });

        bottomBar=findViewById(R.id.albumActivityBottomBar);
        prev=findViewById(R.id.albumActivityPrevButton);
        play=findViewById(R.id.albumActivityPlayButton);
        next=findViewById(R.id.albumActivityNextButton);
        bottomBarImage=findViewById(R.id.albumActivityBottomImage);
        bottomBarTrackName=findViewById(R.id.albumActivityBottomTrackName);
        bottomBarArtist=findViewById(R.id.albumActivityBottomArtistName);


        play.setOnClickListener(view -> {
            onPlayClicked();
        });
        prev.setOnClickListener(view -> {
            onPrevClicked();
        });
        next.setOnClickListener(view -> {
            onNextClicked();
        });
        bottomBar.setOnClickListener(view -> {
            Intent intent=new Intent(AlbumActivity.this,PlayScreenActivity.class);
            startActivity(intent);
        });

    }

    @Override
    public void onBackPressed() {
        finishAct(false);
        super.onBackPressed();
    }

    private void finishAct(boolean b){
        Intent resultIntent = new Intent();
        resultIntent.putExtra("is_updated_album", b);
        setResult(32, resultIntent);
        finishAfterTransition();
    }

    //album track clicked
    @Override
    public void onTrackClicked(int position) {
        if(mBound && musicService!=null){
            if(albumIndex!=-1 && musicHelper!=null){
                TrackList trackList=musicHelper.getAlbums().get(albumIndex).getAlbumTracks();
                if(musicHelper.getActiveTracklist().equals(trackList)==false) {
                    musicHelper.setActiveTracklist(trackList);
                }
                Music music;
                try{
                    music=musicHelper.getAlbums().get(albumIndex).getAlbumTracks().getTracks().get(position);
                }catch (Exception e){
                    music=null;
                }
                if(music!=null){
                    musicService.switchTrack(music);
                    setBottomBar(music);
                    if(musicHelper.isPlaying()){
                        play.setImageResource(R.drawable.pause);
                    }else{
                        play.setImageResource(R.drawable.play);
                    }
                }

            }
        }
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
            musicService.playButtonClicked();
            if(musicHelper.isPlaying()){
                play.setImageResource(R.drawable.pause);
            }else{
                play.setImageResource(R.drawable.play);
            }
        }
    }
    public void onPrevClicked(){
        if(mBound && musicService!=null){
            musicService.prevButtonClicked();
            setBottomBar(musicHelper.getActiveTrack());
            play.setImageResource(R.drawable.pause);
        }
    }
    public void onNextClicked(){
        if(mBound && musicService!=null){
            musicService.nextButtonClicked();
            setBottomBar(musicHelper.getActiveTrack());
            play.setImageResource(R.drawable.pause);
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
    @Override
    protected void onStop() {
        super.onStop();
        if(musicService!=null){
            if(musicService.onNotificationUpdateListener==this){
                musicService.onNotificationUpdateListener=null;
            }
            if(musicService.onTrackCompletionListener==this){
                musicService.onTrackCompletionListener=null;
            }
        }
    }
}