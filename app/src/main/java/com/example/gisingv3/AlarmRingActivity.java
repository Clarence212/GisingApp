package com.example.gisingv3;

import androidx.appcompat.app.AppCompatActivity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class AlarmRingActivity extends AppCompatActivity implements SensorEventListener {

    private TextView tvRingTime;
    private TextView tvMathQuestion;
    private EditText etMathAnswer;
    private Button btnSolveChallenge;
    private TextView tvChallengePrompt;
    
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;

    private String challengeType = "Math Problem";
    private int difficulty = 1;
    private int correctAnswer;
    private boolean isChallengeSolved = false;

    private SensorManager sensorManager;
    private int shakeCount = 0;
    private int requiredShakes = 10;
    private long lastShakeTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Configure flags to show over lockscreen and keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (keyguardManager != null) {
                keyguardManager.requestDismissKeyguard(this, null);
            }
        }

        hideSystemUI();
        setContentView(R.layout.activity_alarm_ring);
        
        tvRingTime = findViewById(R.id.tvRingTime);
        tvMathQuestion = findViewById(R.id.tvMathQuestion);
        etMathAnswer = findViewById(R.id.etMathAnswer);
        btnSolveChallenge = findViewById(R.id.btnSolveChallenge);
        tvChallengePrompt = findViewById(R.id.tvChallengePrompt);

        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        tvRingTime.setText(sdf.format(new Date()));

        if (getIntent() != null) {
            challengeType = getIntent().getStringExtra("challenge_type");
            difficulty = getIntent().getIntExtra("difficulty", 1);
        }
        
        setupChallenge();
        startAlarmEffects();
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // If the user tries to swipe away, bring the activity back
        if (!isChallengeSolved) {
            new Handler().postDelayed(() -> {
                if (!isChallengeSolved) {
                    Intent intent = new Intent(this, AlarmRingActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }, 500);
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (!isChallengeSolved) {
            Intent intent = new Intent(this, AlarmRingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private void setupChallenge() {
        if ("Shake Phone".equals(challengeType)) {
            tvMathQuestion.setText("SHAKE TO STOP!");
            etMathAnswer.setVisibility(View.GONE);
            btnSolveChallenge.setVisibility(View.GONE);
            requiredShakes = 10 * difficulty;
            tvChallengePrompt.setText("Shakes remaining: " + requiredShakes);
            
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager != null) {
                Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                if (accelerometer != null) {
                    sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
                }
            }
        } else {
            generateMathProblem();
            btnSolveChallenge.setOnClickListener(v -> checkAnswer());
        }
    }

    private void startAlarmEffects() {
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(this, alarmUri);
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build());
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {}

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(new long[]{0, 1000, 1000}, 0);
        }
    }

    private void generateMathProblem() {
        Random random = new Random();
        int a, b;
        switch (difficulty) {
            case 1: a = random.nextInt(10) + 1; b = random.nextInt(10) + 1; correctAnswer = a + b; tvMathQuestion.setText(a + " + " + b + " = ?"); break;
            case 2: a = random.nextInt(50) + 10; b = random.nextInt(50) + 1; correctAnswer = a + b; tvMathQuestion.setText(a + " + " + b + " = ?"); break;
            case 3: a = random.nextInt(12) + 2; b = random.nextInt(12) + 2; correctAnswer = a * b; tvMathQuestion.setText(a + " × " + b + " = ?"); break;
            default: a = random.nextInt(10) + 1; b = random.nextInt(10) + 1; correctAnswer = a + b; tvMathQuestion.setText(a + " + " + b + " = ?"); break;
        }
    }

    private void checkAnswer() {
        String input = etMathAnswer.getText().toString().trim();
        if (input.isEmpty()) return;

        try {
            int userAnswer = Integer.parseInt(input);
            if (userAnswer == correctAnswer) {
                onChallengeSolved();
            } else {
                Toast.makeText(this, "Wrong answer!", Toast.LENGTH_SHORT).show();
                etMathAnswer.setText("");
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show();
        }
    }

    private void onChallengeSolved() {
        isChallengeSolved = true;
        stopAlarm();
        Toast.makeText(this, "Good Morning!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void stopAlarm() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            } catch (Exception e) {}
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (vibrator != null) vibrator.cancel();
        if (sensorManager != null) sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float gForce = (float) Math.sqrt(Math.pow(event.values[0], 2) + Math.pow(event.values[1], 2) + Math.pow(event.values[2], 2)) / SensorManager.GRAVITY_EARTH;
            if (gForce > 2.2f) {
                long now = System.currentTimeMillis();
                if (lastShakeTime + 300 > now) return;
                lastShakeTime = now;
                shakeCount++;
                if (shakeCount >= requiredShakes) onChallengeSolved();
                else tvChallengePrompt.setText("Shakes remaining: " + (requiredShakes - shakeCount));
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Finish the challenge!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAlarm();
    }
}
