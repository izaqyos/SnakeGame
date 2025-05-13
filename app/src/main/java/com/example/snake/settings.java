package com.example.snake;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
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

        sharedPreferences = getSharedPreferences(SETTINGS_PREFS_NAME, MODE_PRIVATE);
        difficultyRadioGroup = findViewById(R.id.radioGroupDifficulty);

        // שליפת רמת הקושי שנשמרה
        int savedDifficulty = sharedPreferences.getInt(PREF_DIFFICULTY, 0); // ברירת מחדל 0 (קל)
        if (savedDifficulty == 0) {
            difficultyRadioGroup.check(R.id.radioEasy);
        } else if (savedDifficulty == 1) {
            difficultyRadioGroup.check(R.id.radioMedium);
        } else {
            difficultyRadioGroup.check(R.id.radioHard);
        }

        difficultyRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int difficultyLevel = 0;
            if (checkedId == R.id.radioMedium) {
                difficultyLevel = 1; // בינוני
            } else if (checkedId == R.id.radioHard) {
                difficultyLevel = 2; // קשה
            }
            // שמירה ב-SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(PREF_DIFFICULTY, difficultyLevel);
            editor.apply();
        });

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
    public void startGame(View v) {
        SharedPreferences prefs = getSharedPreferences(SETTINGS_PREFS_NAME, MODE_PRIVATE);
        int difficultyLevel = prefs.getInt(PREF_DIFFICULTY, 0); // ברירת מחדל 0 (קל)

        int speed;
        switch (difficultyLevel) {
            case 0: // Easy
                speed = 10;  // מהירות נמוכה
                break;
            case 1: // Medium
                speed = 20;  // מהירות בינונית
                break;
            case 2: // Hard
                speed = 30;  // מהירות גבוהה
                break;
            default:
                speed = 10;  // ברירת מחדל
        }

        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("SPEED", speed); // שלח את המהירות למסך המשחק
        startActivity(intent);
    }

    // אם המשתמש לוחץ על כפתור החזרה במכשיר
    @Override
    public void onBackPressed() {
        returnToMainActivity();
    }
}
