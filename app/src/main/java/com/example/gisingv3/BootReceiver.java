package com.example.gisingv3;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import java.util.Calendar;
import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
            "android.intent.action.QUICKBOOT_POWERON".equals(action) ||
            "com.htc.intent.action.QUICKBOOT_POWERON".equals(action)) {
            
            List<Alarm> alarmList = AlarmStorage.loadAlarms(context);
            if (alarmList != null) {
                for (Alarm alarm : alarmList) {
                    if (alarm.isEnabled()) {
                        scheduleAlarm(context, alarm);
                    }
                }
            }
        }
    }

    private void scheduleAlarm(Context context, Alarm alarm) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("challenge_type", alarm.getChallengeType());
        intent.putExtra("difficulty", alarm.getDifficultyLevel());
        intent.putExtra("alarm_id", alarm.getId());
        
        intent.setData(Uri.parse("alarm://" + alarm.getId()));
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarm.getId(), intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        long triggerTime = getNextTriggerTime(alarm);

        Intent showIntent = new Intent(context, MainActivity.class);
        PendingIntent pShowIntent = PendingIntent.getActivity(context, 0, showIntent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(triggerTime, pShowIntent);

        try {
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
        } catch (SecurityException e) {
            // Fallback for missing exact alarm permission
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }

    private long getNextTriggerTime(Alarm alarm) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, alarm.getHour());
        calendar.set(Calendar.MINUTE, alarm.getMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long now = System.currentTimeMillis();
        boolean[] days = alarm.getDaysSelected();
        
        boolean hasDays = false;
        if (days != null) {
            for (boolean d : days) if (d) hasDays = true;
        }
        
        if (!hasDays) {
            if (calendar.getTimeInMillis() <= now) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            return calendar.getTimeInMillis();
        }

        for (int i = 0; i < 7; i++) {
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK); // 1 = Sun, 2 = Mon...
            int dayIndex = dayOfWeek - 1;
            
            if (days[dayIndex] && calendar.getTimeInMillis() > now) {
                return calendar.getTimeInMillis();
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        
        return calendar.getTimeInMillis();
    }
}
