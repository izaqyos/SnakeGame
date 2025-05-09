package com.example.snake;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher; // <<-- IMPORT THIS
import androidx.activity.result.contract.ActivityResultContracts; // <<-- IMPORT THIS
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.util.Log; // For logging
import android.view.View;
import android.widget.Button; // You're using Button for restartButton

public class GameActivity extends AppCompatActivity {

    private GameManager gameManager;
    private TextView scoreTextView;
    private FrameLayout gameSurfaceContainer;
    private Button restartButton; // This is a Button in your layout

    // Launcher for SettingsActivity
    // This will handle starting 'settings.class' and getting a result/callback
    private final ActivityResultLauncher<Intent> settingsLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                // This block is executed when 'settings.class' (your SettingsActivity) finishes
                // and returns to GameActivity.
                Log.d("GameActivity", "Returned from settings.class");
                if (gameManager != null) {
                    // Tell GameManager to reload the color preference and update its paint object
                    gameManager.refreshAppearance();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game); // Use your game layout

        scoreTextView = findViewById(R.id.scoreTextView);
        gameSurfaceContainer = findViewById(R.id.gameSurfaceContainer);
        restartButton = findViewById(R.id.restartButton); // Ensure this ID matches your Button in XML

        // Pass 'this' (GameActivity context and the activity itself) to GameManager
        gameManager = new GameManager(this, this);
        if (gameSurfaceContainer != null) {
            gameSurfaceContainer.addView(gameManager);
        } else {
            Log.e("GameActivity", "gameSurfaceContainer is null! Cannot add GameManager.");
            // Consider finishing the activity or showing an error if container is vital
            finish(); // Example: finish if the container is not found
            return;
        }

        // Find buttons as ImageButtons
        ImageButton buttonUp = findViewById(R.id.buttonUp);
        ImageButton buttonDown = findViewById(R.id.buttonDown);
        ImageButton buttonLeft = findViewById(R.id.buttonLeft);
        ImageButton buttonRight = findViewById(R.id.buttonRight);
        ImageButton backButton = findViewById(R.id.backButton);
        ImageButton settingsButton = findViewById(R.id.buttonSettings2); // Your settings button

        // Set listeners using GameManager.Direction
        if (buttonUp != null) buttonUp.setOnClickListener(v -> gameManager.setDirection(GameManager.Direction.UP));
        if (buttonDown != null) buttonDown.setOnClickListener(v -> gameManager.setDirection(GameManager.Direction.DOWN));
        if (buttonLeft != null) buttonLeft.setOnClickListener(v -> gameManager.setDirection(GameManager.Direction.LEFT));
        if (buttonRight != null) buttonRight.setOnClickListener(v -> gameManager.setDirection(GameManager.Direction.RIGHT));

        // Set listener for back button
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                Intent intent = new Intent(GameActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish(); // Finish GameActivity when going back to MainActivity
            });
        }

        // Set listener for restart button
        if (restartButton != null) {
            restartButton.setOnClickListener(v -> {
                if (gameManager != null) {
                    gameManager.restartGame();
                }
                hideRestartButton(); // Hide after clicking
            });
        }

        // Set listener for settings button
        if (settingsButton != null) {
            settingsButton.setOnClickListener(v -> {
                Intent intent = new Intent(GameActivity.this, settings.class); // Assuming 'settings' is your SettingsActivity class name
                settingsLauncher.launch(intent); // <<-- USE THE LAUNCHER HERE
                // <<-- DO NOT call finish() on GameActivity
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("GameActivity", "onResume called");
        if (gameManager != null) {
            gameManager.resume(); // Resume game logic in GameManager

            // Update UI based on game state from GameManager
            if (gameManager.isGameOver()) { // <<-- USE THE GETTER
                showRestartButton();
            } else {
                hideRestartButton();
            }
            // Optional: Refresh appearance here too, as a general refresh point.
            // The launcher callback is more specific for immediate return from settings.
            // gameManager.refreshAppearance();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("GameActivity", "onPause called");
        if (gameManager != null) {
            gameManager.pause(); // Pause game logic in GameManager
        }
    }

    // Method for GameManager to call to update the score TextView
    public void updateScore(int score) {
        runOnUiThread(() -> {
            if (scoreTextView != null) {
                scoreTextView.setText("Score: " + score);
            }
        });
    }

    // Method for GameManager to call to show the restart button
    public void showRestartButton() {
        runOnUiThread(() -> {
            if (restartButton != null) {
                restartButton.setVisibility(View.VISIBLE);
            }
        });
    }

    // Method to hide the restart button
    private void hideRestartButton() {
        runOnUiThread(() -> {
            if (restartButton != null) {
                restartButton.setVisibility(View.GONE);
            }
        });
    }
}