package com.example.smarthome.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.card.MaterialCardView;
import com.example.smarthome.R;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerRecentEvents;
    
    // 新增UI元素
    private TextView temperatureText;
    private TextView humidityText;
    private MaterialCardView patternMorningCard;
    private MaterialCardView patternHeaterCard;
    private MaterialCardView roomLightCard;
    private MaterialCardView roomTemperatureCard;
    private MaterialCardView roomHumidityCard;
    private MaterialCardView roomMusicCard;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupSwipeRefresh();
        loadData();
    }

    private void initViews(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        recyclerRecentEvents = view.findViewById(R.id.recycler_recent_events);
        
        // 初始化新增的UI元素
        temperatureText = view.findViewById(R.id.text_temperature);
        humidityText = view.findViewById(R.id.text_humidity);
        
        // 初始化模式卡片
        patternMorningCard = view.findViewById(R.id.card_pattern_morning);
        patternHeaterCard = view.findViewById(R.id.card_pattern_heater);
        
        // 初始化房间控制卡片
        roomLightCard = view.findViewById(R.id.card_room_light);
        roomTemperatureCard = view.findViewById(R.id.card_room_temperature);
        roomHumidityCard = view.findViewById(R.id.card_room_humidity);
        roomMusicCard = view.findViewById(R.id.card_room_music);
        
        // 设置RecyclerView的LayoutManager
        recyclerRecentEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // 设置空的Adapter避免"No adapter attached"错误
        SecurityEventAdapter adapter = new SecurityEventAdapter(new ArrayList<>());
        recyclerRecentEvents.setAdapter(adapter);
        
        // 设置点击监听器
        setupCardClickListeners();
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // 模拟刷新数据
            swipeRefreshLayout.postDelayed(() -> {
                swipeRefreshLayout.setRefreshing(false);
                // TODO: 实际的数据刷新逻辑
            }, 1500);
        });
    }

    private void loadData() {
        // 加载主页数据
        loadGreetingMessage();
        loadEnvironmentData();
        loadSecurityEvents();
        // TODO: 网络状态检查
    }
    
    private void loadGreetingMessage() {
        // 根据时间显示问候语 - 这里可以直接在布局中设置，不需要动态更新
        // 或者可以通过其他方式实现问候功能
    }
    
    private void loadEnvironmentData() {
        // 模拟环境数据
        if (temperatureText != null) {
            temperatureText.setText("24°C");
        }
        if (humidityText != null) {
            humidityText.setText("65%");
        }
    }
    
    private void setupCardClickListeners() {
        // 模式卡片点击事件
        if (patternMorningCard != null) {
            patternMorningCard.setOnClickListener(v -> {
                // TODO: 执行外出模式
                showToast("外出模式已激活");
            });
        }
        
        if (patternHeaterCard != null) {
            patternHeaterCard.setOnClickListener(v -> {
                // TODO: 执行开启暖气模式
                showToast("暖气已开启");
            });
        }
        
        // 设备控制卡片点击事件
        if (roomLightCard != null) {
            roomLightCard.setOnClickListener(v -> {
                // TODO: 打开灯光控制详情页面
                showToast("灯光控制");
            });
        }
        
        if (roomTemperatureCard != null) {
            roomTemperatureCard.setOnClickListener(v -> {
                // TODO: 打开温度控制详情页面
                showToast("温度控制");
            });
        }
        
        if (roomHumidityCard != null) {
            roomHumidityCard.setOnClickListener(v -> {
                // TODO: 打开湿度控制详情页面
                showToast("湿度控制");
            });
        }
        
        if (roomMusicCard != null) {
            roomMusicCard.setOnClickListener(v -> {
                // TODO: 打开音乐控制详情页面
                showToast("音乐控制");
            });
        }
    }
    
    private void showToast(String message) {
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_SHORT).show();
        }
    }
    
    private void loadSecurityEvents() {
        // 模拟安防事件数据
        List<SecurityEvent> events = new ArrayList<>();
        events.add(new SecurityEvent("门窗传感器异常", "10:30", "客厅门窗传感器检测到异常开启"));
        events.add(new SecurityEvent("温度报警", "09:15", "厨房温度传感器检测到高温"));
        events.add(new SecurityEvent("设备离线", "08:45", "卧室智能开关失去连接"));
        
        // 更新RecyclerView数据
        if (recyclerRecentEvents.getAdapter() instanceof SecurityEventAdapter) {
            ((SecurityEventAdapter) recyclerRecentEvents.getAdapter()).updateEvents(events);
        }
    }
}