package com.example.firefly;

import android.Manifest;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class Application extends android.app.Application  {

    public static final String CHANNEL_ID1="CHANNEL1";
    public static final String CHANNEL_ID2="CHANNEL2";
    public static final String ACTION_NEXT="NEXT";
    public static final String ACTION_PREV="PREV";
    public static final String ACTION_PLAY="PLAY";
    public static final String ACTION_CLOSE="CLOSE";
    MusicHelper musicHelper;
    @Override
    public void onCreate() {
        musicHelper=MusicHelper.getInstance();
        super.onCreate();
        createNotificationServiceChannel();

    }

    private void createNotificationServiceChannel(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel notificationChannel1=new NotificationChannel(CHANNEL_ID1,"channel1", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel1.setDescription("Channel 1 description");

            NotificationChannel notificationChannel2=new NotificationChannel(CHANNEL_ID2,"channel2", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel2.setDescription("Channel 2 description");

            NotificationManager notificationManager=getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel1);
            notificationManager.createNotificationChannel(notificationChannel2);

        }
    }




}
