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
    private String toneUri;
    private String toneTitle;

    public Alarm(int id, int hour, int minute, String challengeType, int difficultyLevel, boolean isEnabled, boolean[] daysSelected, String toneUri, String toneTitle) {
        this.id = id;
        this.hour = hour;
        this.minute = minute;
        this.challengeType = challengeType;
        this.difficultyLevel = difficultyLevel;
        this.isEnabled = isEnabled;
        this.daysSelected = daysSelected;
        this.toneUri = toneUri;
        this.toneTitle = toneTitle;
    }

    public int getId() { return id; }
    public int getHour() { return hour; }
    public int getMinute() { return minute; }
    public String getChallengeType() { return challengeType; }
    public int getDifficultyLevel() { return difficultyLevel; }
    public boolean isEnabled() { return isEnabled; }
    public void setEnabled(boolean enabled) { isEnabled = enabled; }
    public String getToneUri() { return toneUri; }
    public String getToneTitle() { return toneTitle; }
    public void setTone(String uri, String title) { 
        this.toneUri = uri;
        this.toneTitle = title;
    }
    public boolean[] getDaysSelected() { 
        if (daysSelected == null) return new boolean[7];
        return daysSelected; 
    }
    
    public String getTimeString() {
        String amPm = hour >= 12 ? "PM" : "AM";
        int hour12 = hour % 12;
        if (hour12 == 0) hour12 = 12;
        // Use %d instead of %02d for hour to remove the leading zero
        return String.format(Locale.getDefault(), "%d:%02d %s", hour12, minute, amPm);
    }

    public String getDaysDisplay() {
        boolean[] days = getDaysSelected();
        
        StringBuilder sb = new StringBuilder();
        String[] shortDays = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        int count = 0;
        for (int i = 0; i < 7; i++) {
            if (days[i]) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(shortDays[i]);
                count++;
            }
        }
        
        if (count == 0) return "Once";
        if (count == 7) return "Every day";
        if (count == 5 && !days[0] && !days[6]) return "Weekdays";
        if (count == 2 && days[0] && days[6]) return "Weekends";
        
        return sb.toString();
    }
}
