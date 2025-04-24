package com.example.snake;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    public String key;
    public String userName;
    public String password;
    public int level;
    public int score;
    public int coins;

    public User() {
        // Default constructor required for Firebase
    }

    public User(String userName, String password, int level, int score, int coins) {
        this.userName = userName;
        this.password = password;
        this.level = level;
        this.score = score;
        this.coins = coins;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    @Override
    public String toString() {
        return "User{" +
                "key='" + key + '\'' +
                ", userName='" + userName + '\'' +
                ", level=" + level +
                ", score=" + score +
                ", coins=" + coins +
                '}';
    }
}