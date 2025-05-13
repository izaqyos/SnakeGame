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
    // Define SharedPreferences constants - ensure these are IDENTICAL in MainActivity
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

        Log.d(TAG, "onCreate: Activity created.");

        // Get current username from the Intent that started this Activity
        currentUsername = getIntent().getStringExtra("USERNAME");
        if (currentUsername != null && !currentUsername.isEmpty()) {
            Log.i(TAG, "Username received from Intent: " + currentUsername);
        } else {
            Log.w(TAG, "No USERNAME passed in Intent to HighScoresActivity. Cannot save last username if it's unknown.");
            // currentUsername will be null or empty, affecting save logic
        }

        TextView titleTextView = findViewById(R.id.HighScoresTitle);
        Typeface typeface = getResources().getFont(R.font.fascinaregular);
        titleTextView.setTypeface(typeface);
        titleTextView.setTextSize(95);

        myFBDB = new MyFBDB();

        recyclerViewHighScores = findViewById(R.id.recyclerViewHighScores);
        tvNoScores = findViewById(R.id.tvNoScores);
        ImageButton buttonBack = findViewById(R.id.backToGame); // ID from your activity_high_scores.xml

        if (buttonBack != null) {
            buttonBack.setOnClickListener(v -> {
                // Save the current username to SharedPreferences
                if (currentUsername != null && !currentUsername.isEmpty()) {
                    Log.i(TAG, "Attempting to save username: '" + currentUsername + "' to SharedPreferences.");
                    SharedPreferences prefs = getSharedPreferences(AUTH_PREFS_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(KEY_LAST_USERNAME, currentUsername);
                    boolean commitSuccessful = editor.commit(); // Using commit() for immediate result for debugging
                    if (commitSuccessful) {
                        Log.i(TAG, "Successfully saved last username: '" + currentUsername + "' using SharedPreferences name: '" + AUTH_PREFS_NAME + "' and key: '" + KEY_LAST_USERNAME + "'.");
                    } else {
                        Log.e(TAG, "Failed to save username to SharedPreferences!");
                    }
                } else {
                    Log.w(TAG, "Current username is null or empty. Not saving to SharedPreferences.");
                }

                // Intent to go back to MainActivity
                Intent intent = new Intent(HighScoresActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish(); // Close HighScoresActivity
            });
        } else {
            Log.e(TAG, "Back button (backToGame) is null. Check ID R.id.backToGame in XML.");
        }

        if (recyclerViewHighScores != null) {
            recyclerViewHighScores.setLayoutManager(new LinearLayoutManager(this));
            userScoreList = new ArrayList<>();
            // Pass currentUsername to adapter for potential highlighting
            scoreAdapter = new ScoreAdapter(userScoreList, currentUsername);
            recyclerViewHighScores.setAdapter(scoreAdapter);
        } else {
            Log.e(TAG, "recyclerViewHighScores is null. Check ID in XML.");
        }

        loadFirebaseHighScores();

        ImageButton settingsButton = findViewById(R.id.settingsButton);
        if (settingsButton != null) {
            settingsButton.setOnClickListener(v -> {
                Intent intent = new Intent(HighScoresActivity.this, settings.class);
                startActivity(intent);
                finish();
            });
        }
    }

    private void loadFirebaseHighScores() {
        Log.d(TAG, "loadFirebaseHighScores: Attempting to fetch scores from Firebase...");
        if (tvNoScores != null) {
            tvNoScores.setVisibility(View.GONE);
        }
        if (recyclerViewHighScores != null) {
            recyclerViewHighScores.setVisibility(View.VISIBLE);
        }

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
                Log.d(TAG, "Firebase data loaded via listener in HighScoresActivity.");
                List<User> fetchedUsers = myFBDB.getUsersArrayList();

                if (fetchedUsers == null) {
                    Log.e(TAG, "myFBDB.getUsersArrayList() returned null after data load!");
                    handleNoScores("Data structure error from DB.");
                    return;
                }
                List<User> allUsers = new ArrayList<>(fetchedUsers);

                if (allUsers.isEmpty()) {
                    Log.d(TAG, "No users found in Firebase data.");
                    handleNoScores("No high scores yet!");
                } else {
                    Collections.sort(allUsers, (u1, u2) -> Integer.compare(u2.getScore(), u1.getScore()));
                    userScoreList.clear();
                    userScoreList.addAll(allUsers);
                    if (scoreAdapter != null) {
                        scoreAdapter.notifyDataSetChanged();
                    }
                    if (tvNoScores != null) tvNoScores.setVisibility(View.GONE);
                    if (recyclerViewHighScores != null) recyclerViewHighScores.setVisibility(View.VISIBLE);
                    Log.d(TAG, "Displaying " + allUsers.size() + " scores.");
                }
            }

            @Override
            public void onDataLoadError(DatabaseError error) {
                Log.e(TAG, "Firebase data load error: " + error.getMessage());
                handleNoScores("Failed to load scores: " + error.toException().getMessage());
            }
        };

        myFBDB.addDataLoadListener(fbDataListener);

        List<User> initialUsers = myFBDB.getUsersArrayList();
        if (initialUsers != null && !initialUsers.isEmpty() && (scoreAdapter != null && scoreAdapter.getItemCount() == 0) ) {
            Log.d(TAG, "Initial data already present in MyFBDB, processing now.");
            fbDataListener.onDataLoaded();
        } else if (initialUsers != null && initialUsers.isEmpty()) {
            Log.d(TAG, "MyFBDB.getUsersArrayList() is initialized but empty. Waiting for listener.");
        } else if (initialUsers == null) {
            Log.d(TAG, "MyFBDB.getUsersArrayList() returned null initially. Waiting for listener.");
        }
    }

    private void handleNoScores(String message) {
        if (tvNoScores != null) {
            tvNoScores.setText(message);
            tvNoScores.setVisibility(View.VISIBLE);
        }
        if (recyclerViewHighScores != null) {
            recyclerViewHighScores.setVisibility(View.GONE);
        }
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
