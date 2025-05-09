package com.example.snake; // Make sure this matches your actual package name

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color; // <<-- Make sure this is imported
import android.os.Bundle;
import android.view.View;
import android.widget.Button;     // <<-- Make sure this is imported
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;    // <<-- Make sure this is imported (for user feedback)

import androidx.appcompat.app.AppCompatActivity;

public class settings extends AppCompatActivity { // Class name is 'settings' as per your file

    private Switch musicSwitch;
    private SharedPreferences sharedPreferencesMusic; // For music settings
    private static final String PREF_MUSIC = "pref_music";
    private static final String MUSIC_PREFS_NAME = "game_settings"; // SharedPreferences file for music

    // Declare buttons for color selection
    private Button buttonColorGreen;
    private Button buttonColorRed;
    private Button buttonColorBlue;
    private Button buttonColorYellow;
    private Button buttonColorWhite;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings); // Your XML layout file

        ImageButton backButton = findViewById(R.id.backButton3);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                Intent intent = new Intent(settings.this, MainActivity.class);
                startActivity(intent);
                finish();
            });
        }

        // --- Music Switch Logic (Your existing logic) ---
        musicSwitch = findViewById(R.id.musicSwitch);
        if (musicSwitch != null) {
            sharedPreferencesMusic = getSharedPreferences(MUSIC_PREFS_NAME, MODE_PRIVATE);
            boolean isMusicEnabled = sharedPreferencesMusic.getBoolean(PREF_MUSIC, true); // Default to true

            musicSwitch.setChecked(isMusicEnabled);
            // updateMusicService(isMusicEnabled); // Consider if initial call is needed or handled elsewhere

            musicSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                SharedPreferences.Editor editor = sharedPreferencesMusic.edit();
                editor.putBoolean(PREF_MUSIC, isChecked);
                editor.apply();
                updateMusicService(isChecked);
            });
        }

        // --- Snake Color Selection Logic (New) ---
        initializeColorButtons();
    }

    private void initializeColorButtons() {
        buttonColorGreen = findViewById(R.id.button_color_green);
        buttonColorRed = findViewById(R.id.button_color_red);
        buttonColorBlue = findViewById(R.id.button_color_blue);
        buttonColorYellow = findViewById(R.id.button_color_yellow);
        buttonColorWhite = findViewById(R.id.button_color_white);

        // Set OnClickListeners for each color button
        if (buttonColorGreen != null) {
            buttonColorGreen.setOnClickListener(v -> saveSnakeColorAndNotify(Color.parseColor("#4CAF50"), "Green"));
        }
        if (buttonColorRed != null) {
            buttonColorRed.setOnClickListener(v -> saveSnakeColorAndNotify(Color.parseColor("#F44336"), "Red"));
        }
        if (buttonColorBlue != null) {
            buttonColorBlue.setOnClickListener(v -> saveSnakeColorAndNotify(Color.parseColor("#2196F3"), "Blue"));
        }
        if (buttonColorYellow != null) {
            // Material Yellow 500 for better visibility than pure yellow
            buttonColorYellow.setOnClickListener(v -> saveSnakeColorAndNotify(Color.parseColor("#FFEB3B"), "Yellow"));
        }
        if (buttonColorWhite != null) {
            buttonColorWhite.setOnClickListener(v -> saveSnakeColorAndNotify(Color.WHITE, "White"));
        }
    }

    private void saveSnakeColorAndNotify(int colorToSave, String colorName) {
        // Use your PrefsManager to save the snake color.
        // This ensures it's saved to "SnakeGamePrefs" file with "snake_color" key.
        PrefsManager.saveSnakeColor(this, colorToSave);

        // Optional: Give user feedback that the color was set
        Toast.makeText(this, "Snake color set to " + colorName, Toast.LENGTH_SHORT).show();

        // You could also visually update the selected button here if desired (e.g., add a border)
    }

    private void updateMusicService(boolean enableMusic) {
        Intent serviceIntent = new Intent(this, BackgroundMusicService.class); // Assuming BackgroundMusicService.class exists
        if (enableMusic) {
            startService(serviceIntent);
        } else {
            stopService(serviceIntent);
        }
    }

    @Override
    public void onBackPressed() {
        // This is called when the system's back button is pressed.
        // We want the same behavior as your custom backButton3: just finish the activity.
        super.onBackPressed(); // This calls finish() by default.
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Your existing onDestroy logic, if any.
        // For the music service, if it's a started service, it might continue
        // running depending on its implementation (e.g., START_STICKY).
    }
}



