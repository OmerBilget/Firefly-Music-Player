package com.example.firefly;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.media.session.MediaButtonReceiver;

public class NotificationsReceiver extends MediaButtonReceiver {
    public static final String ACTION_NEXT="NEXT";
    public static final String ACTION_PREV="PREV";
    public static final String ACTION_PLAY="PLAY";
    public static final String ACTION_CLOSE="CLOSE";
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intentNotification=new Intent(context, MusicService.class);

       if(intent.getAction()!=null){
           switch (intent.getAction()){
               case ACTION_PREV:

                   intentNotification.putExtra("myAction",intent.getAction());
                   context.startService(intentNotification);
                   break;
               case ACTION_PLAY:

                   intentNotification.putExtra("myAction",intent.getAction());
                   context.startService(intentNotification);
                   break;
               case ACTION_NEXT:

                   intentNotification.putExtra("myAction",intent.getAction());
                   context.startService(intentNotification);
                   break;
               case ACTION_CLOSE:

                   intentNotification.putExtra("myAction",intent.getAction());
                   context.startService(intentNotification);
           }
       }
    }

}
