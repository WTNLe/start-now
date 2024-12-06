package com.example.startnownew;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class AddHabitActivity extends AppCompatActivity {
    private EditText etHabitName;
    private Button btnPickLocation, btnSaveHabit;
    private Double selectedLatitude = null;
    private Double selectedLongitude = null;
    private String selectedAddress = null;
    private ActivityResultLauncher<Intent> locationPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_habit);

        // 初始化视图
        etHabitName = findViewById(R.id.etHabitName);
        btnPickLocation = findViewById(R.id.btnPickLocation);
        btnSaveHabit = findViewById(R.id.btnSaveHabit);

        // 初始化位置选择器启动器
        locationPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        double latitude = result.getData().getDoubleExtra("latitude", 0);
                        double longitude = result.getData().getDoubleExtra("longitude", 0);
                        selectedLatitude = latitude;
                        selectedLongitude = longitude;

                        // 使用 Geocoder 获取具体的地址信息
                        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                Address address = addresses.get(0);
                                selectedAddress = address.getAddressLine(0); // 获取详细地址
                                Toast.makeText(this, "位置已选择: " + selectedAddress, Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "无法获取地址，请检查网络连接", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // 设置位置选择按钮
        btnPickLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddHabitActivity.this, LocationPickerActivity.class);
                locationPickerLauncher.launch(intent);
            }
        });

        // 设置保存按钮
        btnSaveHabit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String habitName = etHabitName.getText().toString().trim();

                if (TextUtils.isEmpty(habitName)) {
                    Toast.makeText(AddHabitActivity.this, "请填写习惯名称", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 保存习惯到数据库
                HabitDBHelper dbHelper = new HabitDBHelper(AddHabitActivity.this);
                Habit newHabit = new Habit(habitName);
                newHabit.setLatitude(selectedLatitude);
                newHabit.setLongitude(selectedLongitude);
                newHabit.setAddress(selectedAddress);
                newHabit.setCompleted(false);
                dbHelper.insertHabit(newHabit); // 调用 insertHabit 方法

                Toast.makeText(AddHabitActivity.this, "习惯已保存", Toast.LENGTH_SHORT).show();

                // 结束当前活动并返回主界面
                finish();
            }
        });
    }
}

