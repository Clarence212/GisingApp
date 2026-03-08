package com.example.gisingv3;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvAlarms;
    private TextView tvNoAlarms;
    private AlarmAdapter adapter;
    private List<Alarm> alarmList;
    private int editingPosition = -1;

    private final ActivityResultLauncher<Intent> alarmActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Alarm alarm = (Alarm) result.getData().getSerializableExtra("new_alarm");
                    if (alarm != null) {
                        if (editingPosition != -1) {
                            cancelAlarm(alarmList.get(editingPosition));
                            alarmList.set(editingPosition, alarm);
                            adapter.notifyItemChanged(editingPosition);
                            editingPosition = -1;
                        } else {
                            alarmList.add(alarm);
                            adapter.notifyItemInserted(alarmList.size() - 1);
                        }
                        saveAlarms();
                        updateEmptyState();
                        if (alarm.isEnabled()) {
                            scheduleAlarm(alarm);
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvAlarms = findViewById(R.id.rvAlarms);
        tvNoAlarms = findViewById(R.id.tvNoAlarms);
        FloatingActionButton btnAddAlarm = findViewById(R.id.btnAddAlarm);

        alarmList = AlarmStorage.loadAlarms(this);
        if (alarmList == null) {
            alarmList = new ArrayList<>();
        }

        // Reschedule to ensure accuracy after app update/storage load
        rescheduleActiveAlarms();

        adapter = new AlarmAdapter(alarmList, new AlarmAdapter.OnAlarmListener() {
            @Override
            public void onToggle(Alarm alarm, boolean isEnabled) {
                if (isEnabled) {
                    scheduleAlarm(alarm);
                } else {
                    cancelAlarm(alarm);
                }
                saveAlarms();
            }

            @Override
            public void onDelete(Alarm alarm, int position) {
                cancelAlarm(alarm);
                alarmList.remove(position);
                adapter.notifyItemRemoved(position);
                saveAlarms();
                updateEmptyState();
                Toast.makeText(MainActivity.this, "Alarm Deleted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemClick(Alarm alarm, int position) {
                editingPosition = position;
                Intent intent = new Intent(MainActivity.this, AddAlarmActivity.class);
                intent.putExtra("edit_alarm", alarm);
                alarmActivityLauncher.launch(intent);
            }
        });
        
        rvAlarms.setLayoutManager(new LinearLayoutManager(this));
        rvAlarms.setAdapter(adapter);

        updateEmptyState();
        checkExactAlarmPermission();

        btnAddAlarm.setOnClickListener(view -> {
            editingPosition = -1;
            Intent intent = new Intent(MainActivity.this, AddAlarmActivity.class);
            alarmActivityLauncher.launch(intent);
        });
    }

    private void checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }
    }

    private void saveAlarms() {
        AlarmStorage.saveAlarms(this, alarmList);
    }

    private void rescheduleActiveAlarms() {
        for (Alarm alarm : alarmList) {
            if (alarm.isEnabled()) {
                scheduleAlarm(alarm);
            }
        }
    }

    private void updateEmptyState() {
        if (alarmList.isEmpty()) {
            tvNoAlarms.setVisibility(View.VISIBLE);
            rvAlarms.setVisibility(View.GONE);
        } else {
            tvNoAlarms.setVisibility(View.GONE);
            rvAlarms.setVisibility(View.VISIBLE);
        }
    }

    private void scheduleAlarm(Alarm alarm) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("challenge_type", alarm.getChallengeType());
        intent.putExtra("difficulty", alarm.getDifficultyLevel());
        intent.putExtra("alarm_id", alarm.getId());
        
        // Add unique data to intent so PendingIntents don't merge
        intent.setData(Uri.parse("alarm://" + alarm.getId()));
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, alarm.getId(), intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        long triggerTime = getNextTriggerTime(alarm);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }
        } catch (SecurityException e) {
            Toast.makeText(this, "Permission required for exact alarms", Toast.LENGTH_LONG).show();
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

    private void cancelAlarm(Alarm alarm) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setData(Uri.parse("alarm://" + alarm.getId()));
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, alarm.getId(), intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}
