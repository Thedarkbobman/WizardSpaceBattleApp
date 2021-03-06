package com.dandibhotla.ananth.wizardspacebattleapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Ananth on 9/11/2017.
 */

public class BackgroundSoundService extends Service {
    private static final String TAG = null;
    public static MediaPlayer player;
    public static boolean isPrepared = false;
    public IBinder onBind(Intent arg0) {

        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        player = MediaPlayer.create(this, R.raw.space_waltz);
        player.setLooping(true); // Set looping
        SharedPreferences sharedPref = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        player.setVolume(sharedPref.getInt("musicVolume",80)/(100f),sharedPref.getInt("musicVolume",80)/(100f));

    }
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences sharedPref = getSharedPreferences("Settings", Context.MODE_PRIVATE);

            player.start();


        return Service.START_NOT_STICKY;
    }

    public void onStart(Intent intent, int startId) {
        // TO DO
    }
    public IBinder onUnBind(Intent arg0) {
        // TO DO Auto-generated method
        return null;
    }

    public void onStop() {
    player.stop();
    }
    public void onPause() {
        stopService(new Intent(this, BackgroundSoundService.class));
    player.stop();
        stopService(new Intent(this, BackgroundSoundService.class));
        Log.v("sound","onpause");
    }
    public static void setVolume(int volume){
        int maxVolume = 100;
        if(player!=null) {
                player.setVolume(volume / (100f), volume / (100f));

        }
    }
    @Override
    public void onDestroy() {
        player.stop();
        player.release();
        stopService(new Intent(this, BackgroundSoundService.class));
    }

    @Override
    public void onLowMemory() {

    }
}