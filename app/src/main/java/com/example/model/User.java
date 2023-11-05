package com.example.model;

public class User {
    public String userId;
    public String username;
    public String email;
    public String step;

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public User() {
        // Default constructor is required for Firebase
    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


}
