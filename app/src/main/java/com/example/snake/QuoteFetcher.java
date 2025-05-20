package com.example.snake;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import org.json.JSONArray; // Needed for Zen Quotes API response
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.nio.charset.StandardCharsets;

public class QuoteFetcher {
    private static final String TAG = "QuoteFetcher_Zen";
    private static final String QUOTE_API_URL = "https://zenquotes.io/api/random"; // כתובת ה-API
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(); // לביצוע ברקע
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper()); // להחזרת תוצאות ל-UI Thread

    public interface QuoteCallback {
        void onQuoteFetched(String quote, String author);
        void onFetchFailed(String error);
    }

    public void fetchRandomQuote(QuoteCallback callback) {
        executorService.execute(() -> { // הרצת הפעולה ב-Thread נפרד
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            StringBuilder responseContent = new StringBuilder();

            try {
                URL url = new URL(QUOTE_API_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.setRequestProperty("Accept", "application/json");

                int statusCode = connection.getResponseCode();

                if (statusCode == HttpURLConnection.HTTP_OK) {  // הצלחה
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseContent.append(line);
                    }
                    String responseBody = responseContent.toString();
                    Log.d(TAG, "Raw JSON response: " + responseBody); // Log raw response

                    JSONArray jsonArray = new JSONArray(responseBody); // ה-API מחזיר מערך
                    if (jsonArray.length() > 0) {
                        JSONObject quoteObject = jsonArray.getJSONObject(0); // לקיחת האובייקט הראשון
                        String content = quoteObject.optString("q", "Could not parse quote content."); // 'q' for quote
                        String author = quoteObject.optString("a", "Unknown");        // 'a' for author

                        mainThreadHandler.post(() -> callback.onQuoteFetched(content, author));
                    } else {
                        String errorMsg = "Empty array received from Zen Quotes API.";
                        Log.e(TAG, errorMsg);
                        mainThreadHandler.post(() -> callback.onFetchFailed(errorMsg));
                    }

                } else {
                    String errorMsg = "Failed to fetch quote. HTTP Status: " + statusCode;
                    try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                        String errorLine;
                        StringBuilder errorResponse = new StringBuilder();
                        while ((errorLine = errorReader.readLine()) != null) {
                            errorResponse.append(errorLine);
                        }
                    }
                    mainThreadHandler.post(() -> callback.onFetchFailed(errorMsg + " - Check Logcat for API error response."));
                }
            } catch (Exception e) { // טיפול בשגיאות כלליות (רשת, JSON, וכו')
                String errorMsg = "Error fetching/parsing quote: " + e.getMessage();
                mainThreadHandler.post(() -> callback.onFetchFailed(errorMsg));
            } finally {
                if (reader != null) {   // סגירת משאבים
                    try {
                        reader.close();
                    } catch (Exception e) {
                        Log.e(TAG, "Error closing reader", e);
                    }
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
