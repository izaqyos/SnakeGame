package com.example.snake;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class level1 extends AppCompatActivity implements SurfaceHolder.Callback {
    private FrameLayout frame;
    private boolean started = false;
    private int scrHeight;
    private int scrWidth;
    // Make package-private for testing
    /* private */ GameManager myGameManager;
    private SurfaceHolder surfaceHolder;
    private static final String TAG = "level1"; // Add TAG for logging

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level1);
        frame = findViewById(R.id.gameFrame);
        
        // DO NOT setup buttons here, wait for GameManager in onWindowFocusChanged
        // setupDirectionButtons(); 
        Log.d(TAG, "onCreate completed.");
    }

    private void setupDirectionButtons() {
        Log.i(TAG, "setupDirectionButtons: Setting up listeners..."); // Log entry
        ImageButton buttonUp = findViewById(R.id.buttonUp);
        ImageButton buttonDown = findViewById(R.id.buttonDown);
        ImageButton buttonLeft = findViewById(R.id.buttonLeft);
        ImageButton buttonRight = findViewById(R.id.buttonRight);

        buttonUp.setOnClickListener(v -> {
            Log.d("level1Buttons", "Up button clicked.");
            if (myGameManager != null) {
                myGameManager.setDirection(GameManager.Direction.UP);
            } else {
                Log.w("level1Buttons", "Up click ignored: myGameManager is null.");
            }
        });
        buttonDown.setOnClickListener(v -> {
             Log.d("level1Buttons", "Down button clicked.");
            if (myGameManager != null) {
                myGameManager.setDirection(GameManager.Direction.DOWN);
            } else {
                 Log.w("level1Buttons", "Down click ignored: myGameManager is null.");
            }
        });
        buttonLeft.setOnClickListener(v -> {
             Log.d("level1Buttons", "Left button clicked.");
            if (myGameManager != null) {
                myGameManager.setDirection(GameManager.Direction.LEFT);
            } else {
                 Log.w("level1Buttons", "Left click ignored: myGameManager is null.");
            }
        });
        buttonRight.setOnClickListener(v -> {
             Log.d("level1Buttons", "Right button clicked.");
            if (myGameManager != null) {
                myGameManager.setDirection(GameManager.Direction.RIGHT);
            } else {
                 Log.w("level1Buttons", "Right click ignored: myGameManager is null.");
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.d(TAG, "onWindowFocusChanged - hasFocus: " + hasFocus + ", myGameManager is null: " + (myGameManager == null));
        if (hasFocus && myGameManager == null) { // Initialize only once when focus is gained
            scrHeight = frame.getHeight();
            scrWidth = frame.getWidth();
            Log.d(TAG, "onWindowFocusChanged - Dimensions: " + scrWidth + "x" + scrHeight);
            
            if (scrHeight > 0 && scrWidth > 0) { // Ensure dimensions are valid
                 // Redundant check, should be null based on outer condition
                 // if (myGameManager != null) { ... }
                 
                Log.i(TAG, "onWindowFocusChanged: Creating GameManager...");
                myGameManager = new GameManager(this, scrWidth, scrHeight);
                frame.addView(myGameManager);
                surfaceHolder = myGameManager.getHolder(); 
                surfaceHolder.addCallback(this); 
                Log.i(TAG, "onWindowFocusChanged: Starting GameManager thread...");
                myGameManager.start();
                started = true;
                
                // Setup buttons AFTER GameManager is created and added
                Log.i(TAG, "onWindowFocusChanged: Calling setupDirectionButtons.");
                setupDirectionButtons(); 
            } else {
                Log.w(TAG, "onWindowFocusChanged: Invalid dimensions, cannot create GameManager.");
            }
        } else if (hasFocus) {
            Log.d(TAG, "onWindowFocusChanged: Focus gained, but GameManager already exists.");
             // If resuming, ensure buttons are set up if layout was somehow recreated
             // Might be unnecessary if setupDirectionButtons() is robust
             if (findViewById(R.id.buttonUp).hasOnClickListeners()) {
                  Log.d(TAG, "onWindowFocusChanged: Buttons seem to have listeners already.");
             } else {
                 Log.w(TAG, "onWindowFocusChanged: Buttons had no listeners on refocus? Re-setting up.");
                 setupDirectionButtons();
             }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (myGameManager != null) {
            myGameManager.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (myGameManager != null) {
            myGameManager.resume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myGameManager != null) {
            myGameManager.stopThread();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (myGameManager != null) {
            myGameManager.stopThread();
        }
    }
}