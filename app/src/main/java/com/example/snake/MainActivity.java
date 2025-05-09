package com.example.snake;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private UserFileStorage userFileStorage;
    private MyFBDB myFBDB; // Kept for potential future use, though inactive
    private Dialog helpDialog;
    private EditText etUserName, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Use the restored login layout

        userFileStorage = new UserFileStorage(this);
        myFBDB = new MyFBDB();
        // myFBDB.ensureTestUserExists(); // Maybe remove if not testing FB path

        etUserName = findViewById(R.id.etUserName);
        etPassword = findViewById(R.id.etPassword);

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
                startActivity(intent);
                finish();
            }
        });
    }

    // Method called by Play button's onClick
    public void checkUser(View v) {
        // ------ TEMPORARY BYPASS FOR TESTING ------
        Log.d("CheckUser", "Bypassing login, starting game as testuser...");
        // Ensure testuser exists (or create it if needed)
        if (!userFileStorage.userExists("testuser")) {
             userFileStorage.saveUser(new User("testuser", "password", 0, 0, 0));
             Log.i("CheckUser", "Created testuser for bypass.");
        }
        Intent intent = new Intent(this, GameActivity.class);
        // intent.putExtra("USERNAME", "testuser"); // Pass username if needed
        startActivity(intent);
        // finish(); // Optional: finish MainActivity
        return; // Skip the actual credential check below
        // ------ END TEMPORARY BYPASS ------

        /*  // Original Login Logic (commented out)
        String un = etUserName.getText().toString().trim();
        String pw = etPassword.getText().toString().trim();
        Log.d("CheckUser", "Attempting login for user: " + un);

        if (un.isEmpty() || pw.isEmpty()) {
            Toast.makeText(this, "Username and password cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use file storage check (primary method now)
        if (userFileStorage.checkUser(un, pw)) {
            Log.d("CheckUser", "FileStorage: User credentials valid.");
            Intent intent = new Intent(this, GameActivity.class);
            // Optional: Pass username to GameActivity if needed later
            // intent.putExtra("USERNAME", un);
            startActivity(intent);
            // finish(); // Optional: finish MainActivity so back button doesn't return here?
        } else {
            Log.w("CheckUser", "FileStorage: Invalid credentials for user: " + un);
            Toast.makeText(this, "Invalid username or password.", Toast.LENGTH_LONG).show();
        }

        // Firebase Check (inactive - keep for reference)
        // if(myFBDB.checkUser(un, pw)){ ... }
        */
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