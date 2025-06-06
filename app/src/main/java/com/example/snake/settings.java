package com.example.snake;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class settings extends AppCompatActivity {
    private Switch musicSwitch;
    private SharedPreferences sharedPreferencesMusic;
    private static final String PREF_MUSIC = "pref_music";  // מפתח לשמירת מצב המוזיקה
    private static final String MUSIC_PREFS_NAME = "game_settings";  // שם קובץ ההעדפות
    private Button buttonColorGreen, buttonColorRed, buttonColorBlue, buttonColorYellow, buttonColorWhite;
    private RadioGroup difficultyRadioGroup;
    private SharedPreferences sharedPreferences;
    private static final String PREF_DIFFICULTY = "pref_difficulty";
    private static final String SETTINGS_PREFS_NAME = "game_settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // כפתור חזרה למסך הראשי
        ImageButton backButton = findViewById(R.id.backButton3);
        if (backButton != null) {
            backButton.setOnClickListener(v -> returnToMainActivity());
        }

        // הגדרת ה-Switch עבור המוזיקה
        musicSwitch = findViewById(R.id.musicSwitch);
        if (musicSwitch != null) {
            sharedPreferencesMusic = getSharedPreferences(MUSIC_PREFS_NAME, MODE_PRIVATE);
            boolean isMusicEnabled = sharedPreferencesMusic.getBoolean(PREF_MUSIC, true); // ברירת המחדל: מוזיקה מופעלת
            musicSwitch.setChecked(isMusicEnabled);

            // מאזין לשינוי במצב ה-Switch
            musicSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                SharedPreferences.Editor editor = sharedPreferencesMusic.edit();
                editor.putBoolean(PREF_MUSIC, isChecked); // שמירה של המצב החדש
                editor.apply();
                updateMusicService(isChecked); // עדכון השירות בהתאם למצב החדש
            });
        }

        // אתחול כפתורי צבעים לנחש
        initializeColorButtons();
    }

    // אתחול כפתורים לבחירת צבע לנחש
    private void initializeColorButtons() {
        buttonColorGreen = findViewById(R.id.button_color_green);
        buttonColorRed = findViewById(R.id.button_color_red);
        buttonColorBlue = findViewById(R.id.button_color_blue);
        buttonColorYellow = findViewById(R.id.button_color_yellow);
        buttonColorWhite = findViewById(R.id.button_color_white);

        // לכל כפתור צבע מצורף מאזין שיבחר את הצבע ויודיע למשתמש
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
            buttonColorYellow.setOnClickListener(v -> saveSnakeColorAndNotify(Color.parseColor("#FFEB3B"), "Yellow"));
        }
        if (buttonColorWhite != null) {
            buttonColorWhite.setOnClickListener(v -> saveSnakeColorAndNotify(Color.WHITE, "White"));
        }
    }

    // שמירת צבע הנחש להעדפות והצגת הודעה למשתמש
    private void saveSnakeColorAndNotify(int colorToSave, String colorName) {
        PrefsManager.saveSnakeColor(this, colorToSave);  // שמירה בעזרת PrefsManager
        Toast.makeText(this, "Snake color set to " + colorName, Toast.LENGTH_SHORT).show();  // הודעה למשתמש
    }

    // עדכון שירות המוזיקה (הפעלה/הפסקה)
    private void updateMusicService(boolean enableMusic) {
        Intent serviceIntent = new Intent(this, BackgroundMusicService.class);
        if (enableMusic) {
            startService(serviceIntent);  // הפעלת המוזיקה
        } else {
            stopService(serviceIntent);   // עצירת המוזיקה
        }
    }

    // חזרה למסך הראשי
    private void returnToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }


    // אם המשתמש לוחץ על כפתור החזרה במכשיר
    @Override
    public void onBackPressed() {
        returnToMainActivity();
    }
}
