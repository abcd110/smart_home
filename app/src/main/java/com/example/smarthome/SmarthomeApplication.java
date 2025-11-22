package com.example.smarthome;

import android.app.Application;
import com.example.smarthome.utils.FilePicker;
import com.example.smarthome.utils.PermissionManager;

/**
 * SmartHome应用程序主类
 * 注意：Supabase依赖已被移除，现在使用本地存储方案
 */
public class SmarthomeApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        PermissionManager.init(getApplicationContext());
        FilePicker.init(getApplicationContext());
    }
}
