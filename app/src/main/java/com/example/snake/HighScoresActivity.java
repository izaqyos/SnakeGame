package com.example.snake; // Ensure this matches your package name

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HighScoresActivity extends AppCompatActivity {

    private static final String TAG = "HighScoresActivity";
    private static final String AUTH_PREFS_NAME = "SnakeAuthPrefs";
    private static final String KEY_LAST_USERNAME = "last_username";


    private RecyclerView recyclerViewHighScores;
    private ScoreAdapter scoreAdapter;
    private List<User> userScoreList;
    private TextView tvNoScores;
    private MyFBDB myFBDB;
    private String currentUsername; // This should be populated from the Intent

    private MyFBDB.DataLoadListener fbDataListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        currentUsername = getIntent().getStringExtra("USERNAME");   // קבלת שם המשתמש מה-Intent
        if (currentUsername != null && !currentUsername.isEmpty()) {
            Log.i(TAG, "Username received from Intent: '" + currentUsername + "'");
        } else {
            Log.w(TAG, "No USERNAME passed in Intent to HighScoresActivity. currentUsername is: '" + currentUsername + "'");
        }

        TextView titleTextView = findViewById(R.id.HighScoresTitle);   // אתחול TextView לכותרת עם פונט מותאם אישית
        Typeface typeface = getResources().getFont(R.font.fascinaregular);
        titleTextView.setTypeface(typeface);
        titleTextView.setTextSize(95);

        myFBDB = new MyFBDB(); // אתחול הגישה ל-Firebase
        recyclerViewHighScores = findViewById(R.id.recyclerViewHighScores);
        tvNoScores = findViewById(R.id.tvNoScores); //כשאין שיאים
        ImageButton buttonBack = findViewById(R.id.backToGame);

        ImageButton settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(HighScoresActivity.this, settings.class);
            intent.putExtra("CALLING_ACTIVITY", "MainActivity");
            startActivity(intent);
        });

        if (buttonBack != null) {
            buttonBack.setOnClickListener(v -> {
                Log.d(TAG, "Back button clicked. Attempting to save username and navigate to MainActivity.");
                if (currentUsername != null && !currentUsername.isEmpty()) {
                    Log.i(TAG, "Attempting to save username: '" + currentUsername + "' to SharedPreferences.");
                    SharedPreferences prefs = getSharedPreferences(AUTH_PREFS_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(KEY_LAST_USERNAME, currentUsername);
                    boolean commitSuccessful = editor.commit();
                    if (commitSuccessful) {
                        Log.i(TAG, "Successfully saved last username: '" + currentUsername + "' using SharedPreferences name: '" + AUTH_PREFS_NAME + "' and key: '" + KEY_LAST_USERNAME + "'.");
                    } else {
                        Log.e(TAG, "Failed to save username to SharedPreferences!");
                    }
                } else {
                    Log.w(TAG, "Current username is null or empty when back button clicked. Not saving to SharedPreferences.");
                }
                Intent intent = new Intent(HighScoresActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            });
        } else {
            Log.e(TAG, "Back button (backToGame) is null. Check ID R.id.backToGame in XML.");
        }

        if (recyclerViewHighScores != null) {
            recyclerViewHighScores.setLayoutManager(new LinearLayoutManager(this));
            userScoreList = new ArrayList<>(); // אתחול רשימה ריקה עבור ה-Adapter
            scoreAdapter = new ScoreAdapter(userScoreList, this.currentUsername); // טעינת שם המשתמש
            recyclerViewHighScores.setAdapter(scoreAdapter);
        } else {
            Log.e(TAG, "recyclerViewHighScores is null. Check ID in XML.");
        }

        loadFirebaseHighScores(); //טעינת הנתונים
    }

    private void loadFirebaseHighScores() { //מיון קבלת השיאים
        Log.d(TAG, "loadFirebaseHighScores: Attempting to fetch scores from Firebase...");
        if (tvNoScores != null) tvNoScores.setVisibility(View.GONE); //טיפול בשגיאות
        if (recyclerViewHighScores != null) recyclerViewHighScores.setVisibility(View.VISIBLE);
        if (myFBDB == null) {
            Log.e(TAG, "MyFBDB instance is null! Cannot load scores.");
            if (tvNoScores != null) {
                tvNoScores.setText("Error: Database connection failed.");
                tvNoScores.setVisibility(View.VISIBLE);
                if (recyclerViewHighScores != null) recyclerViewHighScores.setVisibility(View.GONE);
            }
            return;
        }
        fbDataListener = new MyFBDB.DataLoadListener() {
            @Override
            public void onDataLoaded() {
                List<User> fetchedUsers = myFBDB.getUsersArrayList();
                if (fetchedUsers == null) {
                    Log.e(TAG, "myFBDB.getUsersArrayList() returned null after data load!");
                    handleNoScores("Data structure error from DB.");
                    return;
                }
                List<User> allUsers = new ArrayList<>(fetchedUsers);
                if (allUsers.isEmpty()) {
                    handleNoScores("No high scores yet!");
                } else {     // מיון המשתמשים לפי ניקוד בסדר יורד
                    Collections.sort(allUsers, (u1, u2) -> Integer.compare(u2.getScore(), u1.getScore()));
                    userScoreList.clear();
                    userScoreList.addAll(allUsers);
                    if (scoreAdapter != null) scoreAdapter.notifyDataSetChanged();// עדכון ה-RecyclerView
                    if (tvNoScores != null) tvNoScores.setVisibility(View.GONE);
                    if (recyclerViewHighScores != null) recyclerViewHighScores.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onDataLoadError(DatabaseError error) {
                Log.e(TAG, "Firebase data load error: " + error.getMessage());
                handleNoScores("Failed to load scores: " + error.toException().getMessage());
            }
        };
        myFBDB.addDataLoadListener(fbDataListener); // הרשמה למאזין
    }

    private void handleNoScores(String message) {
        if (tvNoScores != null) {
            tvNoScores.setText(message);
            tvNoScores.setVisibility(View.VISIBLE);
        }
        if (recyclerViewHighScores != null) recyclerViewHighScores.setVisibility(View.GONE);
        if (userScoreList != null) userScoreList.clear();
        if (scoreAdapter != null) scoreAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Activity resumed.");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Activity being destroyed.");
        if (myFBDB != null && fbDataListener != null) {
            myFBDB.removeDataLoadListener(fbDataListener);
            Log.d(TAG, "Removed fbDataListener from MyFBDB.");
        }
    }
}
