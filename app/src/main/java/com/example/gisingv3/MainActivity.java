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

    private final ActivityResultLauncher<Intent> addAlarmLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Alarm newAlarm = (Alarm) result.getData().getSerializableExtra("new_alarm");
                    if (newAlarm != null) {
                        alarmList.add(newAlarm);
                        adapter.notifyItemInserted(alarmList.size() - 1);
                        updateEmptyState();
                        scheduleAlarm(newAlarm);
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

        alarmList = new ArrayList<>();
        adapter = new AlarmAdapter(alarmList);
        adapter.setOnAlarmToggleListener((alarm, isEnabled) -> {
            if (isEnabled) {
                scheduleAlarm(alarm);
            } else {
                cancelAlarm(alarm);
            }
        });
        
        rvAlarms.setLayoutManager(new LinearLayoutManager(this));
        rvAlarms.setAdapter(adapter);

        updateEmptyState();

        btnAddAlarm.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddAlarmActivity.class);
            addAlarmLauncher.launch(intent);
        });
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
        
        // Use a unique request code for each alarm
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, alarm.getId(), intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, alarm.getHour());
        calendar.set(Calendar.MINUTE, alarm.getMinute());
        calendar.set(Calendar.SECOND, 0);

        // If the time is in the past, add one day
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                } else {
                    // Just set inexact if permission not granted, or prompt user (omitted for brevity)
                     alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
            Toast.makeText(this, "Alarm set for " + alarm.getTimeString(), Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(this, "Permission required to set exact alarms", Toast.LENGTH_LONG).show();
        }
    }

    private void cancelAlarm(Alarm alarm) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, alarm.getId(), intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        
        alarmManager.cancel(pendingIntent);
        Toast.makeText(this, "Alarm cancelled", Toast.LENGTH_SHORT).show();
    }
}
