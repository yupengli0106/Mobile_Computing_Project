package com.example.model;

import java.io.Serializable;

public class FriendRequest implements Serializable {
    private String requestId;
    private String requester;
    private String fromUserId;
    private String toUserId;
    private String status; // "pending", "accepted", "declined"

    public FriendRequest() {

    }

    public FriendRequest(String requestId, String requester, String fromUserId, String toUserId, String status) {
        this.requestId = requestId;
        this.requester = requester;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.status = status;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequester() {
        return requester;
    }

    public void setRequester(String requester) {
        this.requester = requester;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
