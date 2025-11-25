package com.example.gisingv3;

import java.io.Serializable;
import java.util.Calendar;

public class Alarm implements Serializable {
    private int id;
    private int hour;
    private int minute;
    private String challengeType;
    private int difficultyLevel; // 1, 2, 3
    private boolean isEnabled;

    public Alarm(int id, int hour, int minute, String challengeType, int difficultyLevel, boolean isEnabled) {
        this.id = id;
        this.hour = hour;
        this.minute = minute;
        this.challengeType = challengeType;
        this.difficultyLevel = difficultyLevel;
        this.isEnabled = isEnabled;
    }

    public int getId() {
        return id;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public String getChallengeType() {
        return challengeType;
    }

    public int getDifficultyLevel() {
        return difficultyLevel;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
    
    public String getTimeString() {
        return String.format("%02d:%02d", hour, minute);
    }
}
