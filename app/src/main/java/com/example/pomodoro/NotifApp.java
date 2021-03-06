package com.example.pomodoro;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.util.Log;


public class NotifApp extends Application {

    public static final String CHANNEL_1_ID = "channel1";
    public static final String CHANNEL_2_ID = "channel2";
    public static final String CHANNEL_3_ID = "channel3";


    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        Log.d("TOAST","notification________________________________channel___________________________created");

        jumpToNewActivity();

    }

    private void jumpToNewActivity() {
            Log.d("NOTIF","USER SEEMS TO BE NULL IN THIS SYSTEM WHICH IS NOT WEIRD XD*********************");
            Intent intent =  new Intent(NotifApp.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
    }




    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "Channel 1",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("This is Chanel 1");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
        }
    }
}
