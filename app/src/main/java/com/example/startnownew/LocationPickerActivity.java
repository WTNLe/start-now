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

import androidx.appcompat.app.AppCompatActivity;

import com.example.startnownew.databinding.ActivityLocationPickerBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationPickerActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private Marker selectedMarker;
    private ActivityLocationPickerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化 View Binding
        binding = ActivityLocationPickerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 如果有 Toolbar 需要使用，请确保布局中包含 ID 为 toolbar 的视图
        if (binding.toolbar != null) {
            setSupportActionBar(binding.toolbar);
        }

        // 初始化地图
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // 搜索框和按钮
        EditText etSearchLocation = binding.etSearchLocation;
        Button btnSearch = binding.btnSearch;

        // 设置搜索按钮点击事件
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String locationName = etSearchLocation.getText().toString().trim();
                if (!TextUtils.isEmpty(locationName)) {
                    searchLocation(locationName);
                } else {
                    Toast.makeText(LocationPickerActivity.this, "请输入有效的地址", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 设置保存位置的按钮
        binding.btnSaveLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedMarker != null) {
                    LatLng selectedLocation = selectedMarker.getPosition();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("latitude", selectedLocation.latitude);
                    resultIntent.putExtra("longitude", selectedLocation.longitude);
                    setResult(RESULT_OK, resultIntent);
                    finish(); // 结束 Activity 并返回数据
                } else {
                    Toast.makeText(LocationPickerActivity.this, "请先选择一个位置", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // 允许用户点击地图选择位置
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (selectedMarker != null) {
                    selectedMarker.remove();
                }
                selectedMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("选定的位置"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            }
        });
    }

    // 搜索位置的具体实现
    private void searchLocation(String locationName) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                // 在地图上移动并标记找到的位置
                if (selectedMarker != null) {
                    selectedMarker.remove();
                }
                selectedMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("搜索到的位置"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            } else {
                // 处理未找到位置的情况
                Toast.makeText(this, "未找到该地址，请尝试其他地址", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "无法搜索位置，请检查网络连接", Toast.LENGTH_SHORT).show();
        }
    }
}
