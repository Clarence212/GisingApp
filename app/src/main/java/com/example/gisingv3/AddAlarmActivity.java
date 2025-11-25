package com.example.gisingv3;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

public class AddAlarmActivity extends AppCompatActivity {

    private TimePicker timePicker;
    private Spinner challengeSpinner;
    private Spinner difficultySpinner;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);

        timePicker = findViewById(R.id.timePicker);
        challengeSpinner = findViewById(R.id.challengeSpinner);
        difficultySpinner = findViewById(R.id.difficultySpinner);
        saveButton = findViewById(R.id.saveAlarmButton);

        // Populate the challenge spinner
        String[] challenges = {"Math Problem", "Shake Phone"};
        ArrayAdapter<String> challengeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, challenges);
        challengeSpinner.setAdapter(challengeAdapter);

        // Populate the difficulty spinner
        String[] difficulties = {"Easy", "Medium", "Hard"};
        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, difficulties);
        difficultySpinner.setAdapter(difficultyAdapter);

        saveButton.setOnClickListener(view -> {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            String challenge = "None";
            if (challengeSpinner.getSelectedItem() != null) {
                challenge = challengeSpinner.getSelectedItem().toString();
            }

            int difficulty = 1;
            if (difficultySpinner.getSelectedItem() != null) {
                String diffStr = difficultySpinner.getSelectedItem().toString();
                if (diffStr.equals("Medium")) difficulty = 2;
                else if (diffStr.equals("Hard")) difficulty = 3;
            }

            // Generate a somewhat unique ID
            int id = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);

            Alarm alarm = new Alarm(id, hour, minute, challenge, difficulty, true);

            Intent resultIntent = new Intent();
            resultIntent.putExtra("new_alarm", alarm);
            setResult(RESULT_OK, resultIntent);

            Toast.makeText(AddAlarmActivity.this, "Alarm Saved", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
