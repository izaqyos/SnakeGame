package com.example.snake;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // Import Log
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

// Note: Removed EdgeToEdge, Insets, ViewCompat, WindowInsetsCompat imports if not needed

public class RegActivityFB extends AppCompatActivity { // Changed class name

    private EditText usernameEditText, passwordEditText;
    private Button registerButton;
    private TextView errorTextView;
    // private UserFileStorage userFileStorage; // Removed File Storage
    private MyFBDB myFBDB; // Keep Firebase DB variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set content view to the new FB layout
        setContentView(R.layout.activity_reg_fb); // Changed layout reference

        usernameEditText = findViewById(R.id.registerUsernameEditText); // Assuming IDs are same
        passwordEditText = findViewById(R.id.registerPasswordEditText); // Assuming IDs are same
        registerButton = findViewById(R.id.registerConfirmButton);    // Assuming IDs are same
        errorTextView = findViewById(R.id.registerErrorTextView);      // Assuming IDs are same
        // userFileStorage = new UserFileStorage(this); // Removed File Storage initialization
        myFBDB = new MyFBDB(); // Initialize Firebase DB

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

                User newUser = new User(username, password, 0, 0, 0);

                /* File Storage Check (Commented out/Removed)
                if (userFileStorage.userExists(username)) {
                    errorTextView.setText("Username already exists. Please choose another.");
                    errorTextView.setVisibility(View.VISIBLE);
                    return;
                }
                userFileStorage.saveUser(newUser);
                */

                // Firebase Check & Save (Active implementation)
                if (myFBDB.userExists(newUser)) { // Check against Firebase
                    Log.d("RegActivityFB", "Firebase: User already exists: " + username);
                    errorTextView.setText("Username already exists. Please choose another.");
                    errorTextView.setVisibility(View.VISIBLE);
                    return;
                }
                Log.d("RegActivityFB", "Firebase: Saving new user: " + username);
                myFBDB.saveUser(newUser); // Save to Firebase

                Toast.makeText(RegActivityFB.this, "Registration successful!", Toast.LENGTH_SHORT).show(); // Changed context
                finish();
            }
        });
    }

    // toPlay method might not be needed or should go elsewhere depending on flow
    /*
    public void toPlay(View v) {
        Intent intent = new Intent(RegActivityFB.this, level1.class);
        startActivity(intent);
        finish();
    }
    */

} 