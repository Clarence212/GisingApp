package com.example.gisingv3;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {

    private List<Alarm> alarmList;

    public AlarmAdapter(List<Alarm> alarmList) {
        this.alarmList = alarmList;
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alarm, parent, false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        Alarm alarm = alarmList.get(position);
        holder.tvTime.setText(alarm.getTimeString());
        holder.tvChallenge.setText(alarm.getChallengeType() + " - Lvl " + alarm.getDifficultyLevel());
        holder.switchAlarm.setChecked(alarm.isEnabled());
        
        holder.switchAlarm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            alarm.setEnabled(isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return alarmList.size();
    }

    public static class AlarmViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime;
        TextView tvChallenge;
        Switch switchAlarm;

        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvAlarmTime);
            tvChallenge = itemView.findViewById(R.id.tvAlarmChallenge);
            switchAlarm = itemView.findViewById(R.id.switchAlarm);
        }
    }
}
