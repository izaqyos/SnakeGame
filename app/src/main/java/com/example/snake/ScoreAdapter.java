package com.example.snake; // Ensure this matches your package name

import android.graphics.Color; // For custom colors
import android.graphics.Typeface; // For bold text
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat; // For accessing colors from resources
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class ScoreAdapter extends RecyclerView.Adapter<ScoreAdapter.ScoreViewHolder> {

    private List<User> userScores; // List of users with their scores
    private String currentUsername; // To identify the current user's score

    // Updated constructor to accept the current username
    public ScoreAdapter(List<User> userScores, String currentUsername) {
        this.userScores = userScores;
        this.currentUsername = currentUsername;
    }

    @NonNull
    @Override
    public ScoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item_score.xml layout for each row
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_score, parent, false);
        return new ScoreViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreViewHolder holder, int position) {
        // Get the user at the current position
        User currentUser = userScores.get(position);

        // Set the data to the TextViews in item_score.xml
        holder.tvRank.setText(String.format(Locale.getDefault(), "%d.", position + 1)); // Rank (1., 2., etc.)
        holder.tvUsernameScore.setText(currentUser.getUserName()); // Username
        holder.tvScoreValue.setText(String.valueOf(currentUser.getScore())); // Score

        // Reset styles first (important for RecyclerView recycling)
        holder.tvRank.setTypeface(null, Typeface.NORMAL);
        holder.tvUsernameScore.setTypeface(null, Typeface.NORMAL);
        holder.tvScoreValue.setTypeface(null, Typeface.NORMAL); // Assuming score value might be bold by default in XML

        // Default text colors (you can define these in colors.xml and use ContextCompat.getColor)
        int defaultTextColor = Color.parseColor("#ba1160"); // Default from item_score.xml
        int scoreValueColor = Color.parseColor("#ba1160"); // Theme color for score from item_score.xml
        holder.tvRank.setTextColor(defaultTextColor);
        holder.tvUsernameScore.setTextColor(defaultTextColor);
        holder.tvScoreValue.setTextColor(scoreValueColor);
        holder.itemView.setBackgroundColor(Color.TRANSPARENT); // Default transparent background


        // --- Highlighting Logic ---
        boolean isCurrentUser = currentUsername != null && currentUsername.equals(currentUser.getUserName());

        if (position == 0) {
            // Apply special styling for the top score (first place)
            holder.tvRank.setTypeface(null, Typeface.BOLD);
            holder.tvUsernameScore.setTypeface(null, Typeface.BOLD);
            holder.tvScoreValue.setTypeface(null, Typeface.BOLD);

            int goldColor = Color.parseColor("#420129");
            holder.tvRank.setTextColor(goldColor);
            holder.tvUsernameScore.setTextColor(goldColor);
            holder.tvScoreValue.setTextColor(goldColor);

            // Optional: Different background for first place
            // holder.itemView.setBackgroundColor(Color.parseColor("#FFFDE7")); // A light yellow/cream background
        } else if (isCurrentUser) {
            // Apply special styling for the current user's score (if not first place)
            holder.tvUsernameScore.setTypeface(null, Typeface.BOLD_ITALIC);
            holder.tvScoreValue.setTypeface(null, Typeface.BOLD_ITALIC);

            // Example: Change text color for the current user
            int currentUserColor = Color.parseColor("#007BFF"); // A distinct blue
            holder.tvUsernameScore.setTextColor(currentUserColor);
            holder.tvScoreValue.setTextColor(currentUserColor);
            holder.tvRank.setTextColor(currentUserColor); // Also highlight rank for current user

            // Optional: Different background for current user's score row
            // holder.itemView.setBackgroundColor(Color.parseColor("#E7F3FF")); // A light blue background
        }
        // No 'else' needed here for default styles, as they are set at the beginning of onBindViewHolder
    }

    @Override
    public int getItemCount() {
        return userScores != null ? userScores.size() : 0;
    }

    // Method to update the list of scores in the adapter
    public void updateScores(List<User> newUserScores) {
        this.userScores = newUserScores;
        notifyDataSetChanged(); // Notify the RecyclerView that the data has changed
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
