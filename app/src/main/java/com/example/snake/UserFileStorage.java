package com.example.snake;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class UserFileStorage {

    private static final String TAG = "UserFileStorage";
    private static final String FILENAME = "users.json";
    private Context context;
    private List<User> usersList;

    public UserFileStorage(Context context) {
        Log.i(TAG, "UserFileStorage instance created.");
        this.context = context;
        Log.d(TAG, "Loading users from file...");
        this.usersList = loadUsersFromFile();
        Log.d(TAG, "Initializing test user check...");
        initializeTestUser();
        Log.i(TAG, "UserFileStorage initialization complete. Users loaded: " + usersList.size());
    }

    private void initializeTestUser() {
        boolean exists = userExists("testuser"); // Check first
        if (!exists) {
            Log.i(TAG, "InitializeTestUser: Test user NOT found, attempting to create.");
            User testUser = new User("testuser", "abc123", 0, 0, 0);
            testUser.setKey("testkey"); // Assign a key
            saveUser(testUser); // This will also save the updated list to the file
        } else {
             Log.i(TAG, "InitializeTestUser: Test user already exists.");
        }
    }

    private List<User> loadUsersFromFile() {
        Log.d(TAG, "loadUsersFromFile: Attempting to load from " + FILENAME);
        List<User> users = new ArrayList<>();
        File file = new File(context.getFilesDir(), FILENAME);
        if (!file.exists()) {
             Log.w(TAG, "loadUsersFromFile: File does not exist. Returning empty list.");
             return users;
        }

        FileInputStream fis = null;
        String jsonString = null;
        try {
            fis = context.openFileInput(FILENAME);
            InputStreamReader inputStreamReader = new InputStreamReader(fis, StandardCharsets.UTF_8);
            StringBuilder stringBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                String line = reader.readLine();
                while (line != null) {
                    stringBuilder.append(line);
                    line = reader.readLine();
                }
            }
            jsonString = stringBuilder.toString();
            Log.d(TAG, "loadUsersFromFile: Read raw JSON string: " + jsonString);

            if (jsonString.trim().isEmpty()) {
                Log.w(TAG, "loadUsersFromFile: File is empty. Returning empty list.");
                return users;
            }

            JSONArray jsonArray = new JSONArray(jsonString);
            Log.d(TAG, "loadUsersFromFile: Parsed JSON array with length: " + jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject userJson = jsonArray.getJSONObject(i);
                User user = new User();
                user.setKey(userJson.optString("key", "user_" + i)); // Use optString
                user.setUserName(userJson.optString("userName", null));
                user.setPassword(userJson.optString("password", null));
                user.setLevel(userJson.optInt("level", 0));
                user.setScore(userJson.optInt("score", 0));
                user.setCoins(userJson.optInt("coins", 0));
                 if (user.getUserName() == null || user.getPassword() == null) {
                    Log.e(TAG, "loadUsersFromFile: Loaded user with null username/password at index " + i + ", skipping.");
                    continue;
                 }
                users.add(user);
                Log.d(TAG, "loadUsersFromFile: Successfully loaded user: " + user.getUserName());
            }
        } catch (IOException e) {
             Log.e(TAG, "loadUsersFromFile: IOException reading file.", e);
             users.clear(); // Clear list on error
        } catch (JSONException e) {
            Log.e(TAG, "loadUsersFromFile: JSONException parsing string: " + jsonString, e);
            users.clear(); // Clear list on error
        } finally {
            if (fis != null) {
                try { fis.close(); } catch (IOException e) { Log.e(TAG, "Error closing FIS", e); }
            }
        }
         Log.d(TAG, "loadUsersFromFile: Finished. Loaded users count: " + users.size());
        return users;
    }

    private void saveUsersToFile() {
        Log.d(TAG, "saveUsersToFile: Attempting to save " + usersList.size() + " users.");
        JSONArray jsonArray = new JSONArray();
        for (User user : usersList) {
            // Basic check to avoid saving incomplete users
            if (user.getUserName() == null || user.getPassword() == null || user.getKey() == null) {
                 Log.w(TAG, "saveUsersToFile: Skipping user with null fields: " + user); 
                 continue;
            }
            try {
                JSONObject userJson = new JSONObject();
                userJson.put("key", user.getKey());
                userJson.put("userName", user.getUserName());
                userJson.put("password", user.getPassword());
                userJson.put("level", user.getLevel());
                userJson.put("score", user.getScore());
                userJson.put("coins", user.getCoins());
                jsonArray.put(userJson);
            } catch (JSONException e) {
                Log.e(TAG, "saveUsersToFile: Error creating JSON for user: " + user.getUserName(), e);
            }
        }

        String jsonString = jsonArray.toString();
        Log.d(TAG, "saveUsersToFile: Saving JSON string: " + jsonString);
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            fos.write(jsonString.getBytes(StandardCharsets.UTF_8));
            Log.i(TAG, "saveUsersToFile: Users saved successfully to " + FILENAME);
        } catch (IOException e) {
            Log.e(TAG, "saveUsersToFile: Error saving users to file", e);
        } finally {
            if (fos != null) {
                try { fos.close(); } catch (IOException e) { Log.e(TAG, "Error closing FOS", e); }
            }
        }
    }

    public void saveUser(User user) {
        Log.d(TAG, "saveUser: Attempting to save user: " + (user != null ? user.getUserName() : "null"));
        if (user == null || user.getUserName() == null || user.getPassword() == null) {
             Log.w(TAG, "saveUser: Cannot save null user or user with null name/password.");
             return;
        }
        
        if (!userExists(user.getUserName())) {
            if (user.getKey() == null) {
                user.setKey("user_" + System.currentTimeMillis());
                Log.d(TAG, "saveUser: Assigned new key: " + user.getKey());
            }
            Log.i(TAG, "saveUser: Adding new user to list: " + user.getUserName());
            usersList.add(user);
            saveUsersToFile();
        } else {
             Log.w(TAG, "saveUser: User already exists, save attempt ignored: " + user.getUserName());
        }
    }

    public boolean userExists(String userName) {
        Log.d(TAG, "userExists: Checking for user: " + userName + ". Current list size: " + usersList.size());
        if (userName == null) return false;
        for (User user : usersList) {
             // Log.d(TAG, "userExists: Comparing against: " + user.getUserName());
            if (user.getUserName() != null && user.getUserName().equals(userName)) {
                 Log.d(TAG, "userExists: Found match for " + userName);
                return true;
            }
        }
         Log.d(TAG, "userExists: No match found for " + userName);
        return false;
    }

     // Overload removed for simplicity unless specifically needed later
     // public boolean userExists(User u) { ... }

    public boolean checkUser(String un, String pw) {
         Log.d(TAG, "checkUser: Checking credentials for user: " + un + ", Pass: [REDACTED]. List size: " + usersList.size());
         if (un == null || pw == null) return false;
        for (User user : usersList) {
             Log.d(TAG, "checkUser: Comparing against stored user: " + user.getUserName());
            if (user.getUserName() != null && user.getUserName().equals(un)) {
                // Found the user by username
                Log.d(TAG, "checkUser: Found user by name: " + un + ". Checking password.");
                if (user.getPassword() != null && user.getPassword().equals(pw)) {
                     Log.i(TAG, "checkUser: Credentials MATCH for user: " + un);
                    return true;
                } else {
                    Log.w(TAG, "checkUser: Password MISMATCH for user: " + un + ". Stored pass: [REDACTED], Provided pass: [REDACTED]"); // Avoid logging passwords
                    return false; // Found user, but password wrong
                }
            }
        }
        Log.w(TAG, "checkUser: User NOT FOUND by name: " + un);
        return false; // User not found
    }
} 