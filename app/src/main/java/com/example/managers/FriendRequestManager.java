package com.example.managers;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.model.FriendRequest;

import java.util.List;

public class FriendRequestManager {

    private static FriendRequestManager instance;
    private final MutableLiveData<List<FriendRequest>> friendRequestsLiveData;

    private FriendRequestManager() {
        friendRequestsLiveData = new MutableLiveData<>();
    }

    public static synchronized FriendRequestManager getInstance() {
        if (instance == null) {
            instance = new FriendRequestManager();
        }
        return instance;
    }

    public void setFriendRequests(List<FriendRequest> friendRequests) {
        friendRequestsLiveData.setValue(friendRequests);
    }

    public LiveData<List<FriendRequest>> getFriendRequests() {
        return friendRequestsLiveData;
    }
}
