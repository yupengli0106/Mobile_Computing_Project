package com.example.managers;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.helpers.FirebaseHelper;
import com.example.model.Discussion;
import com.example.model.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class MessagesManager {
    private final String TAG = "MessagesManager";
    private static MessagesManager instance;
    private final MutableLiveData<List<Message>> messagesLiveData;


    private MessagesManager() {
        messagesLiveData = new MutableLiveData<>(new ArrayList<>());
    }

    public static MessagesManager getInstance() {
        if (instance == null) {
            instance = new MessagesManager();
        }
        return instance;
    }

    public LiveData<List<Message>> getMessages() {
        return messagesLiveData;
    }

    public void setMessages(List<Message> messages) {
        messagesLiveData.setValue(messages);
    }

    public void addMessage(Discussion discussion, String messageContent) {
        FirebaseHelper firebaseHelper = FirebaseHelper.getInstance();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firebaseHelper.sendMessage(discussion.getDiscussionId(), messageContent, new FirebaseHelper.AuthCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess: discussion created");
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.d(TAG, "onFailure: failed to create discussion");
            }
        });
    }
}
