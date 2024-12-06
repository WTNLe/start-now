package com.example.startnownew;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationProviderClient fusedLocationClient;
    private HabitDBHelper dbHelper;
    private RecyclerView recyclerView;
    private HabitAdapter habitAdapter;
    private List<Habit> habitList;
    private FloatingActionButton fabAddHabit;
    private TextView tvDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化数据库、RecyclerView 和适配器
        dbHelper = new HabitDBHelper(this);
        recyclerView = findViewById(R.id.recyclerView);
        habitList = dbHelper.getAllHabits();
        habitAdapter = new HabitAdapter(habitList);
        recyclerView.setAdapter(habitAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 日期展示
        tvDate = findViewById(R.id.tvDate);
        updateDate();

        // 添加习惯的 FloatingActionButton
        fabAddHabit = findViewById(R.id.fab_add_habit);
        fabAddHabit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddHabitActivity.class);
                startActivity(intent);
            }
        });

        // 禁用含有位置的习惯的手动点击完成操作
        habitAdapter.setOnItemClickListener(new HabitAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Habit habit = habitList.get(position);
                if (habit.getLatitude() != null && habit.getLongitude() != null) {
                    Toast.makeText(MainActivity.this, "该习惯需要到达特定位置才能完成", Toast.LENGTH_SHORT).show();
                } else {
                    // 手动切换完成状态（适用于不含位置信息的习惯）
                    habit.setCompleted(!habit.isCompleted());
                    dbHelper.updateHabit(habit);
                    habitAdapter.notifyItemChanged(position);
                }
            }
        });

        // 初始化位置服务客户端
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 请求位置权限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }

        // 设置定时任务来在午夜更新日期
        scheduleMidnightUpdate();

        // 添加 ItemTouchHelper 实现滑动删除功能
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // 我们不需要移动功能
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // 获取被滑动的习惯
                int position = viewHolder.getAdapterPosition();
                Habit habitToDelete = habitList.get(position);

                // 从数据库中删除习惯
                dbHelper.deleteHabit(habitToDelete.getName());

                // 从列表中删除习惯，并通知适配器更新界面
                habitList.remove(position);
                habitAdapter.notifyItemRemoved(position);

                // 提示用户已删除
                Toast.makeText(MainActivity.this, "已删除习惯: " + habitToDelete.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    // 定时任务在午夜更新日期
    private void scheduleMidnightUpdate() {
        Handler handler = new Handler();
        long currentTimeMillis = System.currentTimeMillis();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTimeMillis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_YEAR, 1);

        long midnightTimeMillis = calendar.getTimeInMillis();
        long delay = midnightTimeMillis - currentTimeMillis;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateDate();
                scheduleMidnightUpdate();
            }
        }, delay);
    }

    // 更新显示的日期
    private void updateDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());
        tvDate.setText(today);
    }

    // 启动位置更新
    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000); // 每10秒请求一次位置
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    // 位置回调
    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            for (Location location : locationResult.getLocations()) {
                checkHabitCompletion(location);
            }
        }
    };

    // 检查用户是否到达含有位置信息的习惯位置
    private void checkHabitCompletion(Location currentLocation) {
        for (int i = 0; i < habitList.size(); i++) {
            Habit habit = habitList.get(i);
            if (habit.getLatitude() != null && habit.getLongitude() != null && !habit.isCompleted()) {
                Location habitLocation = new Location("");
                habitLocation.setLatitude(habit.getLatitude());
                habitLocation.setLongitude(habit.getLongitude());

                float distance = currentLocation.distanceTo(habitLocation);
                if (distance <= 100) { // 距离在100米以内则标记为完成
                    habit.setCompleted(true);
                    dbHelper.updateHabit(habit);
                    habitAdapter.notifyItemChanged(i);
                    Toast.makeText(this, "已完成习惯：" + habit.getName(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onResume() {
        super.onResume();
        habitList.clear();
        habitList.addAll(dbHelper.getAllHabits());
        habitAdapter.notifyDataSetChanged();

        // 再次检查位置权限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    // 处理位置权限请求结果
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "位置权限被拒绝，无法完成基于位置的习惯", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
