package com.example.gisingv3;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.Context;
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
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;

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

    // Shake challenge variables
    private SensorManager sensorManager;
    private int shakeCount = 0;
    private int requiredShakes = 10;
    private long lastShakeTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (keyguardManager != null) {
                keyguardManager.requestDismissKeyguard(this, null);
            }
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }

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
            if (challengeType == null) challengeType = "Math Problem";
        }
        
        setupChallenge();
        startAlarmEffects();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Toast.makeText(AlarmRingActivity.this, "Finish the challenge to stop the alarm!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupChallenge() {
        if ("Shake Phone".equals(challengeType)) {
            tvMathQuestion.setText("SHAKE TO STOP!");
            etMathAnswer.setVisibility(View.GONE);
            btnSolveChallenge.setVisibility(View.GONE);
            
            requiredShakes = 10 * difficulty;
            tvChallengePrompt.setText(String.format(Locale.getDefault(), "Shakes remaining: %d", requiredShakes));
            
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager != null) {
                Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                if (accelerometer != null) {
                    sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
                } else {
                    Toast.makeText(this, "Accelerometer not found! Switching to button.", Toast.LENGTH_LONG).show();
                    btnSolveChallenge.setVisibility(View.VISIBLE);
                    btnSolveChallenge.setText("Stop Alarm (Sensor Error)");
                    btnSolveChallenge.setOnClickListener(v -> onChallengeSolved());
                }
            }
        } else {
            generateMathProblem();
            btnSolveChallenge.setOnClickListener(v -> checkAnswer());
        }
    }

    private void startAlarmEffects() {
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        }
        
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
        } catch (Exception e) {
            // Log error
        }

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null) {
            long[] pattern = {0, 1000, 1000};
            vibrator.vibrate(pattern, 0);
        }
    }

    private void generateMathProblem() {
        Random random = new Random();
        int a, b;
        
        switch (difficulty) {
            case 1:
                a = random.nextInt(10) + 1;
                b = random.nextInt(10) + 1;
                correctAnswer = a + b;
                tvMathQuestion.setText(String.format(Locale.getDefault(), "%d + %d = ?", a, b));
                break;
            case 2:
                a = random.nextInt(50) + 10;
                b = random.nextInt(50) + 1;
                if (random.nextBoolean()) {
                    correctAnswer = a + b;
                    tvMathQuestion.setText(String.format(Locale.getDefault(), "%d + %d = ?", a, b));
                } else {
                    correctAnswer = a - b;
                    tvMathQuestion.setText(String.format(Locale.getDefault(), "%d - %d = ?", a, b));
                }
                break;
            case 3:
                a = random.nextInt(12) + 2;
                b = random.nextInt(12) + 2;
                correctAnswer = a * b;
                tvMathQuestion.setText(String.format(Locale.getDefault(), "%d × %d = ?", a, b));
                break;
        }
    }

    private void checkAnswer() {
        String input = etMathAnswer.getText().toString();
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
            Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
        }
    }

    private void onChallengeSolved() {
        stopAlarm();
        
        // Update notification to be dismissible
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "ALARM_CHANNEL")
                    .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                    .setContentTitle("ALARM")
                    .setContentText("Challenge solved! Swipe to dismiss.")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setOngoing(false) // Make it swipeable
                    .setAutoCancel(true);
            
            notificationManager.notify(1, builder.build());
        }

        Toast.makeText(this, "Good Morning! Swipe notification to dismiss.", Toast.LENGTH_LONG).show();
        finish();
    }

    private void stopAlarm() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
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
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float gForce = (float) Math.sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH;

            if (gForce > 2.0f) {
                long now = System.currentTimeMillis();
                if (lastShakeTime + 300 > now) {
                    return;
                }
                lastShakeTime = now;
                shakeCount++;

                int remaining = Math.max(0, requiredShakes - shakeCount);
                tvChallengePrompt.setText(String.format(Locale.getDefault(), "Shakes remaining: %d", remaining));
                
                if (remaining == 0) {
                    onChallengeSolved();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAlarm();
    }
}
