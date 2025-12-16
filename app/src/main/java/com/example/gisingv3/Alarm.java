package com.example.gisingv3;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Locale;

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
        String amPm = hour >= 12 ? "PM" : "AM";
        int hour12 = hour % 12;
        if (hour12 == 0) hour12 = 12;
        return String.format(Locale.getDefault(), "%02d:%02d %s", hour12, minute, amPm);
    }
}
