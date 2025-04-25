package com.example.snake;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.app.Dialog;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private UserFileStorage userFileStorage;
    private MyFBDB myFBDB;
    private Dialog helpDialog;

    public void checkUser(View v){
        // Removed reading from EditText fields
        // EditText etUserName = findViewById(R.id.etUserName);
        // EditText etPassword = findViewById(R.id.etPassword);
        // String un = etUserName.getText().toString().trim();
        // String pw = etPassword.getText().toString().trim();

        // Hardcode test user credentials
        String un = "testuser";
        String pw = "abc123";
        Log.d("CheckUser", "Attempting login for hardcoded user: " + un);

        // Removed check for empty username/password as it's hardcoded now
        // if (un.isEmpty() || pw.isEmpty()) { ... }

        // Check hardcoded credentials using file storage
        if(userFileStorage.checkUser(un, pw)){
            Log.d("CheckUser", "FileStorage: Hardcoded test user credentials valid.");
            Intent intent =  new Intent(this, level1.class);
            startActivity(intent);
        } else {
            // This case should not happen if testuser was created correctly
            Log.e("CheckUser", "FileStorage: Hardcoded test user credentials INVALID! Check UserFileStorage.");
            Toast.makeText(this, "Test user login failed!", Toast.LENGTH_LONG).show();
        }

        /* Firebase Check (commented out - keep for reference)
        // if(myFBDB.checkUser(un, pw)){ ... }
        */
    }

    public void toRegister(View v){
        Intent intent = new Intent(this, RegActivityFile.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userFileStorage = new UserFileStorage(this);
        myFBDB = new MyFBDB();
        myFBDB.ensureTestUserExists();

        Button helpButton = findViewById(R.id.helpButton);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHelpDialog();
            }
        });

        helpDialog = new Dialog(this);
        helpDialog.setContentView(R.layout.dialog_help);
        TextView tv  = helpDialog.findViewById(R.id.helpText);
        tv.setText("Welcome to 'Snake!' Before you can dive into the slithering action, you'll need to create an account or log in.\n\n" +
                "Login/Registration:\n\n" +
                "New Users: If you don't have an account yet, tap the 'Register' button. You'll be prompted to enter a username and password. Once registered, you can use these credentials to log in.\n\n" +
                "Existing Users: If you already have an account, enter your username and password into the provided fields, and then tap the 'Play' button to begin.\n\n" +
                "HOW TO PLAY: Snake!\n\n" +
                "'Snake!' is a fast-paced, addictive game of reflexes and strategy. You control a growing snake within an enclosed arena. Your goal is to eat as many apples as possible to increase your snake's length and score.\n\n" +
                "Gameplay Mechanics:\n\n" +
                "Movement: Control the snake's direction using on-screen arrow buttons.\n" +
                "Eating Apples: Guide the snake to consume apples that appear randomly within the arena. Each apple eaten increases the snake's length.\n" +
                "Avoid Collisions: The snake must not collide with the walls of the arena or with its own body. A collision results in game over.\n\n" +
                "Scoring: Your score is determined by the number of apples eaten. The longer the snake, the higher the score.\n\n" +
                "Have fun and see how long you can make your snake grow!");

        Button closeButton = helpDialog.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helpDialog.dismiss();
            }
        });
    }

    private void showHelpDialog() {
        if (helpDialog != null) {
            helpDialog.show();
        }
    }
}