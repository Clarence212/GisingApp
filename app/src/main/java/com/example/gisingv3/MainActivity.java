package com.example.gisingv3;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
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
                    // Use getSerializableExtra with compatibility check or just cast if targetSdk is low enough or suppressed
                    // For API 33+ (Tiramisu), getSerializableExtra(key, class) is preferred, but simpler method works for now
                    Alarm newAlarm = (Alarm) result.getData().getSerializableExtra("new_alarm");
                    if (newAlarm != null) {
                        alarmList.add(newAlarm);
                        adapter.notifyItemInserted(alarmList.size() - 1);
                        updateEmptyState();
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
}
