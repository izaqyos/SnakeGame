package com.example.snake;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private UserFileStorage userFileStorage;
    private MyFBDB myFBDB; // Kept for potential future use, though inactive
    private Dialog helpDialog;
    private EditText etUserName, etPassword;
    private ProgressBar loginProgressBar;
    private TextView loginStatusText;
    private Button playButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Use the restored login layout

        userFileStorage = new UserFileStorage(this);
        myFBDB = new MyFBDB();
        // myFBDB.ensureTestUserExists(); // Maybe remove if not testing FB path

        etUserName = findViewById(R.id.etUserName);
        etPassword = findViewById(R.id.etPassword);
        loginProgressBar = findViewById(R.id.loginProgressBar);
        loginStatusText = findViewById(R.id.loginStatusText);
        playButton = findViewById(R.id.startGameButton);

        // Setup Help Button and Dialog (Restored)
        Button helpButton = findViewById(R.id.helpButton);
        helpButton.setOnClickListener(v -> showHelpDialog());

        helpDialog = new Dialog(this);
        helpDialog.setContentView(R.layout.dialog_help);
        TextView tv = helpDialog.findViewById(R.id.helpText);
        // Restore or update help text as needed
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

        ImageButton backButton = findViewById(R.id.imageButtonSettings);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, settings.class);
                intent.putExtra("CALLING_ACTIVITY", "MainActivity");
                startActivity(intent);
                // Don't finish MainActivity to avoid login issues
            }
        });
    }

    // Method called by Play button's onClick
    public void checkUser(View v) {
        String username = etUserName.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        Log.d("CheckUser", "Attempting login for user: " + username);

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Username and password cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress and update status
        loginProgressBar.setVisibility(View.VISIBLE);
        loginStatusText.setText("Checking credentials...");
        loginStatusText.setVisibility(View.VISIBLE);
        
        // Disable the login button while checking
        playButton.setEnabled(false);

        // First check local storage
        if (userFileStorage.checkUser(username, password)) {
            Log.d("CheckUser", "FileStorage: User credentials valid.");
            loginStatusText.setText("Login successful!");
            
            // Hide progress after a short delay
            new Handler().postDelayed(() -> {
                loginProgressBar.setVisibility(View.GONE);
                loginStatusText.setVisibility(View.GONE);
                playButton.setEnabled(true);
                launchGame();
            }, 500);
            return;
        }

        // Update status for Firebase check
        loginStatusText.setText("Checking cloud database...");
        
        // Check Firebase for the user
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
                        // User exists, now check password
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
                                        
                                        // Copy user to local storage for future logins
                                        try {
                                            User user = new User(username, password, 0, 0, 0);
                                            userFileStorage.saveUser(user);
                                            Log.d("CheckUser", "User copied to local storage");
                                        } catch (Exception e) {
                                            Log.w("CheckUser", "Failed to copy user to local storage: " + e.getMessage());
                                        }
                                        
                                        // Hide progress after a short delay
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
    
    // Helper method to launch the game
    private void launchGame() {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("USERNAME", etUserName.getText().toString().trim());
        startActivity(intent);
    }

    // Method called by Register button's onClick
    public void toRegister(View v) {
        Intent intent = new Intent(this, RegActivityFile.class);
        startActivity(intent);
    }

    // Method called by Help button's onClick
    public void showHelpDialog() {
        if (helpDialog != null) {
            helpDialog.show();
        }
    }
}