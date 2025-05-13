package com.example.snake; // ודאי שהחבילה תואמת לשלך

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.ImageButton;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HighScoresManager {

    private static final String PREFS_NAME = "SnakeHighScoresPrefs"; // שם קובץ ההעדפות
    private static final String KEY_HIGH_SCORES = "high_scores_list"; // מפתח לרשימת הניקוד
    private static final int MAX_SCORES_TO_KEEP = 5; // כמה שיאים לשמור (למשל, 5 הגבוהים ביותר)
    private static final String TAG = "HighScoresManager";

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * שומר ניקוד חדש אם הוא מספיק גבוה.
     * @param context Context של האפליקציה
     * @param newScore הניקוד החדש שהושג
     */
    public static void saveScore(Context context, int newScore) {
        if (context == null) {
            Log.e(TAG, "Context is null, cannot save score.");
            return;
        }
        Log.d(TAG, "Attempting to save score: " + newScore);
        List<Integer> highScores = getHighScores(context);

        if (highScores.size() < MAX_SCORES_TO_KEEP) {
            highScores.add(newScore);
            Log.d(TAG, "Added score (list not full): " + newScore);
        } else if (newScore > Collections.min(highScores)) { // בדוק אם הניקוד החדש גבוה מהנמוך ביותר ברשימה
            highScores.remove(Collections.min(highScores)); // הסר את הנמוך ביותר
            highScores.add(newScore); // הוסף את החדש
            Log.d(TAG, "Replaced score. New score: " + newScore);
        } else {
            Log.d(TAG, "Score " + newScore + " is not high enough.");
            return; // הניקוד לא מספיק גבוה
        }

        Collections.sort(highScores, Collections.reverseOrder()); // מיין מהגבוה לנמוך

        // ודא שהרשימה לא חורגת מהמקסימום (למקרה שהייתה קטנה מ-MAX_SCORES_TO_KEEP והוספנו)
        while (highScores.size() > MAX_SCORES_TO_KEEP) {
            highScores.remove(highScores.size() - 1);
        }

        // שמור את הרשימה המעודכנת כ-JSON string
        JSONArray jsonArray = new JSONArray(highScores);
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putString(KEY_HIGH_SCORES, jsonArray.toString());
        editor.apply();
        Log.d(TAG, "High scores saved: " + jsonArray.toString());
    }

    /**
     * מחזיר רשימה של הניקוד הגבוה ביותר.
     * @param context Context של האפליקציה
     * @return List<Integer> של הניקוד הגבוה, ממוין מהגבוה לנמוך.
     */
    public static List<Integer> getHighScores(Context context) {
        if (context == null) {
            Log.e(TAG, "Context is null, cannot get high scores.");
            return new ArrayList<>(); // החזר רשימה ריקה במקרה של שגיאה
        }
        List<Integer> highScores = new ArrayList<>();
        String jsonString = getPreferences(context).getString(KEY_HIGH_SCORES, null);

        if (jsonString != null) {
            try {
                JSONArray jsonArray = new JSONArray(jsonString);
                for (int i = 0; i < jsonArray.length(); i++) {
                    highScores.add(jsonArray.getInt(i));
                }
                Log.d(TAG, "High scores loaded: " + highScores.toString());
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing high scores JSON: " + e.getMessage());
                // במקרה של שגיאה בפירוש, אפשר לנקות את ההעדפות השגויות
                // getPreferences(context).edit().remove(KEY_HIGH_SCORES).apply();
            }
        } else {
            Log.d(TAG, "No high scores found in SharedPreferences.");
        }
        // ודא שהרשימה ממוינת גם אם היא נטענה לראשונה
        Collections.sort(highScores, Collections.reverseOrder());
        return highScores;
    }
}
