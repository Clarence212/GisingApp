package com.example.gisingv3;

import java.io.Serializable;
import java.util.Locale;

public class Alarm implements Serializable {
    private int id;
    private int hour;
    private int minute;
    private String challengeType;
    private int difficultyLevel;
    private boolean isEnabled;
    private boolean[] daysSelected; // [Sun, Mon, Tue, Wed, Thu, Fri, Sat]

    public Alarm(int id, int hour, int minute, String challengeType, int difficultyLevel, boolean isEnabled, boolean[] daysSelected) {
        this.id = id;
        this.hour = hour;
        this.minute = minute;
        this.challengeType = challengeType;
        this.difficultyLevel = difficultyLevel;
        this.isEnabled = isEnabled;
        this.daysSelected = daysSelected;
    }

    public int getId() { return id; }
    public int getHour() { return hour; }
    public int getMinute() { return minute; }
    public String getChallengeType() { return challengeType; }
    public int getDifficultyLevel() { return difficultyLevel; }
    public boolean isEnabled() { return isEnabled; }
    public void setEnabled(boolean enabled) { isEnabled = enabled; }
    public boolean[] getDaysSelected() { return daysSelected; }
    
    public String getTimeString() {
        String amPm = hour >= 12 ? "PM" : "AM";
        int hour12 = hour % 12;
        if (hour12 == 0) hour12 = 12;
        return String.format(Locale.getDefault(), "%02d:%02d %s", hour12, minute, amPm);
    }

    public String getDaysDisplay() {
        if (daysSelected == null) return "One-time alarm";
        
        StringBuilder sb = new StringBuilder();
        String[] shortDays = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        int count = 0;
        for (int i = 0; i < 7; i++) {
            if (daysSelected[i]) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(shortDays[i]);
                count++;
            }
        }
        
        if (count == 0) return "Once";
        if (count == 7) return "Every day";
        if (count == 5 && !daysSelected[0] && !daysSelected[6]) return "Weekdays";
        if (count == 2 && daysSelected[0] && daysSelected[6]) return "Weekends";
        
        return sb.toString();
    }
}
