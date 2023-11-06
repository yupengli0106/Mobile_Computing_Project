package com.example.model;

import java.util.List;
import java.util.Map;

public class User {
    public String userId;
    public String username;
    public String email;
    public Long step;
public Map<String,String> friends;

    public Map<String,String> getFriends() {
        return friends;
    }

    public void setFriends(Map<String,String> friends) {
        this.friends = friends;
    }

    public Long getStep() {
        return step==null?0:step;
    }

    public void setStep(Long step) {
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
