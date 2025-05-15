package com.example.snake;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // ודאי שהייבוא הזה קיים
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegActivityFile extends AppCompatActivity {

    private static final String TAG = "RegActivityFile"; // תג ללוגים

    private EditText usernameEditText, passwordEditText;
    private Button registerButton;
    private TextView errorTextView;
    private UserFileStorage userFileStorage;
    private MyFBDB myFBDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg); // ודאי שזהו שם קובץ ה-Layout הנכון שלך

        ImageButton backButton = findViewById(R.id.backButton2); // ודאי שה-ID נכון
        if (backButton != null) {
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // חזרה למסך הראשי מבלי לבצע רישום
                    navigateToMainActivity();
                }
            });
        }

        usernameEditText = findViewById(R.id.registerUsernameEditText);
        passwordEditText = findViewById(R.id.registerPasswordEditText);
        // אם יש שדה אימות סיסמה ב-XML:
        // confirmPasswordEditText = findViewById(R.id.yourConfirmPasswordEditTextId);
        registerButton = findViewById(R.id.registerConfirmButton);
        errorTextView = findViewById(R.id.registerErrorTextView);

        userFileStorage = new UserFileStorage(this);
        myFBDB = new MyFBDB(); // אתחול MyFBDB

        if (registerButton != null) {
            registerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Register button clicked");
                    String username = usernameEditText.getText().toString().trim();
                    String password = passwordEditText.getText().toString().trim();
                    // String confirmPassword = ""; // אם יש שדה אימות
                    // if (confirmPasswordEditText != null) {
                    //     confirmPassword = confirmPasswordEditText.getText().toString().trim();
                    // }

                    // בדיקות תקינות קלט
                    if (username.isEmpty() || password.isEmpty()) {
                        errorTextView.setText("שם משתמש וסיסמה לא יכולים להיות ריקים.");
                        errorTextView.setVisibility(View.VISIBLE);
                        return;
                    }
                    // אם יש שדה אימות סיסמה:
                    // if (!password.equals(confirmPassword)) {
                    //    errorTextView.setText("הסיסמאות אינן תואמות.");
                    //    errorTextView.setVisibility(View.VISIBLE);
                    //    return;
                    // }
                    // אפשר להוסיף בדיקות נוספות (אורך מינימלי וכו')

                    errorTextView.setVisibility(View.GONE); // הסתרת הודעת שגיאה קודמת
                    registerButton.setEnabled(false); // השבתת הכפתור למניעת לחיצות כפולות

                    // 1. בדיקה אם שם המשתמש קיים באחסון המקומי
                    if (userFileStorage.userExists(username)) {
                        Log.w(TAG, "Username '" + username + "' already exists locally.");
                        errorTextView.setText("שם המשתמש כבר קיים במערכת המקומית. אנא בחר שם אחר.");
                        errorTextView.setVisibility(View.VISIBLE);
                        registerButton.setEnabled(true);
                        return;
                    }

                    // 2. בדיקה אם שם המשתמש קיים ב-Firebase (אסינכרוני)
                    myFBDB.userExistsAsync(username, new MyFBDB.UserExistsCallback() {
                        @Override
                        public void onResult(boolean existsInFirebase, Exception error) {
                            runOnUiThread(() -> { // ודא שהקוד רץ על ה-UI Thread
                                if (error != null) {
                                    Log.e(TAG, "Error checking username in Firebase: " + error.getMessage());
                                    errorTextView.setText("שגיאה בבדיקת שם המשתמש בענן: " + error.getMessage());
                                    errorTextView.setVisibility(View.VISIBLE);
                                    registerButton.setEnabled(true);
                                    return;
                                }

                                if (existsInFirebase) {
                                    Log.w(TAG, "Username '" + username + "' already exists in Firebase.");
                                    errorTextView.setText("שם המשתמש כבר קיים במערכת הענן. אנא בחר שם אחר.");
                                    errorTextView.setVisibility(View.VISIBLE);
                                    registerButton.setEnabled(true);
                                    return;
                                }

                                // אם שם המשתמש פנוי בשני המקומות - בצע רישום
                                Log.i(TAG, "Username '" + username + "' is available. Proceeding with registration.");
                                User newUser = new User(username, password, 0, 0, 0); // נתוני ברירת מחדל למשתמש חדש

                                try {
                                    userFileStorage.saveUser(newUser); // שמירה לקובץ מקומי
                                    Log.i(TAG, "User '" + username + "' saved to local file storage.");

                                    myFBDB.saveUser(newUser); // שמירה ל-Firebase
                                    Log.i(TAG, "User '" + username + "' saved to Firebase.");

                                    Toast.makeText(RegActivityFile.this, "ההרשמה הצליחה!", Toast.LENGTH_LONG).show();
                                    navigateToMainActivity(); // ניווט למסך הראשי

                                } catch (Exception e) {
                                    Log.e(TAG, "Error saving user during registration: " + e.getMessage());
                                    errorTextView.setText("שגיאה בשמירת המשתמש: " + e.getMessage());
                                    errorTextView.setVisibility(View.VISIBLE);
                                    registerButton.setEnabled(true);
                                }
                            });
                        }
                    });
                }
            });
        } else {
            Log.e(TAG, "Register button is null. Check R.id.registerConfirmButton in XML.");
        }
    }

    private void navigateToMainActivity() {
        Log.i(TAG, "Navigating to MainActivity and finishing RegActivityFile.");
        Intent intent = new Intent(RegActivityFile.this, MainActivity.class);
        // דגלים אלו מנקים את מחסנית ה-Activities מעל MainActivity ומפעילים אותו מחדש
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // סגירת מסך הרישום
    }
}
