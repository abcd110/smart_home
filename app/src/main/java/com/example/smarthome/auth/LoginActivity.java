package com.example.smarthome.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.smarthome.R;
import com.example.smarthome.MainActivity;
import com.example.smarthome.auth.AuthService;

/**
 * 登录Activity
 * 处理用户登录和注册功能
 */
public class LoginActivity extends AppCompatActivity {
    
    private static final String TAG = "LoginActivity";
    
    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnSignUp;
    private TextView tvTitle;
    private ProgressBar progressBar;
    private TextView tvStatus;
    
    private AuthService authService;
    private boolean isLoginMode = true; // true为登录模式，false为注册模式
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        initViews();
        initAuthService();
        setupListeners();
        
        // 设置返回键处理 - 现代方法替代已弃用的onBackPressed()
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isLoginMode) {
                    // 在登录模式下退出应用
                    finish();
                } else {
                    // 在注册模式下返回到登录模式
                    toggleMode();
                }
            }
        });
    }
    
    private void initViews() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnSignUp = findViewById(R.id.btn_signup);
        tvTitle = findViewById(R.id.tv_title);
        progressBar = findViewById(R.id.progress_bar);
        tvStatus = findViewById(R.id.tv_status);
    }
    
    private void initAuthService() {
        authService = new AuthService(this);
    }
    
    private void setupListeners() {
        btnLogin.setOnClickListener(v -> performAuth());
        btnSignUp.setOnClickListener(v -> toggleMode());
    }
    
    /**
     * 切换登录/注册模式
     */
    private void toggleMode() {
        isLoginMode = !isLoginMode;
        
        if (isLoginMode) {
            tvTitle.setText("用户登录");
            btnLogin.setText("登录");
            btnSignUp.setText("注册账号");
            tvStatus.setText("");
        } else {
            tvTitle.setText("用户注册");
            btnLogin.setText("注册");
            btnSignUp.setText("已有账号");
            tvStatus.setText("注册后需要邮箱验证");
        }
        
        // 清空输入框
        etEmail.setText("");
        etPassword.setText("");
        etEmail.requestFocus();
    }
    
    /**
     * 执行认证操作
     */
    private void performAuth() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        // 验证输入
        if (!validateInput(email, password)) {
            return;
        }
        
        // 显示加载状态
        showLoading(true);
        
        if (isLoginMode) {
            // 执行登录
            authService.signIn(email, password, new AuthService.AuthCallback() {
                @Override
                public void onSuccess(AuthService.AuthResponse response) {
                    runOnUiThread(() -> {
                        showLoading(false);
                        if (response.success) {
                            Toast.makeText(LoginActivity.this, response.message, Toast.LENGTH_SHORT).show();
                            navigateToMain();
                        } else {
                            showError(response.message);
                        }
                    });
                }
                
                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        showLoading(false);
                        showError(error);
                    });
                }
            });
        } else {
            // 执行注册
            authService.signUp(email, password, new AuthService.AuthCallback() {
                @Override
                public void onSuccess(AuthService.AuthResponse response) {
                    runOnUiThread(() -> {
                        showLoading(false);
                        if (response.success) {
                            Toast.makeText(LoginActivity.this, response.message, Toast.LENGTH_LONG).show();
                            // 注册成功后切换到登录模式
                            toggleMode();
                        } else {
                            showError(response.message);
                        }
                    });
                }
                
                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        showLoading(false);
                        showError(error);
                    });
                }
            });
        }
    }
    
    /**
     * 验证输入
     */
    private boolean validateInput(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("请输入邮箱");
            etEmail.requestFocus();
            return false;
        }
        
        if (!isValidEmail(email)) {
            etEmail.setError("请输入有效的邮箱地址");
            etEmail.requestFocus();
            return false;
        }
        
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("请输入密码");
            etPassword.requestFocus();
            return false;
        }
        
        if (password.length() < 6) {
            etPassword.setError("密码长度不能少于6位");
            etPassword.requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * 验证邮箱格式
     */
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    
    /**
     * 显示/隐藏加载状态
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
        btnSignUp.setEnabled(!show);
        etEmail.setEnabled(!show);
        etPassword.setEnabled(!show);
        
        if (show) {
            btnLogin.setText("处理中...");
        } else {
            btnLogin.setText(isLoginMode ? "登录" : "注册");
        }
    }
    
    /**
     * 显示错误信息
     */
    private void showError(String message) {
        Log.e(TAG, "认证错误: " + message);
        tvStatus.setText(message);
        tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        tvStatus.setVisibility(View.VISIBLE);
        
        // 3秒后自动清除状态信息
        tvStatus.postDelayed(() -> {
            if (tvStatus.getText().toString().equals(message)) {
                tvStatus.setVisibility(View.GONE);
            }
        }, 3000);
    }
    
    /**
     * 跳转到主页面
     */
    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    
    // onBackPressed方法已移除，现在使用OnBackPressedCallback
}