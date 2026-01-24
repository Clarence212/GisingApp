package com.example.gisingv3;

import androidx.appcompat.app.AppCompatActivity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class AddAlarmActivity extends AppCompatActivity {

    private TextView tvTimeInput;
    private LinearLayout cardMath, cardShake;
    private LinearLayout cardEasy, cardMedium, cardHard;
    private ImageView ivMathIcon, ivShakeIcon;
    private TextView tvMathLabel, tvShakeLabel;

    private TextView[] dayViews;
    private boolean[] daysSelected = {false, false, false, false, false, false, false};

    private int selectedHour = 7;
    private int selectedMinute = 0;
    private String selectedChallenge = "Math Problem";
    private int selectedDifficulty = 2;
    private int existingId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);

        tvTimeInput = findViewById(R.id.tvTimeInput);
        cardMath = findViewById(R.id.cardMath);
        cardShake = findViewById(R.id.cardShake);
        cardEasy = findViewById(R.id.cardEasy);
        cardMedium = findViewById(R.id.cardMedium);
        cardHard = findViewById(R.id.cardHard);
        
        ivMathIcon = findViewById(R.id.ivMathIcon);
        ivShakeIcon = findViewById(R.id.ivShakeIcon);
        tvMathLabel = findViewById(R.id.tvMathLabel);
        tvShakeLabel = findViewById(R.id.tvShakeLabel);
        
        Button saveButton = findViewById(R.id.saveAlarmButton);
        ImageButton btnBack = findViewById(R.id.btnBack);

        dayViews = new TextView[7];
        dayViews[0] = findViewById(R.id.daySun);
        dayViews[1] = findViewById(R.id.dayMon);
        dayViews[2] = findViewById(R.id.dayTue);
        dayViews[3] = findViewById(R.id.dayWed);
        dayViews[4] = findViewById(R.id.dayThu);
        dayViews[5] = findViewById(R.id.dayFri);
        dayViews[6] = findViewById(R.id.daySat);

        if (getIntent().hasExtra("edit_alarm")) {
            Alarm editAlarm = (Alarm) getIntent().getSerializableExtra("edit_alarm");
            if (editAlarm != null) {
                existingId = editAlarm.getId();
                selectedHour = editAlarm.getHour();
                selectedMinute = editAlarm.getMinute();
                selectedChallenge = editAlarm.getChallengeType();
                selectedDifficulty = editAlarm.getDifficultyLevel();
                if (editAlarm.getDaysSelected() != null) {
                    daysSelected = editAlarm.getDaysSelected().clone();
                }
            }
        }

        updateTimeDisplay();
        updateChallengeSelection();
        updateDifficultySelection();
        
        for (int i = 0; i < 7; i++) {
            final int dayIndex = i;
            dayViews[i].setOnClickListener(v -> {
                daysSelected[dayIndex] = !daysSelected[dayIndex];
                updateDaySelection(dayIndex);
            });
            updateDaySelection(i);
        }

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
            int id = (existingId != -1) ? existingId : (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
            Alarm alarm = new Alarm(id, selectedHour, selectedMinute, selectedChallenge, selectedDifficulty, true, daysSelected.clone());
            Intent resultIntent = new Intent();
            resultIntent.putExtra("new_alarm", alarm);
            setResult(RESULT_OK, resultIntent);
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
        tvTimeInput.setText(String.format(Locale.getDefault(), "%02d:%02d %s", hour12, selectedMinute, amPm));
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
            ivMathIcon.setColorFilter(Color.WHITE);
            tvMathLabel.setTextColor(Color.WHITE);
            tvMathLabel.setTypeface(null, android.graphics.Typeface.BOLD);

            cardShake.setBackgroundResource(R.drawable.bg_option_unselected);
            ivShakeIcon.setColorFilter(Color.parseColor("#888888"));
            tvShakeLabel.setTextColor(Color.parseColor("#888888"));
            tvShakeLabel.setTypeface(null, android.graphics.Typeface.NORMAL);
        } else {
            cardMath.setBackgroundResource(R.drawable.bg_option_unselected);
            ivMathIcon.setColorFilter(Color.parseColor("#888888"));
            tvMathLabel.setTextColor(Color.parseColor("#888888"));
            tvMathLabel.setTypeface(null, android.graphics.Typeface.NORMAL);

            cardShake.setBackgroundResource(R.drawable.bg_option_selected);
            ivShakeIcon.setColorFilter(Color.WHITE);
            tvShakeLabel.setTextColor(Color.WHITE);
            tvShakeLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        }
    }

    private void updateDifficultySelection() {
        cardEasy.setBackgroundResource(R.drawable.bg_difficulty_unselected);
        cardMedium.setBackgroundResource(R.drawable.bg_difficulty_unselected);
        cardHard.setBackgroundResource(R.drawable.bg_difficulty_unselected);
        
        setDifficultyChildStyle(cardEasy, false);
        setDifficultyChildStyle(cardMedium, false);
        setDifficultyChildStyle(cardHard, false);

        if (selectedDifficulty == 1) {
            cardEasy.setBackgroundResource(R.drawable.bg_difficulty_selected);
            setDifficultyChildStyle(cardEasy, true);
        } else if (selectedDifficulty == 2) {
            cardMedium.setBackgroundResource(R.drawable.bg_difficulty_selected);
            setDifficultyChildStyle(cardMedium, true);
        } else {
            cardHard.setBackgroundResource(R.drawable.bg_difficulty_selected);
            setDifficultyChildStyle(cardHard, true);
        }
    }
    
    private void setDifficultyChildStyle(LinearLayout container, boolean isSelected) {
        int color = isSelected ? Color.WHITE : Color.parseColor("#888888");
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof TextView) {
                TextView tv = (TextView) child;
                if (!tv.getText().toString().contains("★")) {
                    tv.setTextColor(color);
                    tv.setTypeface(null, isSelected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
                } else {
                    // Keep stars yellow when unselected, white when selected
                    tv.setTextColor(isSelected ? Color.WHITE : Color.parseColor("#FBC02D"));
                }
            }
        }
    }
}
