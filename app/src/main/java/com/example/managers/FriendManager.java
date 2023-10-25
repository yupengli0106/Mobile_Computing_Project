package com.example.managers;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.model.Friend;

import java.util.ArrayList;
import java.util.List;

public class FriendManager {
    private static FriendManager instance;
    private final MutableLiveData<List<Friend>> friendsListLiveData;

    private FriendManager() {
        friendsListLiveData = new MutableLiveData<>(new ArrayList<>());
    }

    public static FriendManager getInstance() {
        if (instance == null) {
            instance = new FriendManager();
        }
        return instance;
    }

    public LiveData<List<Friend>> getFriendsList() {
        return friendsListLiveData;
    }

    public void setFriendsList(List<Friend> friendList) {
        friendsListLiveData.setValue(friendList);
    }

    public boolean isUserAFriend(String userId) {
        List<Friend> friendsList = friendsListLiveData.getValue();
        if (friendsList != null) {
            for (Friend friend : friendsList) {
                if (friend.getUserId().equals(userId)) {
                    return true;
                }
            }
        }
        return false;
    }

}
