package com.example.gisingv3;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {

    private List<Alarm> alarmList;
    private OnAlarmListener listener;

    public interface OnAlarmListener {
        void onToggle(Alarm alarm, boolean isEnabled);
        void onDelete(Alarm alarm, int position);
        void onItemClick(Alarm alarm, int position);
    }

    public AlarmAdapter(List<Alarm> alarmList, OnAlarmListener listener) {
        this.alarmList = alarmList;
        this.listener = listener;
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
        holder.tvDays.setText(alarm.getDaysDisplay());
        holder.tvChallenge.setText(String.format(java.util.Locale.getDefault(), "%s - Lvl %d", alarm.getChallengeType(), alarm.getDifficultyLevel()));
        
        holder.switchAlarm.setOnCheckedChangeListener(null);
        holder.switchAlarm.setChecked(alarm.isEnabled());
        updateVisualState(holder, alarm.isEnabled());

        holder.switchAlarm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            alarm.setEnabled(isChecked);
            updateVisualState(holder, isChecked);
            if (listener != null) {
                listener.onToggle(alarm, isChecked);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(alarm, holder.getAdapterPosition());
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(alarm, holder.getAdapterPosition());
            }
        });
    }

    private void updateVisualState(AlarmViewHolder holder, boolean isEnabled) {
        float alpha = isEnabled ? 1.0f : 0.5f;
        holder.tvTime.setAlpha(alpha);
        holder.tvDays.setAlpha(alpha);
        holder.tvChallenge.setAlpha(alpha);
    }

    @Override
    public int getItemCount() {
        return alarmList.size();
    }

    public static class AlarmViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime;
        TextView tvDays;
        TextView tvChallenge;
        SwitchMaterial switchAlarm;
        ImageButton btnDelete;

        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvAlarmTime);
            tvDays = itemView.findViewById(R.id.tvAlarmDays);
            tvChallenge = itemView.findViewById(R.id.tvAlarmChallenge);
            switchAlarm = itemView.findViewById(R.id.switchAlarm);
            btnDelete = itemView.findViewById(R.id.btnDeleteAlarm);
        }
    }
}
