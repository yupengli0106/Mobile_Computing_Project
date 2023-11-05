package com.example.model;

import android.util.Log;

import com.example.helpers.FirebaseHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Discussion implements Serializable {
    private String discussionId;
    private Map<String, Map<String, Object>> participants;
    private Map<String, Message> messages;
    private Message lastMessage;

    private final FirebaseHelper firebaseHelper = FirebaseHelper.getInstance();
    private final String currentUserId = firebaseHelper.getCurrentUserId();

    public Discussion() {

    }

    public Discussion(String discussionId, Map<String, Map<String, Object>> participants, Map<String, Message> messages, Message lastMessage) {
        this.discussionId = discussionId;
        this.participants = participants;
        this.messages = messages;
        this.lastMessage = lastMessage;
    }

    public String getDiscussionId() {
        return discussionId;
    }

    public void setDiscussionId(String discussionId) {
        this.discussionId = discussionId;
    }

    public Map<String, Map<String, Object>> getParticipants() {
        return participants;
    }

    public void setParticipants(Map<String, Map<String, Object>> participants) {
        this.participants = participants;
    }

    public List<String> getOtherParticipantIDs(String currentUserID) {
        List<String> otherParticipants = new ArrayList<>(getParticipants().keySet());
        otherParticipants.remove(currentUserID);
        return otherParticipants;
    }

    public Map<String, Map<String, Object>> getOtherParticipantDetails(String currentUserID) {
        Map<String, Map<String, Object>> allParticipants = new HashMap<>(getParticipants());

        for (Map.Entry<String, Map<String, Object>> entry : getParticipants().entrySet()) {
            allParticipants.put(entry.getKey(), new HashMap<>(entry.getValue()));
        }

        allParticipants.remove(currentUserID);
        return allParticipants;
    }

    public Map<String, Message> getMessages() {
        return messages;
    }

    public void setMessages(Map<String, Message> messages) {
        this.messages = messages;
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Boolean isUnread() {
        Long lastTimeOpened = null;
        if (participants.get(currentUserId).containsKey("lastTimeOpened")) {
            Log.d("CHECK UNREAD", "contains key");
            lastTimeOpened = Long.valueOf(participants.get(currentUserId).get("lastTimeOpened").toString());  // Assuming this returns a Long
        }

        Log.d("CHECK UNREAD", "isUnread: " + lastTimeOpened + " " + getLastMessage());

        // Scenario 1: User has never opened it
        if (lastTimeOpened == null) {
            return true;
        }

        Message lastMessage = getLastMessage();

        // Scenario 4: The last message was sent by the current user.
        if (lastMessage != null && lastMessage.getSenderId().equals(currentUserId)) {
            return false;
        }

        // Scenario 2: There's a last message and its timestamp is newer than the last time opened
        if (lastMessage != null && lastMessage.getTimestamp() > lastTimeOpened) {
            return true;
        }

        // Scenario 3: There's no last message and user has opened the conversation
        if (lastMessage == null && lastTimeOpened != null) {
            return false;
        }

        return false; // This is a fallback return, ideally shouldn't be reached
    }
}
