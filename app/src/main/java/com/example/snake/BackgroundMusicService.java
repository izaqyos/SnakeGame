package com.example.snake;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;

public class BackgroundMusicService extends Service {
    private MediaPlayer mediaPlayer;
    private static boolean isPlaying = false;
    private float currentVolume = 2.0f;
    public static boolean isPlaying() {
        return isPlaying;
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() { //היא אחראית על יצירת אובייקט ה-״MediaPlayer״, טעינת קובץ המוזיקה מהמשאבים (R.raw.music), הגדרת המוזיקה לנגינה חוזרת (looping), והחלת עוצמת השמע ההתחלתית
        super.onCreate();
        mediaPlayer = MediaPlayer.create(this, R.raw.music);
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(true);
        }
        setVolume(currentVolume);
        Log.d("MusicService", "onCreate");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { // נקראת מרכיב אחר, דואגת להתחיל את נגינת המוזיקה אם היא לא מופעלת כבר
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
    public void onDestroy() {//נקראת כאשר ה-Service עומד להיהרס ומשחררת משאבים שה-Service תפס, ובמיוחד את אובייקט ה-MediaPlayer.
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