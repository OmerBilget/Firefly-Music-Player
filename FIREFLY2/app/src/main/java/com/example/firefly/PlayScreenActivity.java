package com.example.firefly;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.slider.Slider;



public class PlayScreenActivity extends AppCompatActivity implements ServiceConnection ,
        MusicService.OnTrackCompletionListener ,
        MusicService.OnNotificationUpdateListener
{
    ImageButton play;
    ImageButton prev;
    ImageButton next;
    ImageButton favorites;
    ImageButton shuffle;
    ImageButton repeat;
    ImageButton addButton;
    ImageButton shareButton;
    ImageButton volumeControl;
    ImageView backButton;
    ImageView albumArt;
    TextView artistName;
    TextView trackName;
    TextView currentTime;
    TextView totalTime;
    Slider slider;
    View circle1;
    View circle2;

    MusicHelper musicHelper;
    MusicService musicService;
    boolean mBound;

    Runnable runnable;
    Handler handler;
    Handler circle1Handler;
    boolean sliderMoving;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_screen);
        mBound=false;
        sliderMoving=false;
        play=findViewById(R.id.playerScreenPlayButton);
        prev=findViewById(R.id.playerScreenPrevButton);
        next=findViewById(R.id.playerScreenNextButton);
        addButton=findViewById(R.id.playerScreenAddButton);
        favorites=findViewById(R.id.playerScreenFavorites);
        repeat=findViewById(R.id.playerScreenRepeatButton);
        shuffle=findViewById(R.id.playerScreenShuffleButton);
        backButton=findViewById(R.id.playerScreenBackButton);
        shareButton=findViewById(R.id.playerScreenShareButton);
        volumeControl=findViewById(R.id.playerScreenVolumeButton);



        albumArt=findViewById(R.id.playerScreenAlbumArt);
        artistName=findViewById(R.id.playerScreenArtistName);
        trackName=findViewById(R.id.playerScreenTrackName);
        currentTime=findViewById(R.id.playerScreenCurrentTimeText);
        totalTime=findViewById(R.id.playerScreenTotalTimeText);
        slider=findViewById(R.id.slider);
        circle1=findViewById(R.id.playerScreenCircle1);
        circle2=findViewById(R.id.playerScreenCircle2);

        musicHelper=MusicHelper.getInstance();


        backButton.setOnClickListener(view -> {
            finish_Act(true);
        });


        favorites.setOnClickListener(view -> {
            onFavoritesClicked(musicHelper.getActiveTrack());
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

        repeat.setOnClickListener(view -> {
            repeatClicked();
        });
        shuffle.setOnClickListener(view -> {
            shuffleClicked();
        });

        addButton.setOnClickListener(view -> {
            Intent intent=new Intent(PlayScreenActivity.this,AddToListActivity.class);
            intent.putExtra("music_position",musicHelper.getMainList().getTracks().indexOf(musicHelper.getActiveTrack()));
            startActivity(intent);
        });

        shareButton.setOnClickListener(view -> {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("audio/*");
            share.putExtra(Intent.EXTRA_STREAM, musicHelper.getActiveTrack().getUri());
            startActivity(Intent.createChooser(share, "Share Sound File"));
        });

        volumeControl.setOnClickListener(view -> {
            AudioManager audioManager=(AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI);
        });


        handler=new Handler();
        circle1Handler=new Handler();

        slider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {

            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                if(mBound && musicService!=null){
                    float percent=slider.getValue()/100;
                    int duration_int=musicService.getDuration();
                    float duration=(float)duration_int;
                    int seek=(int)(duration*percent);
                    if(seek>duration_int){
                        seek=duration_int;
                    }
                    setCurrentTime(seek);
                    musicService.seekTo(seek);
                }
                sliderMoving=false;
            }
        });
        slider.addOnChangeListener(new Slider.OnChangeListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                if(fromUser){
                    if(mBound && musicService!=null){
                        float percent=value/100;
                        float duration=(float)musicService.getDuration();
                        int seek=(int)(duration*percent);
                        setCurrentTime(seek);
                    }
                    if(sliderMoving==false){
                        sliderMoving=true;
                    }
                }
            }
        });

        setRepeat();
        setShuffle();
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

    private void finish_Act(boolean b){
        finishAfterTransition();
    }

    @Override
    public void onBackPressed() {
        finish_Act(true);
        super.onBackPressed();

    }

    private void load_music(Music music){
        if(music==null){
            return;
        }
        if(trackName!=null){
            trackName.setText(music.getTrackName());
        }
        if(artistName!=null){
            artistName.setText(music.getArtistName());
        }
        if(albumArt!=null){
            Long albumID=music.getAlbum_id();
            Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
            Uri imageUri = Uri.withAppendedPath(sArtworkUri, String.valueOf(albumID));
            Glide.with(this).load(imageUri).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.music_icon).into(albumArt);
            albumArt.setClipToOutline(true);
        }
        setTotalTime(music.getTrackDuration());
        setCurrentTime(0);
        setFavorites(music);

    }

    private void setTotalTime(int time){
        if(totalTime!=null){
            totalTime.setText(musicHelper.getTimeFormatted(time));
        }
    }
    private void setTotalTime(String time){
        if(totalTime!=null){
            totalTime.setText(time);
        }
    }
    private void setCurrentTime(int time){
        if(currentTime!=null){
            currentTime.setText(musicHelper.getTimeFormatted(time));
        }
    }
    private void setFavorites(Music music){
        if(musicHelper!=null && music!=null){
            if(musicHelper.isInFavorites(music)){
                favorites.setImageResource(R.drawable.favorites_full);
            }else{
                favorites.setImageResource(R.drawable.favorites_border);
            }
            musicHelper.save(this);
        }
    }

    private void onFavoritesClicked(Music music){
        if(musicHelper!=null && music!=null){
            if(musicHelper.isInFavorites(music)){
                musicHelper.removeFromTheFavorites(music);
            }else{
                musicHelper.addToTheFavorites(music);
            }
            setFavorites(music);
        }
    }

    private void setRepeat(){
        if(musicHelper!=null){
            if(musicHelper.isRepeat()){
                repeat.setImageResource(R.drawable.repeat);
            }else{
                repeat.setImageResource(R.drawable.arrow);
            }
        }
    }
    private void setShuffle(){
        if(musicHelper!=null){
            if(musicHelper.isShuffle()){
                ImageViewCompat.setImageTintList(shuffle, ColorStateList.valueOf(ContextCompat.getColor(this,R.color.foreground_2)));
            }else{
                ImageViewCompat.setImageTintList(shuffle, ColorStateList.valueOf(ContextCompat.getColor(this,R.color.primary_text_color)));
            }
        }
    }
    private void shuffleClicked(){
        if(musicHelper!=null){
            musicHelper.setShuffle(!musicHelper.isShuffle());
            setShuffle();
            musicHelper.saveShuffle(this);
        }
    }
    private void repeatClicked(){
        if(musicHelper!=null){
            musicHelper.setRepeat(!musicHelper.isRepeat());
            setRepeat();
            musicHelper.saveRepeat(this);
        }
    }

    Runnable runnableCircle2=new Runnable() {
        @Override
        public void run() {
            circle2.animate().scaleX(2f).scaleY(2f).alpha(0f).setDuration(3000).withEndAction(new Runnable() {
                @Override
                public void run() {
                    circle2.setScaleX(1f);
                    circle2.setScaleY(1f);
                    circle2.setAlpha(1f);
                }
            });
            circle1Handler.postDelayed(runnableCircle2,3050);
        }
    };






    //service methods
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        MusicService.MyBinder myBinder= (MusicService.MyBinder) iBinder;
        musicService=myBinder.getService();
        musicService.setOnTrackCompletionListener(this);
        musicService.setOnNotificationUpdateListener(this);
        if(musicService.isPlaying()){
            play.setBackgroundResource(R.drawable.pause);
            circle1AnimateShrink();
            runnableCircle2.run();
        }else{
            play.setBackgroundResource(R.drawable.play);
        }
        updateSlider();
    }

    private void circle1AnimateShrink(){
        circle1.animate().scaleX(0.6f).scaleY(0.6f).setDuration(400);
    }
    private void circle1AnimateExpand(){
        circle1.animate().scaleX(1f).scaleY(1f).setDuration(400);
    }
    private float getSliderValueBounded(float value){
        if(value<0){
            return 0;
        }else if(value>100){
            return 100;
        }else{
            return value;
        }
    }
    private void updateSlider(){
        if(musicService==null || mBound==false){
            return;
        }
        if(sliderMoving==false){
            float percent=((float)musicService.getCurrentPosition()/(float)musicService.getDuration())*100;
            slider.setValue(getSliderValueBounded(percent));
            setCurrentTime(musicService.getCurrentPosition());
        }
        runnable= () -> updateSlider();
        handler.postDelayed(runnable,100);
    }
    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        musicService=null;

    }


    public void onPlayClicked(){
        if(mBound && musicService!=null){
            musicService.playButtonClicked();
            musicService.onCompleted();
            if(musicHelper.isPlaying()){
                play.setBackgroundResource(R.drawable.pause);
                circle1AnimateShrink();
                runnableCircle2.run();
            }else{
                play.setBackgroundResource(R.drawable.play);
                circle1AnimateExpand();
                circle1Handler.removeCallbacks(runnableCircle2);
            }
        }
    }
    public void onPrevClicked(){
        if(mBound && musicService!=null){
            musicService.prevButtonClicked();
            musicService.onCompleted();
            load_music(musicHelper.getActiveTrack());
            if(musicHelper.isPlaying()){
                play.setBackgroundResource(R.drawable.pause);
                circle1AnimateShrink();
                runnableCircle2.run();
            }else{
                play.setBackgroundResource(R.drawable.play);
                circle1AnimateExpand();
                circle1Handler.removeCallbacks(runnableCircle2);
            }
        }
    }
    public void onNextClicked(){
        if(mBound && musicService!=null){
            musicService.nextButtonClicked();
            musicService.onCompleted();
            load_music(musicHelper.getActiveTrack());
            if(musicHelper.isPlaying()){
                play.setBackgroundResource(R.drawable.pause);
                circle1AnimateShrink();
                runnableCircle2.run();
            }else{
                play.setBackgroundResource(R.drawable.play);
                circle1AnimateExpand();
                circle1Handler.removeCallbacks(runnableCircle2);
            }
        }
    }

    @Override
    public void onTrackCompleted() {
        //onNextClicked();
        load_music(musicHelper.getActiveTrack());
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
        if(musicHelper.getActiveTrack()!=null){
            load_music(musicHelper.getActiveTrack());
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