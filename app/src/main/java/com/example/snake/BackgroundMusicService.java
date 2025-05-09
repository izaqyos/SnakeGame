package com.example.snake; // Replace with your package name

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class BackgroundMusicService extends Service {
    private MediaPlayer mediaPlayer;
    private static boolean isPlaying = false;
    private float currentVolume = 1.0f; // Default volume

    public static boolean isPlaying() {
        return isPlaying;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = MediaPlayer.create(this, R.raw.music);
        mediaPlayer.setLooping(true);
        setVolume(currentVolume); // Apply initial volume
        Log.d("MusicService", "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("MusicService", "onStartCommand");
        if (intent != null && intent.hasExtra("volume")) {
            currentVolume = intent.getFloatExtra("volume", 1.0f);
            setVolume(currentVolume);
            Log.d("MusicService", "Volume updated to: " + currentVolume);
        }
        if (!isPlaying) {
            mediaPlayer.start();
            isPlaying = true;
            Log.d("MusicService", "Music started");
        }
        return START_STICKY;
    }

    private void setVolume(float volume) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volume, volume);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
            Log.d("MusicService", "Music stopped");
        }
    }
}