package com.example.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

public class Discussion implements Serializable {
    private String discussionId;
    private String receiverId;
    private String receiverUserName;

    private Message lastMessage;

    private String lastTimeOpened;

    public Discussion() {

    }
    public Discussion(String discussionId, String receiverId, String receiverUserName, Message lastMessage, String lastTimeOpened) {
        this.discussionId = discussionId;
        this.receiverId = receiverId;
        this.receiverUserName = receiverUserName;
        this.lastMessage = lastMessage;
        this.lastTimeOpened = lastTimeOpened;
    }

    public String getDiscussionId() {
        return discussionId;
    }

    public void setDiscussionId(String discussionId) {
        this.discussionId = discussionId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverUserName() {
        return receiverUserName;
    }

    public void setReceiverUserName(String receiverUserName) {
        this.receiverUserName = receiverUserName;
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastTimeOpened() {
        return lastTimeOpened;
    }

    public void setLastTimeOpened(String lastTimeOpened) {
        this.lastTimeOpened = lastTimeOpened;
    }

    public Boolean isUnread() {
        if (getLastMessage() == null || getLastTimeOpened() == null) {
            return true;
        } else {
            Date lastMessageTime = new Date(Long.parseLong(getLastMessage().getDateTime()));
            Date lastTimeOpened = new Date(Long.parseLong(getLastTimeOpened()));
            if (getLastMessage().getSenderId().equals(getReceiverId())) {
                return lastMessageTime.after(lastTimeOpened);
            }
        }
        return false;
    }
}
