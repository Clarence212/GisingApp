package com.example.gisingv3;

import androidx.appcompat.app.AppCompatActivity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;

public class AddAlarmActivity extends AppCompatActivity {

    private TextView tvTimeInput;
    private LinearLayout cardMath, cardShake;
    private LinearLayout cardEasy, cardMedium, cardHard;
    private Button saveButton;
    private ImageButton btnBack;

    // Day TextViews
    private TextView[] dayViews;
    private boolean[] daysSelected = {false, false, false, false, false, false, false}; // S, M, T, W, T, F, S

    private int selectedHour = 7;
    private int selectedMinute = 0;
    private String selectedChallenge = "Math Problem";
    private int selectedDifficulty = 2; // 1=Easy, 2=Medium, 3=Hard

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);

        // Initialize Views
        tvTimeInput = findViewById(R.id.tvTimeInput);
        cardMath = findViewById(R.id.cardMath);
        cardShake = findViewById(R.id.cardShake);
        cardEasy = findViewById(R.id.cardEasy);
        cardMedium = findViewById(R.id.cardMedium);
        cardHard = findViewById(R.id.cardHard);
        saveButton = findViewById(R.id.saveAlarmButton);
        btnBack = findViewById(R.id.btnBack);

        // Initialize Day Views
        dayViews = new TextView[7];
        dayViews[0] = findViewById(R.id.daySun);
        dayViews[1] = findViewById(R.id.dayMon);
        dayViews[2] = findViewById(R.id.dayTue);
        dayViews[3] = findViewById(R.id.dayWed);
        dayViews[4] = findViewById(R.id.dayThu);
        dayViews[5] = findViewById(R.id.dayFri);
        dayViews[6] = findViewById(R.id.daySat);

        // Set initial state
        updateTimeDisplay();
        updateChallengeSelection();
        updateDifficultySelection();
        
        // Setup Day Click Listeners
        for (int i = 0; i < 7; i++) {
            final int dayIndex = i;
            dayViews[i].setOnClickListener(v -> {
                daysSelected[dayIndex] = !daysSelected[dayIndex];
                updateDaySelection(dayIndex);
            });
            updateDaySelection(i); // Initialize appearance
        }

        // Listeners
        btnBack.setOnClickListener(v -> finish());

        tvTimeInput.setOnClickListener(v -> showTimePicker());

        cardMath.setOnClickListener(v -> {
            selectedChallenge = "Math Problem";
            updateChallengeSelection();
        });

        cardShake.setOnClickListener(v -> {
            selectedChallenge = "Shake Phone";
            updateChallengeSelection();
        });

        cardEasy.setOnClickListener(v -> {
            selectedDifficulty = 1;
            updateDifficultySelection();
        });

        cardMedium.setOnClickListener(v -> {
            selectedDifficulty = 2;
            updateDifficultySelection();
        });

        cardHard.setOnClickListener(v -> {
            selectedDifficulty = 3;
            updateDifficultySelection();
        });

        saveButton.setOnClickListener(view -> {
            // Generate a somewhat unique ID
            int id = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);

            // Note: Currently Alarm model doesn't store days, but we could add it easily.
            // For now, we just save the other parameters as before.
            Alarm alarm = new Alarm(id, selectedHour, selectedMinute, selectedChallenge, selectedDifficulty, true);

            Intent resultIntent = new Intent();
            resultIntent.putExtra("new_alarm", alarm);
            setResult(RESULT_OK, resultIntent);

            Toast.makeText(AddAlarmActivity.this, "Alarm Saved", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    selectedHour = hourOfDay;
                    selectedMinute = minute;
                    updateTimeDisplay();
                }, selectedHour, selectedMinute, false);
        timePickerDialog.show();
    }

    private void updateTimeDisplay() {
        String amPm = selectedHour >= 12 ? "PM" : "AM";
        int hour12 = selectedHour % 12;
        if (hour12 == 0) hour12 = 12;
        String timeStr = String.format(Locale.getDefault(), "%02d:%02d %s", hour12, selectedMinute, amPm);
        tvTimeInput.setText(timeStr);
    }
    
    private void updateDaySelection(int index) {
        TextView dayView = dayViews[index];
        if (daysSelected[index]) {
            dayView.setBackgroundResource(R.drawable.bg_day_squircle_selected);
            dayView.setTextColor(Color.WHITE);
        } else {
            dayView.setBackgroundResource(R.drawable.bg_day_squircle_unselected);
            dayView.setTextColor(Color.parseColor("#555555"));
        }
    }

    private void updateChallengeSelection() {
        if ("Math Problem".equals(selectedChallenge)) {
            cardMath.setBackgroundResource(R.drawable.bg_option_selected);
            setChildTextColor(cardMath, true);
            
            cardShake.setBackgroundResource(R.drawable.bg_option_unselected);
            setChildTextColor(cardShake, false);
        } else {
            cardMath.setBackgroundResource(R.drawable.bg_option_unselected);
            setChildTextColor(cardMath, false);
            
            cardShake.setBackgroundResource(R.drawable.bg_option_selected);
            setChildTextColor(cardShake, true);
        }
    }

    private void updateDifficultySelection() {
        cardEasy.setBackgroundResource(R.drawable.bg_difficulty_unselected);
        cardMedium.setBackgroundResource(R.drawable.bg_difficulty_unselected);
        cardHard.setBackgroundResource(R.drawable.bg_difficulty_unselected);
        
        setChildTextColor(cardEasy, false);
        setChildTextColor(cardMedium, false);
        setChildTextColor(cardHard, false);

        if (selectedDifficulty == 1) {
            cardEasy.setBackgroundResource(R.drawable.bg_difficulty_selected);
            setChildTextColor(cardEasy, true);
        } else if (selectedDifficulty == 2) {
            cardMedium.setBackgroundResource(R.drawable.bg_difficulty_selected);
            setChildTextColor(cardMedium, true);
        } else {
            cardHard.setBackgroundResource(R.drawable.bg_difficulty_selected);
            setChildTextColor(cardHard, true);
        }
    }
    
    private void setChildTextColor(LinearLayout container, boolean isSelected) {
        int color;
        if (isSelected) {
            color = Color.WHITE;
        } else {
            color = Color.parseColor("#555555");
        }

        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof TextView) {
                TextView tv = (TextView) child;
                if (!tv.getText().toString().contains("★")) {
                    tv.setTextColor(color);
                }
            }
        }
    }
}
