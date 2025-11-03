package com.example.smarthome.ui.device;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.example.smarthome.R;

public class DeviceDetailActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView deviceNameText;
    private TextView deviceStatusText;
    private TextView deviceLocationText;
    private TextView temperatureValueText;
    private TextView humidityValueText;
    private SwitchMaterial relaySwitch;
    private MaterialButton motorStartButton;
    private MaterialButton motorStopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);
        
        initViews();
        setupToolbar();
        setupControls();
        loadDeviceData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        deviceNameText = findViewById(R.id.text_device_name);
        deviceStatusText = findViewById(R.id.text_device_status);
        deviceLocationText = findViewById(R.id.text_device_location);
        temperatureValueText = findViewById(R.id.text_temperature_value);
        humidityValueText = findViewById(R.id.text_humidity_value);
        relaySwitch = findViewById(R.id.switch_relay);
        motorStartButton = findViewById(R.id.button_motor_start);
        motorStopButton = findViewById(R.id.button_motor_stop);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.device_detail);
        }
    }

    private void setupControls() {
        relaySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: 发送继电器控制命令
            if (isChecked) {
                // 打开继电器
            } else {
                // 关闭继电器
            }
        });

        motorStartButton.setOnClickListener(v -> {
            // TODO: 发送电机启动命令
        });

        motorStopButton.setOnClickListener(v -> {
            // TODO: 发送电机停止命令
        });
    }

    private void loadDeviceData() {
        // TODO: 从服务器加载设备数据
        // 临时显示模拟数据
        deviceNameText.setText("智能传感器 #001");
        deviceStatusText.setText("在线");
        deviceLocationText.setText("客厅");
        temperatureValueText.setText("24°C");
        humidityValueText.setText("65%");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}