package com.example.smarthome;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smarthome.auth.AuthService;
import com.example.smarthome.auth.LoginActivity;
import com.google.android.material.button.MaterialButton;

/**
 * 主Activity - 启动页面
 */
public class MainActivity extends AppCompatActivity {
    
    private static final String TAG = "MainActivity";
    private static final int SPLASH_DELAY = 2000; // 2秒启动延迟
    
    private AuthService authService;
    private TextView tvUserInfo;
    private TextView tvUserEmail;
    private MaterialButton btnLogout;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        initAuthService();
        
        // 启动延迟处理
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            checkAuthentication();
        }, SPLASH_DELAY);
    }
    
    private void initViews() {
        tvUserInfo = findViewById(R.id.tv_user_info);
        tvUserEmail = findViewById(R.id.tv_user_email);
        btnLogout = findViewById(R.id.btn_logout);
        
        btnLogout.setOnClickListener(v -> performLogout());
    }
    
    private void initAuthService() {
        authService = new AuthService(this);
    }
    
    /**
     * 检查认证状态
     */
    private void checkAuthentication() {
        if (isLoggedIn()) {
            // 已登录，显示主页面
            showMainInterface();
        } else {
            // 未登录，跳转到登录页面
            navigateToLogin();
        }
    }
    
    /**
     * 检查用户是否已登录
     */
    private boolean isLoggedIn() {
        return authService.isLoggedIn();
    }
    
    /**
     * 显示主界面
     */
    private void showMainInterface() {
        // 获取用户信息并显示
        updateUserInterface();
        Log.i(TAG, "用户已登录，显示主界面");
    }
    
    /**
     * 更新用户界面
     */
    private void updateUserInterface() {
        // 获取当前用户信息
        String userEmail = authService.getCurrentUserEmail();
        if (userEmail != null) {
            tvUserInfo.setText("已登录用户");
            tvUserEmail.setText(userEmail);
            tvUserInfo.setVisibility(View.VISIBLE);
            tvUserEmail.setVisibility(View.VISIBLE);
            btnLogout.setVisibility(View.VISIBLE);
        } else {
            // 如果没有用户信息，显示默认状态
            tvUserInfo.setText("已登录用户");
            tvUserEmail.setText("user@example.com");
        }
    }
    
    /**
     * 执行登出操作
     */
    private void performLogout() {
        authService.signOut(new AuthService.AuthCallback() {
            @Override
            public void onSuccess(AuthService.AuthResponse response) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "已成功登出", Toast.LENGTH_SHORT).show();
                    navigateToLogin();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "登出失败: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    /**
     * 跳转到登录页面
     */
    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}