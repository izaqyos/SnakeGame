package com.example.snake;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    public String key;
    public String userName;
    public String password;
    public int score;


    public User() {
        // Default constructor required for Firebase
    }

    public User(String userName, String password, int level, int score, int coins) {
        this.userName = userName;
        this.password = password;
        this.score = score;
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

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "User{" +
                "key='" + key + '\'' +
                ", userName='" + userName + '\'' +
                ", score=" + score +
                '}';
    }
}