package com.example.model;

import java.io.Serializable;
import java.util.HashMap;

public class Discussion implements Serializable {
    private String discussionId;
    private String receiverId;

    private String receiverUserName;

    public Discussion() {

    }
    public Discussion(String discussionId, String receiverId, String receiverUserName) {
        this.discussionId = discussionId;
        this.receiverId = receiverId;
        this.receiverUserName = receiverUserName;
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

}
