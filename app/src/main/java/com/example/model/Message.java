package com.example.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Message {
    private String discussionId;
    private String messageId;
    private String senderId;
    private String receiverId;
    private String content;
    private String dateTime;

    public Message() {

    }

    public Message(String discussionId, String messageId, String senderId, String receiverId, String content, String dateTime) {
        this.discussionId = discussionId;
        this.messageId = messageId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.dateTime = dateTime;
    }
    public String getDiscussionId() {
        return discussionId;
    }

    public void setDiscussionId(String discussionId) {
        this.discussionId = discussionId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getFormattedDateTime() {
        long timestamp = Long.parseLong(dateTime);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM. dd, yyyy - hh:mm a", Locale.US);
        return sdf.format(new Date(timestamp));
    }
}
