package com.example.firefly;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class SearchActivity extends AppCompatActivity implements
        RecyclerAdapterSearchMain.SearchListOnClickListener ,
        MusicService.OnTrackCompletionListener,
        MusicService.OnNotificationUpdateListener,
        ServiceConnection
{
    ImageView backButton;
    EditText editText;
    RecyclerView recyclerView;
    RecyclerAdapterSearchMain recyclerAdapterSearchMain;
    LinearLayoutManager linearLayoutManager;
    MusicHelper musicHelper;

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
        setContentView(R.layout.activity_search);

        musicHelper=MusicHelper.getInstance();


        backButton=findViewById(R.id.searchBackButton);
        editText=findViewById(R.id.searchEditText);
        editText.setText("");
        backButton.setOnClickListener(view -> {
            finishAct(true);
        });


        recyclerView=findViewById(R.id.searchRecyclerView);
        recyclerAdapterSearchMain=new RecyclerAdapterSearchMain(this,this);
        recyclerView.setAdapter(recyclerAdapterSearchMain);
        linearLayoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);




        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(recyclerView!=null && recyclerAdapterSearchMain!=null){
                    recyclerAdapterSearchMain.getFilter().filter(editable.toString());
                }
            }
        });

        bottomBar=findViewById(R.id.searchActivityBottomBar);
        prev=findViewById(R.id.searchActivityPrevButton);
        play=findViewById(R.id.searchActivityPlayButton);
        next=findViewById(R.id.searchActivityNextButton);
        bottomBarImage=findViewById(R.id.searchActivityBottomImage);
        bottomBarTrackName=findViewById(R.id.searchActivityBottomTrackName);
        bottomBarArtist=findViewById(R.id.searchActivityBottomArtistName);

        bottomBar.setOnClickListener(view -> {
            try {
                closeKeyboard();
            }catch (Exception e){

            }
            Intent intent=new Intent(SearchActivity.this,PlayScreenActivity.class);
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

    }

    private void finishAct(boolean b){
        Intent resultIntent = new Intent();
        resultIntent.putExtra("is_updated", b);
        setResult(22, resultIntent);
        finishAfterTransition();
    }

    @Override
    public void onSearchListClicked(int position, TrackList trackList) {
        if(mBound && musicService!=null){
            if(trackList!=null && trackList.getTracks()!=null){
                if(musicHelper.getActiveTracklist().equals(trackList)==false){
                    musicHelper.setActiveTracklist(trackList);
                }
                Music music=trackList.getTracks().get(position);
                if(music!=null){
                    musicService.switchTrack(music);
                    setBottomBar(music);
                    if(musicHelper!=null && play!=null){
                        if(musicHelper.isPlaying()){
                            play.setImageResource(R.drawable.pause);
                        }else{
                            play.setImageResource(R.drawable.play);
                        }
                    }
                }

            }
        }
    }
    private void closeKeyboard()
    {
        // this will give us the view
        // which is currently focus
        // in this layout
        View view = this.getCurrentFocus();

        // if nothing is currently
        // focus then this will protect
        // the app from crash
        if (view != null) {

            // now assign the system
            // service to InputMethodManager
            InputMethodManager manager
                    = (InputMethodManager)
                    getSystemService(
                            Context.INPUT_METHOD_SERVICE);
            manager
                    .hideSoftInputFromWindow(
                            view.getWindowToken(), 0);
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