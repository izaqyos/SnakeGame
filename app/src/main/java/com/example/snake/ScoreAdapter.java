package com.example.snake; // Ensure this matches your package name

import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log; // Make sure Log is imported
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class ScoreAdapter extends RecyclerView.Adapter<ScoreAdapter.ScoreViewHolder> {
    private static final String ADAPTER_TAG = "ScoreAdapterDebug"; // Specific Log tag
    private List<User> userScores;
    private String currentUsernameAdapter; // שם משתמש נוכחי
    public ScoreAdapter(List<User> userScores, String currentUsername) {
        this.userScores = userScores;
        this.currentUsernameAdapter = currentUsername;
        Log.d(ADAPTER_TAG, "Adapter created. Current logged-in username passed to adapter: '" + this.currentUsernameAdapter + "'");
    }

    @NonNull
    @Override
    public ScoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { //נקראת על ידי ה-`RecyclerView` כאשר הוא צריך ליצור `ViewHolder` חדש
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_score, parent, false);
        return new ScoreViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreViewHolder holder, int position) {
        User userInListItem = userScores.get(position);
        String userNameFromList = userInListItem.getUserName();

        // הצבת הנתונים
        holder.tvRank.setText(String.format(Locale.getDefault(), "%d.", position + 1));
        holder.tvUsernameScore.setText(userNameFromList);
        holder.tvScoreValue.setText(String.valueOf(userInListItem.getScore()));

        // איפוס סגנונות (חשוב למיחזור תצוגות)
        holder.tvRank.setTypeface(null, Typeface.NORMAL);
        holder.tvUsernameScore.setTypeface(null, Typeface.NORMAL);
        holder.tvScoreValue.setTypeface(null, Typeface.NORMAL);
        int defaultTextColor = Color.parseColor("#ba1160"); //  default color
        holder.tvRank.setTextColor(defaultTextColor);
        holder.tvUsernameScore.setTextColor(defaultTextColor);
        holder.tvScoreValue.setTextColor(defaultTextColor); // צבע הניקוד
        holder.itemView.setBackgroundColor(Color.TRANSPARENT); //רקע שקוף דיפולט

        // Highlighting logic
        boolean isCurrentUserMatch = false;
        if (currentUsernameAdapter != null && userNameFromList != null) {
            isCurrentUserMatch = currentUsernameAdapter.trim().equals(userNameFromList.trim());
        }
        if (position == 0) { // Highlight first place
            Log.d(ADAPTER_TAG, "Highlighting position 0 (First Place) for user: " + userNameFromList);
            holder.tvRank.setTypeface(null, Typeface.BOLD);
            holder.tvUsernameScore.setTypeface(null, Typeface.BOLD);
            holder.tvScoreValue.setTypeface(null, Typeface.BOLD);
            int goldColor = Color.parseColor("#420129"); // my highlight color for 1st place
            holder.tvRank.setTextColor(goldColor);
            holder.tvUsernameScore.setTextColor(goldColor);
            holder.tvScoreValue.setTextColor(goldColor);
        } else if (isCurrentUserMatch) { // Highlight current user (if not first place)
            Log.d(ADAPTER_TAG, "Highlighting CURRENT USER (not first place): " + userNameFromList);
            holder.tvUsernameScore.setTypeface(null, Typeface.BOLD_ITALIC);
            holder.tvScoreValue.setTypeface(null, Typeface.BOLD_ITALIC);
            holder.tvRank.setTypeface(null, Typeface.BOLD_ITALIC);
            int currentUserColor = Color.parseColor("#8a1576"); // my highlight color for current user
            holder.tvUsernameScore.setTextColor(currentUserColor);
            holder.tvScoreValue.setTextColor(currentUserColor);
            holder.tvRank.setTextColor(currentUserColor);
        }
    }

    @Override
    public int getItemCount() {
        return userScores != null ? userScores.size() : 0;
    }
    // ViewHolder class to hold references to the views in item_score.xml
    static class ScoreViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank;
        TextView tvUsernameScore;
        TextView tvScoreValue;
        ScoreViewHolder(View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvUsernameScore = itemView.findViewById(R.id.tvUsernameScore);
            tvScoreValue = itemView.findViewById(R.id.tvScoreValue);
        }
    }
}
