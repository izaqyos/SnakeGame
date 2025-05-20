package com.example.snake;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

public class PrefsManager {
    private static final String PREFS_NAME = "SnakeGamePrefs"; // שם קובץ ההעדפות
    private static final String KEY_SNAKE_COLOR = "snake_color";  // מפתח לשמירת צבע הנחש
    public static final int DEFAULT_SNAKE_COLOR = Color.parseColor("#4CAF50"); // ירוק דיפולט

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void saveSnakeColor(Context context, int color) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putInt(KEY_SNAKE_COLOR, color);// שמירת הצבע כ-integer
        editor.apply(); // שמירה אסינכרונית
    }

    public static int getSnakeColor(Context context) { // טוענת את ערך הצבע השמור
        return getPreferences(context).getInt(KEY_SNAKE_COLOR, DEFAULT_SNAKE_COLOR);
    }
}
