package com.example.snake;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;

public class GameActivity extends AppCompatActivity {

    private GameManager gameManager;
    private TextView scoreTextView;
    private FrameLayout gameSurfaceContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game); // Use the new layout

        scoreTextView = findViewById(R.id.scoreTextView);
        gameSurfaceContainer = findViewById(R.id.gameSurfaceContainer);

        // Pass 'this' (GameActivity context and the activity itself) to GameManager
        gameManager = new GameManager(this, this);
        gameSurfaceContainer.addView(gameManager);

        // Find buttons as ImageButtons
        ImageButton buttonUp = findViewById(R.id.buttonUp);
        ImageButton buttonDown = findViewById(R.id.buttonDown);
        ImageButton buttonLeft = findViewById(R.id.buttonLeft);
        ImageButton buttonRight = findViewById(R.id.buttonRight);
        ImageButton backButton = findViewById(R.id.backButton);

        // Set listeners using GameManager.Direction
        buttonUp.setOnClickListener(v -> gameManager.setDirection(GameManager.Direction.UP));
        buttonDown.setOnClickListener(v -> gameManager.setDirection(GameManager.Direction.DOWN));
        buttonLeft.setOnClickListener(v -> gameManager.setDirection(GameManager.Direction.LEFT));
        buttonRight.setOnClickListener(v -> gameManager.setDirection(GameManager.Direction.RIGHT));

        // Set listener for back button
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(GameActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameManager != null) {
            gameManager.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameManager != null) {
            gameManager.pause();
        }
    }

    // Method for GameManager to call to update the score TextView
    public void updateScore(int score) {
        // Ensure UI updates happen on the main thread
        runOnUiThread(() -> {
            if (scoreTextView != null) {
                scoreTextView.setText("Score: " + score);
            }
        });
    }
} 