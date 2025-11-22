package com.example.smarthome.model;

public class SecurityEvent {
    private String type;
    private String message;
    private String at;
    private boolean handled;
    private String deviceId;

    public SecurityEvent() {}
    public SecurityEvent(String type, String message, String at) {
        this.type = type; this.message = message; this.at = at; this.handled = false; this.deviceId = null;
    }
    public SecurityEvent(String type, String message, String at, String deviceId) {
        this.type = type; this.message = message; this.at = at; this.handled = false; this.deviceId = deviceId;
    }
    public String getType() { return type; }
    public String getMessage() { return message; }
    public String getAt() { return at; }
    public boolean isHandled() { return handled; }
    public void setHandled(boolean handled) { this.handled = handled; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String id) { this.deviceId = id; }
}