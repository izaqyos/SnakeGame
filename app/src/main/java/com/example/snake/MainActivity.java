package com.example.snake;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
    }

    // Method called by Play button's onClick
    public void checkUser(View v) {
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
            // ** Start GameActivity instead of level1 **
            Intent intent = new Intent(this, GameActivity.class);
            // Optional: Pass username to GameActivity if needed later
            // intent.putExtra("USERNAME", un);
            startActivity(intent);
            // finish(); // Optional: finish MainActivity so back button doesn't return here?
        } else {
            Log.w("CheckUser", "FileStorage: Invalid credentials for user: " + un);
            Toast.makeText(this, "Invalid username or password.", Toast.LENGTH_LONG).show();
        }

        /* Firebase Check (inactive - keep for reference)
        if(myFBDB.checkUser(un, pw)){
             Intent intent = new Intent(this, GameActivity.class); // Also start GameActivity if using FB
             startActivity(intent);
        }
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