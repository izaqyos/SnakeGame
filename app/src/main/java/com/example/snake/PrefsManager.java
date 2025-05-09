package com.example.snake;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

public class PrefsManager {
    private static final String PREFS_NAME = "SnakeGamePrefs";
    private static final String KEY_SNAKE_COLOR = "snake_color";

    // Default snake color if nothing is set (e.g., first launch)
    // This should ideally match your initial snakePaint color or be the desired default.
    public static final int DEFAULT_SNAKE_COLOR = Color.parseColor("#4CAF50"); // Default Green

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void saveSnakeColor(Context context, int color) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putInt(KEY_SNAKE_COLOR, color);
        editor.apply();
    }

    public static int getSnakeColor(Context context) {
        return getPreferences(context).getInt(KEY_SNAKE_COLOR, DEFAULT_SNAKE_COLOR);
    }
}
