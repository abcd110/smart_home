package com.example.smarthome;

import android.app.Application;

/**
 * SmartHome应用程序主类
 * 注意：Supabase依赖已被移除，现在使用本地存储方案
 */
public class SmarthomeApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        // 应用程序初始化逻辑
    }
}