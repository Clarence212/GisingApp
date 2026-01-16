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
import android.os.Bundle;
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
                            // Updating an existing alarm
                            cancelAlarm(alarmList.get(editingPosition));
                            alarmList.set(editingPosition, alarm);
                            adapter.notifyItemChanged(editingPosition);
                            editingPosition = -1;
                        } else {
                            // Adding a new alarm
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

        // Load alarms from storage
        alarmList = AlarmStorage.loadAlarms(this);
        if (alarmList == null) {
            alarmList = new ArrayList<>();
        }

        rescheduleActiveAlarms();

        adapter = new AlarmAdapter(alarmList, new AlarmAdapter.OnAlarmListener() {
            @Override
            public void onToggle(Alarm alarm, boolean isEnabled) {
                if (isEnabled) {
                    scheduleAlarm(alarm);
                } else {
                    cancelAlarm(alarm);
                }
                saveAlarms(); // Save changes
            }

            @Override
            public void onDelete(Alarm alarm, int position) {
                cancelAlarm(alarm);
                alarmList.remove(position);
                adapter.notifyItemRemoved(position);
                saveAlarms(); // Save changes
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

        btnAddAlarm.setOnClickListener(view -> {
            editingPosition = -1;
            Intent intent = new Intent(MainActivity.this, AddAlarmActivity.class);
            alarmActivityLauncher.launch(intent);
        });
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
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, alarm.getId(), intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, alarm.getHour());
        calendar.set(Calendar.MINUTE, alarm.getMinute());
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                } else {
                     alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
            Toast.makeText(this, "Alarm set for " + alarm.getTimeString(), Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(this, "Permission required for exact alarms", Toast.LENGTH_LONG).show();
        }
    }

    private void cancelAlarm(Alarm alarm) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, alarm.getId(), intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}