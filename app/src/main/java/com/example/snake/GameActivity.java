package com.example.snake;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class GameActivity extends AppCompatActivity {

    private static final String UI_TAG = "GameActivity_UI";
    private static final String UI_INIT_TAG = "GameActivity_UI_Init";
    private static final String NAV_TAG = "GameActivity_Nav"; // Navigation Log Tag

    private GameManager gameManager;
    private TextView scoreTextView;
    private TextView usernameTextView;
    private TextView personalHighScoreTextView;
    private TextView globalHighScoreTextView;
    private FrameLayout gameSurfaceContainer;
    private Button restartButton;
    private Button highScoresButton;
    private String currentUsername;
    private int personalHighScore = 0;
    private MyFBDB myFBDB;

    private final ActivityResultLauncher<Intent> settingsLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                Log.d(NAV_TAG, "Returned from settings.class");
                if (gameManager != null) {
                    gameManager.refreshAppearance();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        myFBDB = new MyFBDB(); //אתחול גישה לפיירבייס

        scoreTextView = findViewById(R.id.scoreTextView);
        usernameTextView = findViewById(R.id.usernameTextView);
        personalHighScoreTextView = findViewById(R.id.personalHighScoreTextView);
        globalHighScoreTextView = findViewById(R.id.globalHighScoreTextView);
        gameSurfaceContainer = findViewById(R.id.gameSurfaceContainer);
        restartButton = findViewById(R.id.restartButton);
        highScoresButton = findViewById(R.id.highScoresButton);

        currentUsername = getIntent().getStringExtra("USERNAME"); //העברת שם המשתמש
        if (currentUsername != null && !currentUsername.isEmpty()) {
            if (usernameTextView != null) usernameTextView.setText(currentUsername);
            Log.i(NAV_TAG, "GameActivity onCreate: currentUsername received: " + currentUsername);
        } else {
            currentUsername = "Player"; // Default
            if (usernameTextView != null) usernameTextView.setText(currentUsername);
            Log.w(NAV_TAG, "GameActivity onCreate: Username not passed via Intent, using default 'Player'");
        }

        loadHighScores(); //טעינה ראשונית של שיאים

        gameManager = new GameManager(this, this, currentUsername, myFBDB);    // יצירת GameManager והוספתו ל-Layout
        if (gameSurfaceContainer != null) {
            gameSurfaceContainer.addView(gameManager);
        } else {
            Log.e("GameActivity", "gameSurfaceContainer is null! Cannot add GameManager.");
            finish();
            return;
        }
//הגדרת מאזינים לכפתורים האחרים
        ImageButton buttonUp = findViewById(R.id.buttonUp);
        ImageButton buttonDown = findViewById(R.id.buttonDown);
        ImageButton buttonLeft = findViewById(R.id.buttonLeft);
        ImageButton buttonRight = findViewById(R.id.buttonRight);
        ImageButton backButton = findViewById(R.id.backButton);
        ImageButton settingsButton = findViewById(R.id.buttonSettings2);
        //כפתורי שליטה בתנועת הנחש
        if (buttonUp != null) buttonUp.setOnClickListener(v -> gameManager.setDirection(GameManager.Direction.UP));
        if (buttonDown != null) buttonDown.setOnClickListener(v -> gameManager.setDirection(GameManager.Direction.DOWN));
        if (buttonLeft != null) buttonLeft.setOnClickListener(v -> gameManager.setDirection(GameManager.Direction.LEFT));
        if (buttonRight != null) buttonRight.setOnClickListener(v -> gameManager.setDirection(GameManager.Direction.RIGHT));

        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                Log.d(NAV_TAG, "Back button to MainActivity clicked.");
                navigateToMainActivity();
            });
        }

        if (restartButton != null) {
            restartButton.setOnClickListener(v -> {
                Log.d(UI_TAG, "Restart button clicked.");
                if (gameManager != null) {
                    gameManager.restartGame();
                }
                hideGameOverUI();
            });
        }

        if (highScoresButton != null) { //העברת שם המשתמש למסך שיאים
            highScoresButton.setOnClickListener(v -> {
                Intent intent = new Intent(GameActivity.this, HighScoresActivity.class);
                intent.putExtra("USERNAME", currentUsername); // Pass the username

                Log.i(NAV_TAG, "Intent created. Target: " + (intent.getComponent() != null ? intent.getComponent().getClassName() : "null component"));
                if (intent.hasExtra("USERNAME")) {
                    Log.i(NAV_TAG, "Extras in Intent: USERNAME = " + intent.getStringExtra("USERNAME"));
                } else {
                    Log.w(NAV_TAG, "Extras in Intent: USERNAME key is MISSING!");
                }

                try {
                    Log.i(NAV_TAG, "Attempting to startActivity(intent) for HighScoresActivity...");
                    startActivity(intent);
                    Log.i(NAV_TAG, "startActivity(intent) for HighScoresActivity called successfully.");
                } catch (android.content.ActivityNotFoundException e) {
                    Log.e(NAV_TAG, "CRITICAL: HighScoresActivity not found. Manifest declaration issue?", e);
                    Toast.makeText(GameActivity.this, "High Scores screen error (not found).", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Log.e(NAV_TAG, "CRITICAL: Unexpected error starting HighScoresActivity.", e);
                    Toast.makeText(GameActivity.this, "Error opening High Scores.", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Log.e(NAV_TAG, "highScoresButton is NULL, OnClickListener not set.");
        }

        if (settingsButton != null) {
            settingsButton.setOnClickListener(v -> {
                Log.d(NAV_TAG, "Settings button clicked, launching settings screen.");
                Intent intent = new Intent(GameActivity.this, settings.class);
                settingsLauncher.launch(intent);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("GameActivity", "onResume called");
        if (gameManager != null) {
            gameManager.resume();
            Log.d(UI_TAG, "onResume - isGameOver: " + gameManager.isGameOver());
            if (gameManager.isGameOver()) {
                showGameOverUI();
            } else {
                hideGameOverUI();
            }
            loadHighScores();
        } else {
            Log.w(UI_TAG, "onResume - gameManager is NULL");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("GameActivity", "onPause called");
        if (gameManager != null) {
            gameManager.pause();
        }
    }

    private void loadHighScores() { //מושך את הניקוד הגבוה ביותר של המשתמש הנוכחי ואת הניקוד הגלובלי הגבוה ביותר, ומעדכן את ה-״TextViews״ המתאימים בממשק המשתמש
        if (myFBDB == null || currentUsername == null) {
            Log.e(UI_TAG, "Cannot load high scores: myFBDB or currentUsername is null.");
            if(personalHighScoreTextView!=null) personalHighScoreTextView.setText("Best: N/A");
            if(globalHighScoreTextView!=null) globalHighScoreTextView.setText("Top: N/A");
            return;
        }
        myFBDB.getUserHighScore(currentUsername, new MyFBDB.ScoreCallback() {
            @Override
            public void onResult(int score, Exception error) {
                runOnUiThread(() -> {
                    if (error == null) {
                        personalHighScore = score;
                        if (personalHighScoreTextView != null) personalHighScoreTextView.setText("Best: " + personalHighScore);
                    } else {
                        Log.e(UI_TAG, "Error loading personal high score for " + currentUsername + ": " + error.getMessage());
                        if (personalHighScoreTextView != null) personalHighScoreTextView.setText("Best: Error");
                    }
                });
            }
        });
        myFBDB.getGlobalHighScore(new MyFBDB.ScoreCallback() {
            @Override
            public void onResult(int score, Exception error) {
                runOnUiThread(() -> {
                    if (error == null) {
                        if (globalHighScoreTextView != null) globalHighScoreTextView.setText("Top: " + score);
                    } else {
                        Log.e(UI_TAG, "Error loading global high score: " + error.getMessage());
                        if (globalHighScoreTextView != null) globalHighScoreTextView.setText("Top: Error");
                    }
                });
            }
        });
    }

    public void updateScore(int currentScore) {
        runOnUiThread(() -> {
            if (scoreTextView != null) scoreTextView.setText("Score: " + currentScore);
            if (currentScore > personalHighScore) {
                if (personalHighScoreTextView != null) personalHighScoreTextView.setText("Best: " + currentScore);
            }
        });
    }

    public void showGameOverUI() { //מציגה את כפתורי "הפעלה מחדש" ו"טבלת שיאים"
        Log.d(UI_TAG, "showGameOverUI called");
        runOnUiThread(() -> {
            if (restartButton != null) {
                Log.d(UI_TAG, "Showing restartButton");
                restartButton.setVisibility(View.VISIBLE);
            } else {
                Log.e(UI_TAG, "restartButton is NULL in showGameOverUI");
            }
            if (highScoresButton != null) {
                Log.d(UI_TAG, "Showing highScoresButton");
                highScoresButton.setVisibility(View.VISIBLE);
            } else {
                Log.e(UI_TAG, "highScoresButton is NULL in showGameOverUI");
            }
            loadHighScores();
        });
    }

    private void hideGameOverUI() {//מסתירה את כפתורי "הפעלה מחדש" ו"טבלת שיאים"
        Log.d(UI_TAG, "hideGameOverUI called");
        runOnUiThread(() -> {
            if (restartButton != null) {
                Log.d(UI_TAG, "Hiding restartButton");
                restartButton.setVisibility(View.GONE);
            } else {
                Log.e(UI_TAG, "restartButton is NULL in hideGameOverUI");
            }
            if (highScoresButton != null) {
                Log.d(UI_TAG, "Hiding highScoresButton");
                highScoresButton.setVisibility(View.GONE);
            } else {
                Log.e(UI_TAG, "highScoresButton is NULL in hideGameOverUI");
            }
        });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(GameActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
