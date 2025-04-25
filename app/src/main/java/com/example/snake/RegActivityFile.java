package com.example.snake;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegActivityFile extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private Button registerButton;
    private TextView errorTextView;
    private UserFileStorage userFileStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);

        usernameEditText = findViewById(R.id.registerUsernameEditText);
        passwordEditText = findViewById(R.id.registerPasswordEditText);
        registerButton = findViewById(R.id.registerConfirmButton);
        errorTextView = findViewById(R.id.registerErrorTextView);
        userFileStorage = new UserFileStorage(this);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    errorTextView.setText("Username and password cannot be empty.");
                    errorTextView.setVisibility(View.VISIBLE);
                    return;
                }

                if (userFileStorage.userExists(username)) {
                    errorTextView.setText("Username already exists. Please choose another.");
                    errorTextView.setVisibility(View.VISIBLE);
                    return;
                }
                User newUser = new User(username, password, 0, 0, 0);
                userFileStorage.saveUser(newUser);

                Toast.makeText(RegActivityFile.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                // Intent intent = new Intent(RegActivityFile.this, level1.class);
                // startActivity(intent);
                finish(); // Close the registration activity
            }
        });
    }
    // public void toPlay(View v) { // This method is likely unused and can be removed
    //    Intent intent = new Intent(RegActivityFile.this, level1.class);
    //    startActivity(intent);
    //    finish();
    // }
}