package com.example.snake;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private UserFileStorage userFileStorage;
    private MyFBDB myFBDB;
    private Dialog helpDialog;
    private EditText etUserName, etPassword;
    private ProgressBar loginProgressBar;
    private TextView loginStatusText;
    private Button playButton;
    private Switch saveUserSwitch;

    private static final String AUTH_PREFS_NAME = "SnakeAuthPrefs";
    private static final String KEY_LAST_USERNAME = "last_username";
    private static final String KEY_LAST_PASSWORD = "last_password";
    private static final String KEY_SAVE_USER = "save_user";
    private static final String MUSIC_PREFS_NAME = "game_settings";
    private static final String PREF_MUSIC = "pref_music";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userFileStorage = new UserFileStorage(this);
        myFBDB = new MyFBDB();
        etUserName = findViewById(R.id.etUserName);
        etPassword = findViewById(R.id.etPassword);
        loginProgressBar = findViewById(R.id.loginProgressBar);
        loginStatusText = findViewById(R.id.loginStatusText);
        playButton = findViewById(R.id.startGameButton);
        saveUserSwitch = findViewById(R.id.saveUserSwitch);

        // טעינת פרטי התחברות שמורים ומצב "זכור אותי"
        SharedPreferences prefs = getSharedPreferences(AUTH_PREFS_NAME, MODE_PRIVATE);
        String lastUsername = prefs.getString(KEY_LAST_USERNAME, null);
        String lastPassword = prefs.getString(KEY_LAST_PASSWORD, null);
        boolean isSaveUserEnabled = prefs.getBoolean(KEY_SAVE_USER, false);
        if (lastUsername != null && isSaveUserEnabled) {
            etUserName.setText(lastUsername);
            etPassword.setText(lastPassword);
        }

        // קריאה להעדפת המוזיקה במסך הראשי
        SharedPreferences musicPrefs = getSharedPreferences(MUSIC_PREFS_NAME, MODE_PRIVATE);
        boolean isMusicEnabled = musicPrefs.getBoolean(PREF_MUSIC, true); // ברירת מחדל: מוזיקה פועלת
        if (isMusicEnabled && !BackgroundMusicService.isPlaying()) {
            startMusicService();
        }

        saveUserSwitch.setChecked(isSaveUserEnabled);         // הגדרת מצב ה-Switch
        saveUserSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {// עדכון SharedPreferences כשיש שינוי ב-Switch
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_SAVE_USER, isChecked);
            editor.apply();
        });
        // כפתור עזרה
        Button helpButton = findViewById(R.id.helpButton);
        helpButton.setOnClickListener(v -> showHelpDialog());

        helpDialog = new Dialog(this);
        helpDialog.setContentView(R.layout.dialog_help);
        TextView tv = helpDialog.findViewById(R.id.helpText);
        tv.setText("Welcome to Snake! Please log in or register.\n\n" +
                "Login: Enter username/password, press Play.\n" +
                "Register: Press Register, create account, then log in.\n\n" +
                "Game controls use on-screen buttons.");
        Button closeButton = helpDialog.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> helpDialog.dismiss());

        TextView welcomeTextView = findViewById(R.id.welcome);
        TextView snakeTextView = findViewById(R.id.textViewSnake);
        Typeface typeface = getResources().getFont(R.font.fascinaregular);
        welcomeTextView.setTypeface(typeface);
        snakeTextView.setTypeface(typeface);
        snakeTextView.setTextSize(95);
        welcomeTextView.setTextSize(45);

        ImageButton settingsButton = findViewById(R.id.imageButtonSettings);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, settings.class);
            intent.putExtra("CALLING_ACTIVITY", "MainActivity");
            startActivity(intent);
        });
    }

    public void checkUser(View v) {
        String username = etUserName.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        Log.d("CheckUser", "Attempting login for user: " + username);

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Username and password cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }
        loginProgressBar.setVisibility(View.VISIBLE);
        loginStatusText.setText("Checking credentials...");
        loginStatusText.setVisibility(View.VISIBLE);
        playButton.setEnabled(false);
        if (userFileStorage.checkUser(username, password)) { //בדיקה מול אחסון מקומי
            loginStatusText.setText("Login successful!");
            saveLoginToPreferences(username, password);
            new Handler().postDelayed(() -> {
                loginProgressBar.setVisibility(View.GONE);
                loginStatusText.setVisibility(View.GONE);
                playButton.setEnabled(true);
                launchGame();
            }, 500);
            return;
        }
        loginStatusText.setText("Checking cloud database..."); // אם לא נמצא מקומית, בדיקה מול Firebase
        myFBDB.userExistsAsync(username, new MyFBDB.UserExistsCallback() {
            @Override
            public void onResult(boolean exists, Exception error) {
                runOnUiThread(() -> {
                    if (error != null) {
                        Log.e("CheckUser", "Firebase check error: " + error.getMessage());
                        loginStatusText.setText("Error: " + error.getMessage());
                        loginProgressBar.setVisibility(View.GONE);
                        playButton.setEnabled(true);
                        return;
                    }

                    if (exists) {
                        loginStatusText.setText("Verifying password...");
                        myFBDB.checkUserAsync(username, password, new MyFBDB.LoginCallback() {
                            @Override
                            public void onResult(boolean isValid, Exception error) {
                                runOnUiThread(() -> {
                                    if (error != null) {
                                        Log.e("CheckUser", "Firebase password check error: " + error.getMessage());
                                        loginStatusText.setText("Error: " + error.getMessage());
                                        loginProgressBar.setVisibility(View.GONE);
                                        playButton.setEnabled(true);
                                        return;
                                    }
                                    if (isValid) {
                                        Log.d("CheckUser", "Firebase: User credentials valid.");
                                        loginStatusText.setText("Login successful!");
                                        saveLoginToPreferences(username, password);  // העתקת משתמש לאחסון מקומי אם לא קיים שם
                                        try {
                                            User user = new User(username, password, 0, 0, 0);
                                            userFileStorage.saveUser(user);
                                            Log.d("CheckUser", "User copied to local storage");
                                        } catch (Exception e) {
                                            Log.w("CheckUser", "Failed to copy user to local storage: " + e.getMessage());
                                        }
                                        new Handler().postDelayed(() -> {
                                            loginProgressBar.setVisibility(View.GONE);
                                            loginStatusText.setVisibility(View.GONE);
                                            playButton.setEnabled(true);
                                            launchGame();
                                        }, 500);
                                    } else {
                                        Log.w("CheckUser", "Firebase: Invalid password for user: " + username);
                                        loginStatusText.setText("Invalid password");
                                        loginProgressBar.setVisibility(View.GONE);
                                        playButton.setEnabled(true);
                                    }
                                });
                            }
                        });
                    } else {
                        Log.w("CheckUser", "User not found in either local or cloud storage");
                        loginStatusText.setText("User not found. Please register.");
                        loginProgressBar.setVisibility(View.GONE);
                        playButton.setEnabled(true);
                    }
                });
            }
        });
    }

    private void saveLoginToPreferences(String username, String password) {
        SharedPreferences prefs = getSharedPreferences(AUTH_PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_LAST_USERNAME, username);
        editor.putString(KEY_LAST_PASSWORD, password);
        editor.apply();
    }

    private void launchGame() {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("USERNAME", etUserName.getText().toString().trim()); //מעבירה את שם המשתמש
        startActivity(intent);
    }

    public void toRegister(View v) {
        Intent intent = new Intent(this, RegActivityFile.class);
        startActivity(intent);
    }

    public void showHelpDialog() {
        if (helpDialog != null) {
            helpDialog.show();
        }
    }

    // הפעלת שירות המוזיקה
    private void startMusicService() {
        Intent musicServiceIntent = new Intent(this, BackgroundMusicService.class);
        startService(musicServiceIntent);
    }
}
