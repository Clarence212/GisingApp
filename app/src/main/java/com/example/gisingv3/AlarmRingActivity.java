package com.example.gisingv3;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AlarmRingActivity extends AppCompatActivity {

    private TextView tvRingTime;
    private TextView tvChallengePrompt;
    private Button btnSolveChallenge;
    
    // We would retrieve challenge type from intent
    private String challengeType = "None";
    private int difficulty = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_ring);
        
        tvRingTime = findViewById(R.id.tvRingTime);
        tvChallengePrompt = findViewById(R.id.tvChallengePrompt);
        btnSolveChallenge = findViewById(R.id.btnSolveChallenge);
        
        // Show current time
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        tvRingTime.setText(sdf.format(new Date()));
        
        // Retrieve alarm data from intent (assuming passed via AlarmReceiver)
        // In a real implementation, you'd pass Serializable Alarm object or fields
        if (getIntent() != null) {
            challengeType = getIntent().getStringExtra("challenge_type");
            difficulty = getIntent().getIntExtra("difficulty", 1);
            if (challengeType == null) challengeType = "None";
        }
        
        tvChallengePrompt.setText("Solve " + challengeType + " (" + getDifficultyString(difficulty) + ")");
        
        btnSolveChallenge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Here we would navigate to the specific challenge activity
                // For now, we simulate solving it
                Toast.makeText(AlarmRingActivity.this, "Challenge Solved! Alarm Off.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    
    private String getDifficultyString(int diff) {
        switch(diff) {
            case 1: return "Easy";
            case 2: return "Medium";
            case 3: return "Hard";
            default: return "Normal";
        }
    }
}