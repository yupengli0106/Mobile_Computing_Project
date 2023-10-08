package com.example.model;

import androidx.annotation.NonNull;

public class LocationData {
    private double latitude;
    private double longitude;
    private float speed;
    private long timestamp;
    private int batteryLevel;

    public LocationData() {
        // Default constructor required for calls to DataSnapshot.getValue(LocationData.class)
    }

    public LocationData(double latitude, double longitude, float speed, long timestamp, int batteryLevel) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.timestamp = timestamp;
        this.batteryLevel = batteryLevel;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    @NonNull
    @Override
    public String toString() {
        return "LocationData{" +
                "latitude='" + latitude + '\'' +
                ", longitude=" + longitude +
                ", speed=" + speed +
                ", timestamp=" + timestamp +
                ", batteryLevel=" + batteryLevel +
                '}';
    }
}

