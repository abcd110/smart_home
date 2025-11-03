package com.example.smarthome.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.example.smarthome.R;

public class ProfileFragment extends Fragment {

    private TextView userNameText;
    private TextView userPhoneText;
    private MaterialCardView userInfoCard;
    private MaterialCardView securitySettingsCard;
    private MaterialCardView systemSettingsCard;
    private MaterialCardView aboutCard;
    private MaterialButton logoutButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupClickListeners();
        loadUserInfo();
    }

    private void initViews(View view) {
        userNameText = view.findViewById(R.id.text_user_name);
        userPhoneText = view.findViewById(R.id.text_user_phone);
        userInfoCard = view.findViewById(R.id.card_user_info);
        securitySettingsCard = view.findViewById(R.id.card_security_settings);
        systemSettingsCard = view.findViewById(R.id.card_system_settings);
        aboutCard = view.findViewById(R.id.card_about);
        logoutButton = view.findViewById(R.id.button_logout);
    }

    private void setupClickListeners() {
        userInfoCard.setOnClickListener(v -> {
            // TODO: 跳转到用户信息编辑页面
        });

        securitySettingsCard.setOnClickListener(v -> {
            // TODO: 跳转到安全设置页面
        });

        systemSettingsCard.setOnClickListener(v -> {
            // TODO: 跳转到系统设置页面
        });

        aboutCard.setOnClickListener(v -> {
            // TODO: 跳转到关于页面
        });

        logoutButton.setOnClickListener(v -> {
            // TODO: 实现退出登录功能
        });
    }

    private void loadUserInfo() {
        // TODO: 从本地存储或服务器加载用户信息
        // 临时显示模拟数据
        userNameText.setText("智慧社区用户");
        userPhoneText.setText("138****8888");
    }
}