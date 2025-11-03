package com.example.smarthome.ui.home;

public class SecurityEvent {
    private String title;
    private String time;
    private String description;

    public SecurityEvent(String title, String time, String description) {
        this.title = title;
        this.time = time;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}