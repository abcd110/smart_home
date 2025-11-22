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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.fragment.app.FragmentTransaction;
import android.os.Build;
import android.graphics.RenderEffect;
import android.graphics.Shader;

import com.example.smarthome.auth.AuthService;
import com.example.smarthome.auth.LoginActivity;
import com.example.smarthome.ui.home.HomeFragment;
import com.google.android.material.button.MaterialButton;

/**
 * 主Activity - 启动页面
 */
public class MainActivity extends AppCompatActivity {
    
    private static final String TAG = "MainActivity";
    private static final int SPLASH_DELAY = 2000; // 2秒启动延迟
    
    private AuthService authService;
    private BottomNavigationView bottomNav;
    
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
        bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new com.example.smarthome.ui.home.HomeFragment())
                        .commitAllowingStateLoss();
                return true;
            } else if (item.getItemId() == R.id.nav_features) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new com.example.smarthome.ui.features.FeaturesFragment())
                        .commitAllowingStateLoss();
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new com.example.smarthome.ui.profile.ProfileFragment())
                        .commitAllowingStateLoss();
                return true;
            }
            return true;
        });

        // 取消底部导航模糊效果
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
        updateUserInterface();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, new HomeFragment());
        ft.commitAllowingStateLoss();
        Log.i(TAG, "用户已登录，显示主界面");
    }
    
    /**
     * 更新用户界面
     */
    private void updateUserInterface() {
        // 顶部栏已移除，无需更新副标题
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
