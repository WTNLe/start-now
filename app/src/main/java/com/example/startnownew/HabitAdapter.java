package com.example.startnownew;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.ViewHolder> {
    private List<Habit> habitList;
    private OnItemClickListener listener;

    public HabitAdapter(List<Habit> habitList) {
        this.habitList = habitList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_habit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Habit habit = habitList.get(position);

        // 设置习惯名称
        holder.tvHabitName.setText(habit.getName());

        // 设置完成状态的图标
        holder.ivCompleted.setImageResource(habit.isCompleted() ? R.drawable.ic_yes : R.drawable.ic_no);

        // 设置具体的地址信息（如果有的话）
        if (habit.getAddress() != null && !habit.getAddress().isEmpty()) {
            holder.tvLocationInfo.setText(habit.getAddress());
            holder.tvLocationInfo.setVisibility(View.VISIBLE);
        } else if (habit.getLatitude() != null && habit.getLongitude() != null) {
            // 如果没有具体的地址信息但有经纬度，则显示经纬度
            String locationInfo = "位置: 经度 " + habit.getLongitude() + ", 纬度 " + habit.getLatitude();
            holder.tvLocationInfo.setText(locationInfo);
            holder.tvLocationInfo.setVisibility(View.VISIBLE);
        } else {
            // 如果没有位置信息，则隐藏 TextView
            holder.tvLocationInfo.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return habitList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHabitName;
        TextView tvLocationInfo; // 新增位置信息 TextView
        ImageView ivCompleted;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHabitName = itemView.findViewById(R.id.tvHabitName);
            tvLocationInfo = itemView.findViewById(R.id.tvLocationInfo); // 初始化位置信息 TextView
            ivCompleted = itemView.findViewById(R.id.ivCompleted);

            // 设置点击监听器
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
