package com.example.snake; // Ensure this matches your package name

import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log; // Make sure Log is imported
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
// import androidx.core.content.ContextCompat; // Not used in this version
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class ScoreAdapter extends RecyclerView.Adapter<ScoreAdapter.ScoreViewHolder> {

    private static final String ADAPTER_TAG = "ScoreAdapterDebug"; // Specific Log tag

    private List<User> userScores;
    private String currentUsernameAdapter; // Renamed for clarity within adapter

    public ScoreAdapter(List<User> userScores, String currentUsername) {
        this.userScores = userScores;
        this.currentUsernameAdapter = currentUsername;
        Log.d(ADAPTER_TAG, "Adapter created. Current logged-in username passed to adapter: '" + this.currentUsernameAdapter + "'");
    }

    @NonNull
    @Override
    public ScoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_score, parent, false);
        return new ScoreViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreViewHolder holder, int position) {
        User userInListItem = userScores.get(position);

        // Log details for the current item being bound
        String userNameFromList = userInListItem.getUserName(); // Get it once for logging and comparison
        Log.d(ADAPTER_TAG, "Binding item at position " + position +
                ". User in list: '" + userNameFromList + "' (Score: " + userInListItem.getScore() + "). " +
                "Adapter's currentUsername: '" + currentUsernameAdapter + "'");

        // Set the data
        holder.tvRank.setText(String.format(Locale.getDefault(), "%d.", position + 1));
        holder.tvUsernameScore.setText(userNameFromList);
        holder.tvScoreValue.setText(String.valueOf(userInListItem.getScore()));

        // Reset styles first
        holder.tvRank.setTypeface(null, Typeface.NORMAL);
        holder.tvUsernameScore.setTypeface(null, Typeface.NORMAL);
        holder.tvScoreValue.setTypeface(null, Typeface.NORMAL);

        // Your default text colors
        int defaultTextColor = Color.parseColor("#ba1160");
        int scoreValueColor = Color.parseColor("#ba1160"); // Same as default in your version
        holder.tvRank.setTextColor(defaultTextColor);
        holder.tvUsernameScore.setTextColor(defaultTextColor);
        holder.tvScoreValue.setTextColor(scoreValueColor);
        holder.itemView.setBackgroundColor(Color.TRANSPARENT); // Default transparent background

        // Highlighting logic
        boolean isCurrentUserMatch = false;
        if (currentUsernameAdapter != null && userNameFromList != null) {
            // Using trim() for comparison to handle potential leading/trailing spaces
            isCurrentUserMatch = currentUsernameAdapter.trim().equals(userNameFromList.trim());
            if (isCurrentUserMatch) {
                Log.i(ADAPTER_TAG, "MATCH FOUND for current user: AdapterUser='" + currentUsernameAdapter.trim() + "', ListItemUser='" + userNameFromList.trim() + "' at position " + position);
            } else {
                Log.d(ADAPTER_TAG, "No match for current user at position " + position + ": AdapterUser='" + currentUsernameAdapter.trim() + "', ListItemUser='" + userNameFromList.trim() + "'");
            }
        } else {
            Log.w(ADAPTER_TAG, "Cannot check for current user match at position " + position +
                    ": currentUsernameAdapter is " + (currentUsernameAdapter == null ? "null" : "'" + currentUsernameAdapter + "'") +
                    ", userNameFromList is " + (userNameFromList == null ? "null" : "'" + userNameFromList + "'"));
        }


        if (position == 0) {
            Log.d(ADAPTER_TAG, "Highlighting position 0 (First Place) for user: " + userNameFromList);
            holder.tvRank.setTypeface(null, Typeface.BOLD);
            holder.tvUsernameScore.setTypeface(null, Typeface.BOLD);
            holder.tvScoreValue.setTypeface(null, Typeface.BOLD);

            int goldColor = Color.parseColor("#420129"); // Your highlight color
            holder.tvRank.setTextColor(goldColor);
            holder.tvUsernameScore.setTextColor(goldColor);
            holder.tvScoreValue.setTextColor(goldColor);
        } else if (isCurrentUserMatch) {
            Log.d(ADAPTER_TAG, "Highlighting CURRENT USER (not first place): " + userNameFromList);
            holder.tvUsernameScore.setTypeface(null, Typeface.BOLD_ITALIC);
            holder.tvScoreValue.setTypeface(null, Typeface.BOLD_ITALIC);
            holder.tvRank.setTypeface(null, Typeface.BOLD_ITALIC); // Also make rank bold italic for current user

            int currentUserColor = Color.parseColor("#8a1576"); // Your highlight color
            holder.tvUsernameScore.setTextColor(currentUserColor);
            holder.tvScoreValue.setTextColor(currentUserColor);
            holder.tvRank.setTextColor(currentUserColor);
        }
        // Default styles for non-highlighted items are already set at the beginning of onBindViewHolder
    }

    @Override
    public int getItemCount() {
        return userScores != null ? userScores.size() : 0;
    }

    public void updateScores(List<User> newUserScores) {
        this.userScores = newUserScores;
        Log.d(ADAPTER_TAG, "Scores updated in adapter. New count: " + (newUserScores != null ? newUserScores.size() : 0));
        notifyDataSetChanged();
    }

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
