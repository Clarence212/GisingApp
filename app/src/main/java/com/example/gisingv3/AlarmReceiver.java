package com.example.gisingv3;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import androidx.core.app.NotificationCompat;
import java.util.Calendar;
import java.util.List;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "ALARM_CHANNEL_V4";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Use a WakeLock to ensure the CPU stays awake while we process the alarm
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "GisingApp:AlarmWakeLock");
        wakeLock.acquire(10000); // Hold for 10 seconds

        int alarmId = intent.getIntExtra("alarm_id", -1);
        String challengeType = intent.getStringExtra("challenge_type");
        int difficulty = intent.getIntExtra("difficulty", 1);
        String toneUri = intent.getStringExtra("tone_uri");

        createNotificationChannel(context);

        Intent ringIntent = new Intent(context, AlarmRingActivity.class);
        ringIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        ringIntent.setAction("com.example.gisingv3.RING_ALARM_" + alarmId);
        ringIntent.putExtra("alarm_id", alarmId);
        ringIntent.putExtra("challenge_type", challengeType);
        ringIntent.putExtra("difficulty", difficulty);
        ringIntent.putExtra("tone_uri", toneUri);

        PendingIntent ringPendingIntent = PendingIntent.getActivity(context, alarmId,
                ringIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (toneUri != null && !toneUri.isEmpty()) {
            alarmSound = Uri.parse(toneUri);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("ALARM")
                .setContentText("WAKE UP! Solve the challenge to stop.")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(ringPendingIntent, true)
                .setContentIntent(ringPendingIntent)
                .setSound(alarmSound)
                .setOngoing(true)
                .setAutoCancel(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(alarmId, builder.build());
        }
        
        // Force the activity to the front
        context.startActivity(ringIntent);

        // Reschedule logic
        rescheduleNextOccurrence(context, alarmId);
    }

    private void rescheduleNextOccurrence(Context context, int alarmId) {
        List<Alarm> alarmList = AlarmStorage.loadAlarms(context);
        if (alarmList == null) return;

        for (Alarm alarm : alarmList) {
            if (alarm.getId() == alarmId && alarm.isEnabled()) {
                boolean hasDays = false;
                for (boolean d : alarm.getDaysSelected()) if (d) hasDays = true;
                if (hasDays) {
                    scheduleNext(context, alarm);
                }
                break;
            }
        }
    }

    private void scheduleNext(Context context, Alarm alarm) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setData(Uri.parse("alarm://" + alarm.getId()));
        intent.putExtra("alarm_id", alarm.getId());
        intent.putExtra("challenge_type", alarm.getChallengeType());
        intent.putExtra("difficulty", alarm.getDifficultyLevel());
        intent.putExtra("tone_uri", alarm.getToneUri());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarm.getId(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, alarm.getHour());
        calendar.set(Calendar.MINUTE, alarm.getMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar.add(Calendar.DAY_OF_YEAR, 1);

        for (int i = 0; i < 7; i++) {
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            int dayIndex = dayOfWeek - 1;

            if (alarm.getDaysSelected()[dayIndex]) {
                if (alarmManager != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (alarmManager.canScheduleExactAlarms()) {
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                        } else {
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                        }
                    } else {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                    }
                }
                return;
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null && manager.getNotificationChannel(CHANNEL_ID) != null) return;

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Alarm Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for Alarm notifications");
            
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            
            channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), audioAttributes);
            channel.enableVibration(true);
            channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);
            
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }
}
