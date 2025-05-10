package com.example.snake;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegActivityFile extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private Button registerButton;
    private TextView errorTextView;
    private UserFileStorage userFileStorage;
    private MyFBDB myFBDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);

        ImageButton backButton = findViewById(R.id.backButton2);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegActivityFile.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        usernameEditText = findViewById(R.id.registerUsernameEditText);
        passwordEditText = findViewById(R.id.registerPasswordEditText);
        registerButton = findViewById(R.id.registerConfirmButton);
        errorTextView = findViewById(R.id.registerErrorTextView);
        userFileStorage = new UserFileStorage(this);
        myFBDB = new MyFBDB();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(RegActivityFile.this, "Register button clicked", Toast.LENGTH_SHORT).show();
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    errorTextView.setText("Username and password cannot be empty.");
                    errorTextView.setVisibility(View.VISIBLE);
                    return;
                }

                if (userFileStorage.userExists(username)) {
                    errorTextView.setText("Username already exists locally. Please choose another.");
                    errorTextView.setVisibility(View.VISIBLE);
                    return;
                }
                User newUser = new User(username, password, 0, 0, 0);
                // Use async Firebase check
                registerButton.setEnabled(false);
                myFBDB.userExistsAsync(username, new MyFBDB.UserExistsCallback() {
                    @Override
                    public void onResult(boolean exists, Exception error) {
                        runOnUiThread(() -> {
                            Toast.makeText(RegActivityFile.this, "Firebase callback fired", Toast.LENGTH_SHORT).show();
                            registerButton.setEnabled(true);
                            if (error != null) {
                                errorTextView.setText("Error checking cloud: " + error.getMessage());
                                errorTextView.setVisibility(View.VISIBLE);
                                return;
                            }
                            if (exists) {
                                errorTextView.setText("Username already exists in cloud. Please choose another.");
                                errorTextView.setVisibility(View.VISIBLE);
                                return;
                            }
                            try {
                                userFileStorage.saveUser(newUser);
                                myFBDB.saveUser(newUser);
                                Toast.makeText(RegActivityFile.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                finish(); // Close the registration activity
                            } catch (Exception e) {
                                errorTextView.setText("Error saving user: " + e.getMessage());
                                errorTextView.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });
            }
        });
    }
    // public void toPlay(View v) { // This method is likely unused and can be removed
    //    Intent intent = new Intent(RegActivityFile.this, level1.class);
    //    startActivity(intent);
    //    finish();
    // }
}