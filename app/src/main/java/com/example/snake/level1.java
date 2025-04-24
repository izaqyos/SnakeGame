package com.example.snake;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder; // Import this
import android.view.View;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;

public class level1 extends AppCompatActivity implements SurfaceHolder.Callback { // Implement SurfaceHolder.Callback
    private FrameLayout frame;
    private boolean started = false;
    private int scrHeight;
    private int scrWidth;
    GameManager myGameManager;
    private SurfaceHolder surfaceHolder; // Add this

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level1);
        frame = findViewById(R.id.gameFrame);
        // Get the SurfaceHolder from the GameManager's SurfaceView
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        scrHeight = frame.getHeight();
        scrWidth = frame.getWidth();

        if (myGameManager != null) {
            frame.removeView(myGameManager);
            myGameManager.stopThread(); // Add a stopThread() method to GameManager
        }
        myGameManager = new GameManager(this, scrWidth, scrHeight);
        frame.addView(myGameManager);
        surfaceHolder = myGameManager.getHolder(); // Get the holder
        surfaceHolder.addCallback(this); // Set the callback
        myGameManager.start();
        started = true;
    }

    public void start(View v) {
        if (myGameManager != null && !started) {
            myGameManager.start();
            started = true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (myGameManager != null) {
            myGameManager.onTouch(event);
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (myGameManager != null) {
            myGameManager.pause(); // Add a pause() method to GameManager
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (myGameManager != null) {
            myGameManager.resume(); // Add a resume() method to GameManager
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myGameManager != null) {
            myGameManager.stopThread(); // Ensure thread stops on destroy
        }
    }

    // Implement SurfaceHolder.Callback methods (even if empty for now)
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // Called when the surface is first created
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Called when the surface changes size or format
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Called when the surface is about to be destroyed
        if (myGameManager != null) {
            myGameManager.stopThread();
        }
    }
}