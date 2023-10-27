package com.example.managers;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.model.Discussion;

import java.util.ArrayList;
import java.util.List;

public class DiscussionsManager {
    private static DiscussionsManager instance;
    private final MutableLiveData<List<Discussion>> discussionsLiveData;

    private DiscussionsManager() {
        discussionsLiveData = new MutableLiveData<>(new ArrayList<>());
    }

    public static DiscussionsManager getInstance() {
        if (instance == null) {
            instance = new DiscussionsManager();
        }
        return instance;
    }

    public LiveData<List<Discussion>> getDiscussions() {
        return discussionsLiveData;
    }

    public void setDiscussions(List<Discussion> discussions) {
        discussionsLiveData.setValue(discussions);
    }

    public void addNewDiscussion(Discussion newDiscussion) {
        List<Discussion> currentDiscussions = discussionsLiveData.getValue();
        if (currentDiscussions == null) {
            currentDiscussions = new ArrayList<>();
        }
        currentDiscussions.add(newDiscussion);
        discussionsLiveData.setValue(currentDiscussions);
    }
}
