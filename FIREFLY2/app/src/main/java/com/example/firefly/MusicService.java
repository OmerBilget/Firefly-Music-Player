package com.example.firefly;



import android.app.Notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadata;
import android.media.MediaPlayer;

import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media.session.MediaButtonReceiver;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.FileDescriptor;
import java.util.concurrent.ExecutionException;


public class MusicService extends Service implements MediaPlayer.OnCompletionListener{
    private IBinder MBinder=new MyBinder();
    public static final String ACTION_NEXT="NEXT";
    public static final String ACTION_PREV="PREV";
    public static final String ACTION_PLAY="PLAY";
    public static final String ACTION_CLOSE="CLOSE";
    MediaPlayer mediaPlayer;
    MusicHelper musicHelper;
    MediaSessionCompat mediaSessionCompat;
    MediaControllerCompat mediaControllerCompat;
    Uri uri;
    OnTrackCompletionListener onTrackCompletionListener;
    OnNotificationUpdateListener onNotificationUpdateListener;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        loadMediaPlayer(this);
        return MBinder;
    }

    private static int NOTIFICATION_ID=123456;

    public class MyBinder extends Binder{
        MusicService getService(){
            return MusicService.this;
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        //Toast.makeText(this, "service started", Toast.LENGTH_SHORT).show();


        mediaSessionCompat=new MediaSessionCompat(getApplicationContext(),"tag");

        //prevent dismiss notification

        musicHelper=MusicHelper.getInstance();
        mediaSessionCompat.setActive(true);
        startForeground(NOTIFICATION_ID,getNotification(getApplicationContext(),false,musicHelper.getActiveTrack()));
        if(musicHelper.getMainList().getTracks().size()==0){
            return;
        }
        if(musicHelper.getActiveTrack()==null){
            musicHelper.setActiveTrack(musicHelper.getMainList().getTracks().get(0));
        }

        uri=musicHelper.getActiveTrack().getUri();
        createMediaPlayer(getApplicationContext(),uri);




        PlaybackStateCompat state = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PAUSE
                        | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS | PlaybackStateCompat.ACTION_REWIND| PlaybackStateCompat.ACTION_SEEK_TO)
                .setState(PlaybackStateCompat.STATE_PLAYING,0,1.0f,SystemClock.elapsedRealtime())
                .build();
        mediaSessionCompat.setPlaybackState(state);
        mediaControllerCompat=mediaSessionCompat.getController();


        mediaSessionCompat.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onCommand(String command, Bundle extras, ResultReceiver cb) {
                super.onCommand(command, extras, cb);
            }

            @Override
            public void onPlay() {

                if(mediaPlayer!=null){
                    if(onNotificationUpdateListener!=null){
                        boolean b=onNotificationUpdateListener.onNotificationPlayClicked();
                        if(b==false){
                            playButtonClicked();
                        }
                    }else{
                        playButtonClicked();
                    }
                }
            }

            @Override
            public void onPause() {

                if(mediaPlayer!=null){
                    if(onNotificationUpdateListener!=null){
                        boolean b=onNotificationUpdateListener.onNotificationPlayClicked();
                        if(b==false){
                            playButtonClicked();
                        }
                    }else{
                        playButtonClicked();
                    }
                }

            }

            @Override
            public void onSkipToNext() {

                if(mediaPlayer!=null){
                    if(onNotificationUpdateListener!=null){
                        boolean b=onNotificationUpdateListener.onNotificationNextClicked();
                        if(b==false){
                            nextButtonClicked();
                        }
                    }else{
                        nextButtonClicked();
                    }
                }

            }
            int d=0;
            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonEvent) {

                String intentAction = mediaButtonEvent.getAction();
                if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
                    KeyEvent event = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

                    if (event != null) {


                        int action = event.getAction();


                        switch (event.getKeyCode()) {
                            case KeyEvent.KEYCODE_HEADSETHOOK:
                                if (action == KeyEvent.ACTION_UP)
                                    if (SystemClock.uptimeMillis() - event.getDownTime() > 2000){
                                        Toast.makeText(MusicService.this, "Long click!", Toast.LENGTH_SHORT).show();
                                        return true;
                                    }else if (SystemClock.uptimeMillis() - event.getDownTime()<700) {
                                        d++;
                                        Handler handler = new Handler(Looper.getMainLooper());
                                        Runnable r = () -> {
                                            // single click *******************************
                                            if (d == 1) {
                                                this.onPlay();
                                            }
                                            // double click *********************************
                                            if (d == 2) {
                                                this.onSkipToNext();
                                            }

                                            if(d == 3){
                                                this.onSkipToPrevious();
                                            }
                                            d = 0;
                                        };
                                        if (d == 1 || d==2) {
                                            handler.postDelayed(r, 700);
                                        }
                                        if(d>=3){
                                            return true;
                                        }
                                    }
                                return true;

                        }
                    }
                }
                return super.onMediaButtonEvent(mediaButtonEvent);

            }

            @Override
            public void onStop() {

                stopMediaPlayer();
                stopService();
            }

            @Override
            public void onSeekTo(long pos) {
                if(mediaPlayer!=null){
                    super.onSeekTo(pos);
                    seekTo((int) pos);
                }

            }

            @Override
            public void onRewind() {
                if(onNotificationUpdateListener!=null){
                    boolean b=onNotificationUpdateListener.onNotificationPrevClicked();
                    if(b==false){
                        prevButtonClicked();
                    }
                }else{
                    prevButtonClicked();
                }
            }

            @Override
            public void onSkipToPrevious() {
                if(onNotificationUpdateListener!=null){
                    boolean b=onNotificationUpdateListener.onNotificationPrevClicked();
                    if(b==false){
                        prevButtonClicked();
                    }
                }else{
                    prevButtonClicked();
                }
            }
        });


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(musicHelper.getMainList().getTracks().size()==0){
            return  START_NOT_STICKY;
        }
        MediaButtonReceiver.handleIntent(mediaSessionCompat, intent);
        mediaSessionCompat.setActive(true);
        musicHelper=MusicHelper.getInstance();
        startForeground(NOTIFICATION_ID,getNotification(getApplicationContext(),musicHelper.isPlaying(),musicHelper.getActiveTrack()));
        String actionName=null;
        if(intent!=null){
            actionName=intent.getStringExtra("myAction");
        }
        if(actionName!=null){
            switch (actionName){
                case ACTION_PREV:

                    if(this.onNotificationUpdateListener!=null){
                        boolean b=this.onNotificationUpdateListener.onNotificationPrevClicked();
                        if(b==false){
                            this.prevButtonClicked();
                        }
                    }else{
                        this.prevButtonClicked();
                    }
                    break;

                case ACTION_PLAY:
                    if(this.onNotificationUpdateListener!=null){
                        boolean b=this.onNotificationUpdateListener.onNotificationPlayClicked();
                        if(b==false){
                            this.playButtonClicked();
                        }
                    }else{
                        this.playButtonClicked();
                    }

                    break;
                case ACTION_NEXT:
                    if(this.onNotificationUpdateListener!=null){
                        boolean b=this.onNotificationUpdateListener.onNotificationNextClicked();
                        if(b==false){
                            this.nextButtonClicked();
                        }
                    }else{
                        this.nextButtonClicked();
                    }
                    break;
                case ACTION_CLOSE:
                    long position=0;
                    if(mediaPlayer!=null){
                        position=getCurrentPosition();
                    }
                    musicHelper.saveTrackAll(getApplicationContext(),musicHelper.getActiveTrack(),position);
                    if(mediaPlayer!=null){
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                        mediaPlayer.release();
                    }
                    stopForeground(true);
                    mediaSessionCompat.release();
                    stopSelf();
                    System.exit(0);

                    return START_NOT_STICKY;
            }
        }


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //stopService();
    }

    public void stopService(){
        if(mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer=null;
        }
        stopForeground(true);
        stopSelf();
        System.exit(0);
    }


    public void switchTrack(Music music){
        if(music!=null){
            if(MusicHelper.getInstance().getMainList().getTracks().contains(music)){
                loadNewTrack(music);
            }
        }
    }
    public void playButtonClicked(){
        if(mediaPlayer==null){
            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
            return;
        }
        if(mediaPlayer.isPlaying()){
            updateMediaPlayerState(PlaybackStateCompat.STATE_PAUSED,getCurrentPosition());
            pauseTrack();
        }else{
            updateMediaPlayerState(PlaybackStateCompat.STATE_PLAYING,getCurrentPosition());
            playTrack();
            onCompleted();
        }
        musicHelper.setPlaying(mediaPlayer.isPlaying());
        updateNotification();
    }
    public void nextButtonClicked(){
        Music music=MusicHelper.getInstance().getNextSong();
        if(music!=null){
            stopMediaPlayer();
            releaseMediaPlayer();
            musicHelper.setActiveTrack(music);
            try{
                createMediaPlayer(getApplicationContext(),musicHelper.getActiveTrack().getUri());
                playTrack();
                onCompleted();
                musicHelper.setPlaying(true);
                updateMediaPlayerState(PlaybackStateCompat.STATE_PLAYING,getCurrentPosition());
                updateNotification();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }
    private void loadNewTrack(Music music){

        if(mediaPlayer==null){
            return;
        }
        stopMediaPlayer();
        releaseMediaPlayer();
        musicHelper.setActiveTrack(music);
        try{
            createMediaPlayer(getApplicationContext(),musicHelper.getActiveTrack().getUri());
            playTrack();
            onCompleted();
            musicHelper.setPlaying(true);
            updateMediaPlayerState(PlaybackStateCompat.STATE_PLAYING,getCurrentPosition());
            updateNotification();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    public void prevButtonClicked(){
        Music music=MusicHelper.getInstance().getPrevSong();
        if(music!=null){
            //if 5 seconds rewind
            if(getCurrentPosition()>=5000){
                seekTo(0);
            }else{
                stopMediaPlayer();
                releaseMediaPlayer();
                musicHelper.setActiveTrack(music);
                try{
                    createMediaPlayer(getApplicationContext(),musicHelper.getActiveTrack().getUri());
                    playTrack();
                    onCompleted();
                    musicHelper.setPlaying(true);
                    updateMediaPlayerState(PlaybackStateCompat.STATE_PLAYING,getCurrentPosition());
                    updateNotification();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

    }



    public void playTrack() {
        if(mediaPlayer!=null){
            mediaPlayer.start();
        }

    }


    public void pauseTrack() {
        if(mediaPlayer!=null){
            mediaPlayer.pause();
        }

    }


    public void seekTo(int position) {
        if(mediaPlayer!=null){
            mediaPlayer.seekTo(position);
            updateMediaPlayerState(PlaybackStateCompat.STATE_PLAYING,getCurrentPosition());
            updateNotification();
        }

    }


    public int getCurrentPosition() {
        if(mediaPlayer!=null){
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }


    public void stopMediaPlayer() {
        if(mediaPlayer!=null){
            mediaPlayer.stop();
        }
    }

    public int getDuration() {
        if(mediaPlayer!=null){
            return mediaPlayer.getDuration();
        }
        return 0;
    }


    public boolean isPlaying() {
        if(mediaPlayer!=null){
            return mediaPlayer.isPlaying();
        }
        return false;

    }

    public void releaseMediaPlayer(){
       if(mediaPlayer!=null){
           mediaPlayer.reset();
           mediaPlayer.release();
           mediaPlayer=null;
       }
    }


    public void createMediaPlayer(Context context,Uri uri){
        if(mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
        }
        mediaPlayer=MediaPlayer.create(context,uri);
    }

    public void loadMediaPlayer(Context context){
        musicHelper=MusicHelper.getInstance();
        if(musicHelper.getMainList().getTracks().size()==0){
            return;
        }
        if(musicHelper.getActiveTrack()!=null){
            try{
                uri=musicHelper.getActiveTrack().getUri();
                createMediaPlayer(context,uri);
                onCompleted();
            }catch(NullPointerException e){
                e.printStackTrace();
            }
        }
    }


    public void loadMediaPlayer(Context context,Music music){
        musicHelper=MusicHelper.getInstance();
        if(musicHelper.getMainList().getTracks().size()==0){
            return;
        }
        if(music!=null){
            try{
                uri=music.getUri();
                createMediaPlayer(context,uri);
                onCompleted();
            }catch(NullPointerException e){
                e.printStackTrace();
            }
        }

    }
    private void playMedia(){
        if(mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            if(musicHelper.getMainList()!=null){
                createMediaPlayer(getApplicationContext(),musicHelper.getActiveTrack().getUri());
            }
        }else{
            if(musicHelper.getActiveTrack()!=null){
                createMediaPlayer(getApplicationContext(),musicHelper.getActiveTrack().getUri());
            }

        }

    }

    void onCompleted(){
        if(mediaPlayer!=null){
            mediaPlayer.setOnCompletionListener(this);
        }

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if(mediaPlayer==null){
            return;
        }
        nextButtonClicked();
        if(this.onTrackCompletionListener!=null){
            onTrackCompletionListener.onTrackCompleted();
        }

    }

    public void setOnTrackCompletionListener(OnTrackCompletionListener onTrackCompletionListener) {
        this.onTrackCompletionListener = onTrackCompletionListener;
    }

    public void setOnNotificationUpdateListener(OnNotificationUpdateListener onNotificationUpdateListener) {
        this.onNotificationUpdateListener = onNotificationUpdateListener;
    }

    public Bitmap getAlbumArt(Long album_id)
    {
        Bitmap bm = null;
        try
        {
            final Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");

            Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);

            ParcelFileDescriptor pfd = getApplicationContext().getContentResolver()
                    .openFileDescriptor(uri, "r");

            if (pfd != null)
            {
                FileDescriptor fd = pfd.getFileDescriptor();
                bm = BitmapFactory.decodeFileDescriptor(fd);
            }
        } catch (Exception e) {
        }
        return bm;
    }
    public Notification getNotification(Context context, boolean isPlaying, Music music){
        if(music==null){
            Notification notification=new androidx.core.app.NotificationCompat.Builder(context,Application.CHANNEL_ID1)
                    .setSmallIcon(R.drawable.music_icon)

                    .setContentTitle("Title")
                    .setContentText("artist")
                    .setColor(context.getResources().getColor(R.color.foreground_1,null))
                    .setPriority(androidx.core.app.NotificationCompat.PRIORITY_MAX)
                    .setOngoing(true)
                    .setAutoCancel(false)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setOnlyAlertOnce(true).build();
            android.app.NotificationManager notificationManager=(android.app.NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
            //notificationManager.notify(0,notification);
            return notification;
        }
        //this opens application
        Intent intent=new Intent(context,MainActivity.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(context,0,intent,0);
        Bitmap albumCoverBitmap=getAlbumArt(music.getAlbum_id());
        if(albumCoverBitmap==null){
            albumCoverBitmap=BitmapFactory.decodeResource(getApplicationContext().getResources(),
                    R.drawable.empty_icon);
        }


        mediaSessionCompat.setMetadata(
                new MediaMetadataCompat.Builder()
                        .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, albumCoverBitmap)
                        .putString(MediaMetadata.METADATA_KEY_TITLE, music.getTrackName())
                        .putString(MediaMetadata.METADATA_KEY_ARTIST,music.getArtistName())
                        .putLong(MediaMetadata.METADATA_KEY_DURATION,Long.valueOf(music.getTrackDurationMilliseconds()))
                        .build()
        );
        Intent prevIntent=new Intent(context,NotificationsReceiver.class).setAction(ACTION_PREV);
        PendingIntent pendingIntentPrev=PendingIntent.getBroadcast(context,0,prevIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent playIntent=new Intent(context,NotificationsReceiver.class).setAction(ACTION_PLAY);
        PendingIntent pendingIntentPlay=PendingIntent.getBroadcast(context,0,playIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent=new Intent(context,NotificationsReceiver.class).setAction(ACTION_NEXT);
        PendingIntent pendingIntentNext=PendingIntent.getBroadcast(context,0,nextIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent closeIntent=new Intent(context,NotificationsReceiver.class).setAction(ACTION_CLOSE);
        PendingIntent pendingIntentClose=PendingIntent.getBroadcast(context,0,closeIntent,PendingIntent.FLAG_UPDATE_CURRENT);


        int icon=R.drawable.play;
        if(isPlaying){
            icon=R.drawable.pause;
        }

        Notification notification=new androidx.core.app.NotificationCompat.Builder(context,Application.CHANNEL_ID1)
                .setSmallIcon(R.drawable.music_icon)
                .setLargeIcon(albumCoverBitmap)
                .setContentTitle(music.getTrackName())
                .setContentText(music.getArtistName())
                .addAction(R.drawable.previous,"<<",pendingIntentPrev)
                .addAction(icon,"||",pendingIntentPlay)
                .addAction(R.drawable.next,">>",pendingIntentNext)
                .addAction(R.drawable.delete,"X",pendingIntentClose)
                .setStyle(new  androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0,1,2).setMediaSession(mediaSessionCompat.getSessionToken()))
                .setColor(context.getResources().getColor(R.color.foreground_1,null))
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_MAX)
                .setOngoing(true)
                .setAutoCancel(false)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true).build();
        android.app.NotificationManager notificationManager=(android.app.NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
        //notificationManager.notify(0,notification);
        return notification;

    }

    private void updateMediaPlayerState(int currentState,long currentPosition){
        if(mediaSessionCompat!=null){
            PlaybackStateCompat state = new PlaybackStateCompat.Builder()
                    .setActions(PlaybackStateCompat.ACTION_PLAY
                            | PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PAUSE
                            | PlaybackStateCompat.ACTION_SKIP_TO_NEXT |PlaybackStateCompat.ACTION_REWIND| PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS | PlaybackStateCompat.ACTION_SEEK_TO)
                    .setState(currentState,currentPosition,1f,SystemClock.elapsedRealtime())
                    .build();
          mediaSessionCompat.setPlaybackState(state);
        }
    }
    public void updateNotification(){
        Notification notification=getNotification(getApplicationContext(),musicHelper.isPlaying(),musicHelper.getActiveTrack());
        NotificationManager notificationManager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID,notification);


    }
    public interface OnTrackCompletionListener{
        void onTrackCompleted();
    }
    public interface OnNotificationUpdateListener{
        boolean onNotificationPrevClicked();
        boolean onNotificationPlayClicked();
        boolean onNotificationNextClicked();
    }

}
