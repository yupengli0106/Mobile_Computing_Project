package com.example.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Message {
    private String senderId;
    private String content;
    private String dateTime;

    public Message() {

    }

    public Message(String senderId, String content, String dateTime) {
        this.senderId = senderId;
        this.content = content;
        this.dateTime = dateTime;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
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

    public Long getTimestamp() {
        return Long.parseLong(dateTime);
    }

    public String getFormattedDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM. dd, yyyy - hh:mm a", Locale.US);
        return sdf.format(new Date(getTimestamp()));
    }
}
