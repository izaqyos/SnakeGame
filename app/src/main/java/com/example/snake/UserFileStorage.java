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
        this.context = context;
        this.usersList = loadUsersFromFile();
        initializeTestUser();
    }

    private void initializeTestUser() {
        if (!userExists("testuser")) {
            Log.d(TAG, "Initializing test user...");
            User testUser = new User("testuser", "abc123", 0, 0, 0);
            // Use a simple key for the test user, actual key isn't critical for file storage
            testUser.setKey("testkey");
            saveUser(testUser); // This will also save the updated list to the file
        } else {
             Log.d(TAG, "Test user already exists.");
        }
    }

    private List<User> loadUsersFromFile() {
        List<User> users = new ArrayList<>();
        FileInputStream fis = null;
        try {
            File file = new File(context.getFilesDir(), FILENAME);
            if (!file.exists()) {
                 Log.d(TAG, "Users file does not exist, creating empty list.");
                 return users; // Return empty list if file doesn't exist
            }

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

            String jsonString = stringBuilder.toString();
             Log.d(TAG, "Read from file: " + jsonString);
             if (jsonString.isEmpty()) {
                 return users; // Return empty list if file is empty
             }

            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject userJson = jsonArray.getJSONObject(i);
                User user = new User();
                // Key might not be present in older saves or needed for file storage logic
                if (userJson.has("key")) {
                   user.setKey(userJson.getString("key"));
                } else {
                    // Assign a placeholder or generate one if needed, though not strictly necessary for file check
                    user.setKey("user_" + i);
                }
                user.setUserName(userJson.getString("userName"));
                user.setPassword(userJson.getString("password")); // Assuming password stored directly for now
                user.setLevel(userJson.optInt("level", 0)); // Use optInt for optional fields
                user.setScore(userJson.optInt("score", 0));
                user.setCoins(userJson.optInt("coins", 0));
                users.add(user);
                 Log.d(TAG, "Loaded user: " + user.getUserName());
            }
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error loading users from file", e);
            // Consider clearing the list or handling the error appropriately
             users.clear();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing file input stream", e);
                }
            }
        }
         Log.d(TAG, "Finished loading users. Count: " + users.size());
        return users;
    }

    private void saveUsersToFile() {
        JSONArray jsonArray = new JSONArray();
        for (User user : usersList) {
            try {
                JSONObject userJson = new JSONObject();
                // Key might be null if user was loaded from an older file without it
                userJson.put("key", user.getKey() != null ? user.getKey() : "user_" + usersList.indexOf(user));
                userJson.put("userName", user.getUserName());
                userJson.put("password", user.getPassword()); // Storing password directly (consider hashing in production)
                userJson.put("level", user.getLevel());
                userJson.put("score", user.getScore());
                userJson.put("coins", user.getCoins());
                jsonArray.put(userJson);
            } catch (JSONException e) {
                Log.e(TAG, "Error creating JSON for user: " + user.getUserName(), e);
            }
        }

        String jsonString = jsonArray.toString();
         Log.d(TAG, "Saving to file: " + jsonString);
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            fos.write(jsonString.getBytes(StandardCharsets.UTF_8));
             Log.d(TAG, "Users saved successfully.");
        } catch (IOException e) {
            Log.e(TAG, "Error saving users to file", e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing file output stream", e);
                }
            }
        }
    }

    public void saveUser(User user) {
        if (!userExists(user.getUserName())) {
            // Assign a simple key if not present, needed for JSON structure consistency
            if (user.getKey() == null) {
                 user.setKey("user_" + System.currentTimeMillis()); // Simple unique key
            }
            usersList.add(user);
            saveUsersToFile(); // Save the updated list
             Log.d(TAG, "User saved: " + user.getUserName());
        } else {
             Log.w(TAG, "Attempted to save existing user: " + user.getUserName());
        }
    }

    public boolean userExists(String userName) {
         Log.d(TAG, "Checking existence for user: " + userName);
         Log.d(TAG, "Current user list size: " + usersList.size());
        for (User user : usersList) {
             Log.d(TAG, "Comparing with: " + user.getUserName());
            if (user.getUserName() != null && user.getUserName().equals(userName)) {
                 Log.d(TAG, "User found: " + userName);
                return true;
            }
        }
         Log.d(TAG, "User not found: " + userName);
        return false;
    }

     // Overload for checking User object (used initially, keeping for compatibility if needed)
     public boolean userExists(User u) {
        return userExists(u.getUserName());
     }


    public boolean checkUser(String un, String pw) {
         Log.d(TAG, "Checking credentials for user: " + un);
        for (User user : usersList) {
            if (user.getUserName() != null && user.getUserName().equals(un)) {
                // Direct password comparison (Insecure! Use hashing in real apps)
                if (user.getPassword() != null && user.getPassword().equals(pw)) {
                     Log.d(TAG, "Credentials match for user: " + un);
                    return true;
                } else {
                    Log.d(TAG, "Password mismatch for user: " + un);
                    return false; // Found user, but password wrong
                }
            }
        }
        Log.d(TAG, "User not found during check: " + un);
        return false; // User not found
    }
} 