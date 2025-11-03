package com.example.smarthome.ui.features;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import com.example.smarthome.R;
import com.example.smarthome.ui.device.DeviceDetailActivity;

public class FeaturesFragment extends Fragment {

    private CardView deviceControlCard;
    private CardView sensorDataCard;
    private CardView sceneAutomationCard;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_features, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupClickListeners();
    }

    private void initViews(View view) {
        deviceControlCard = view.findViewById(R.id.card_device_control);
        sensorDataCard = view.findViewById(R.id.card_sensor_data);
        sceneAutomationCard = view.findViewById(R.id.card_scene_automation);
    }

    private void setupClickListeners() {
        deviceControlCard.setOnClickListener(v -> {
            // 跳转到设备控制页面
            Intent intent = new Intent(getActivity(), DeviceDetailActivity.class);
            startActivity(intent);
        });

        sensorDataCard.setOnClickListener(v -> {
            // TODO: 跳转到传感数据页面
        });

        sceneAutomationCard.setOnClickListener(v -> {
            // TODO: 跳转到场景联动页面
        });
    }
}