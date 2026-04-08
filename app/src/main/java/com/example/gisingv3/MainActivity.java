package com.example.gisingv3;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvAlarms;
    private TextView tvNoAlarms;
    private AlarmAdapter adapter;
    private List<Alarm> alarmList;
    private int editingPosition = -1;

    private TextView tvCurrentTime, tvCurrentDate;
    private final Handler timeHandler = new Handler(Looper.getMainLooper());
    private final Runnable timeRunnable = new Runnable() {
        @Override
        public void run() {
            updateCurrentTimeAndDate();
            timeHandler.postDelayed(this, 1000);
        }
    };

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Notification permission is required for alarms to show.", Toast.LENGTH_LONG).show();
                }
            });

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
        // Force the app to stay in Light Mode and remove all dark mode related logic
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvCurrentDate = findViewById(R.id.tvCurrentDate);
        rvAlarms = findViewById(R.id.rvAlarms);
        tvNoAlarms = findViewById(R.id.tvNoAlarms);
        FloatingActionButton btnAddAlarm = findViewById(R.id.btnAddAlarm);

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
        checkPermissions();
        
        timeHandler.post(timeRunnable);

        btnAddAlarm.setOnClickListener(view -> {
            editingPosition = -1;
            Intent intent = new Intent(MainActivity.this, AddAlarmActivity.class);
            alarmActivityLauncher.launch(intent);
        });
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                Toast.makeText(this, "Please allow Exact Alarms for GisingApp to work correctly", Toast.LENGTH_LONG).show();
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (pm != null && !pm.isIgnoringBatteryOptimizations(getPackageName())) {
                @SuppressLint("BatteryLife")
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Intent fallback = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    startActivity(fallback);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        timeHandler.post(timeRunnable);
        refreshAlarmList();
    }

    @Override
    protected void onPause() {
        super.onPause();
        timeHandler.removeCallbacks(timeRunnable);
    }

    private void refreshAlarmList() {
        ArrayList<Alarm> updatedList = AlarmStorage.loadAlarms(this);
        if (updatedList != null) {
            alarmList.clear();
            alarmList.addAll(updatedList);
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
            updateEmptyState();
        }
    }

    private void updateCurrentTimeAndDate() {
        if (tvCurrentTime == null || tvCurrentDate == null) return;
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
        Date now = new Date();
        tvCurrentTime.setText(timeFormat.format(now).toUpperCase());
        tvCurrentDate.setText(dateFormat.format(now));
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
        if (alarmManager == null) return;

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("challenge_type", alarm.getChallengeType());
        intent.putExtra("difficulty", alarm.getDifficultyLevel());
        intent.putExtra("alarm_id", alarm.getId());
        intent.setData(Uri.parse("alarm://" + alarm.getId()));
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, alarm.getId(), intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        long triggerTime = getNextTriggerTime(alarm);

        Intent showIntent = new Intent(this, MainActivity.class);
        PendingIntent pShowIntent = PendingIntent.getActivity(this, 0, showIntent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(triggerTime, pShowIntent);
        
        try {
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
        } catch (SecurityException e) {
            // Fallback to inexact alarm if permission is missing
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
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
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
        if (alarmManager == null) return;

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setData(Uri.parse("alarm://" + alarm.getId()));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, alarm.getId(), intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timeHandler.removeCallbacks(timeRunnable);
    }
}
