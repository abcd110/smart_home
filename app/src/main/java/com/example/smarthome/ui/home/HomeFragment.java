package com.example.smarthome.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarthome.R;
import com.example.smarthome.databinding.FragmentHomeBinding;
import com.example.smarthome.ui.home.DeviceAdapter;
import com.example.smarthome.model.DeviceItem;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

/**
 * 首页Fragment
 * 显示设备列表和智能家居概览
 */
public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private DeviceAdapter deviceAdapter;
    private SecurityEventAdapter securityAdapter;
    private List<DeviceItem> deviceList = new ArrayList<>();
    private boolean isRefreshing = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViewModel();
        initUI();
        loadDevices();
    }

    private void initViewModel() {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        homeViewModel.getDeviceList().observe(getViewLifecycleOwner(), this::updateDeviceList);
        homeViewModel.getLoading().observe(getViewLifecycleOwner(), this::updateLoadingState);
        homeViewModel.getError().observe(getViewLifecycleOwner(), this::showError);
        homeViewModel.getSensorSummary().observe(getViewLifecycleOwner(), summary -> {
            if (summary != null && binding != null) {
                binding.textViewTempValue.setText(summary.getTemperature() == null ? "—" : String.format("%.1f°C", summary.getTemperature()));
                binding.textViewHumidityValue.setText(summary.getHumidity() == null ? "—" : String.format("%.1f%%", summary.getHumidity()));
                binding.textViewGasValue.setText(summary.getGas() == null ? "—" : String.format("%.2f", summary.getGas()));
            }
        });
        homeViewModel.getSecurityEvents().observe(getViewLifecycleOwner(), events -> {
            if (events != null && binding != null) {
                if (securityAdapter != null) securityAdapter.setItems(events);
                int idx = events.size() - 1;
                if (idx >= 0 && !events.get(idx).isHandled()) {
                    new AlertDialog.Builder(requireContext())
                            .setTitle("安全事件")
                            .setMessage(events.get(idx).getMessage())
                            .setPositiveButton("确认", (d,w)-> homeViewModel.markSecurityEventHandled(idx))
                            .setNegativeButton("关闭报警", (d,w)-> {
                                String devId = events.get(idx).getDeviceId();
                                if (devId != null) homeViewModel.closeAlarm(devId);
                                homeViewModel.markSecurityEventHandled(idx);
                            })
                            .setNeutralButton("查看设备", (d,w)-> {
                                androidx.fragment.app.Fragment fragment = new com.example.smarthome.ui.features.FeaturesFragment();
                                requireActivity().getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.fragment_container, fragment)
                                        .addToBackStack(null)
                                        .commitAllowingStateLoss();
                            })
                            .show();
                }
            }
        });
    }

    private void initUI() {
        securityAdapter = new SecurityEventAdapter();
        binding.recyclerSecurity.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerSecurity.setAdapter(securityAdapter);

        // 设置下拉刷新
        binding.swipeRefreshLayout.setOnRefreshListener(this::refreshDevices);

        // 已移除添加设备悬浮按钮，避免与底部导航冲突

        // 主页不再包含设备列表入口
    }

    private void loadDevices() {
        homeViewModel.loadDevices();
        homeViewModel.loadSensorSummary();
        homeViewModel.startSse();
        // 简单响应式：定时刷新环境数据
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override public void run() {
                if (binding != null) {
                    homeViewModel.loadSensorSummary();
                    new Handler(Looper.getMainLooper()).postDelayed(this, 5000);
                }
            }
        }, 5000);
    }

    private void refreshDevices() {
        if (isRefreshing) return;
        isRefreshing = true;
        
        // 模拟网络请求延迟
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            homeViewModel.loadDevices();
            binding.swipeRefreshLayout.setRefreshing(false);
            isRefreshing = false;
        }, 1500);
    }

    private void updateDeviceList(List<DeviceItem> devices) {
        deviceList.clear();
        deviceList.addAll(devices);
        // 更新统计信息
        updateDeviceStats(devices);
    }

    private void updateDeviceStats(List<DeviceItem> devices) {
        int totalDevices = devices.size();
        int onlineDevices = 0;
        int activeDevices = 0;

        for (DeviceItem device : devices) {
            if (device.isOnline()) onlineDevices++;
            if (device.isActive()) activeDevices++;
        }

        binding.textViewDeviceCountValue.setText(String.valueOf(totalDevices));
        binding.textViewOnlineCountValue.setText(String.valueOf(onlineDevices));
        binding.textViewActiveCountValue.setText(String.valueOf(activeDevices));
    }

    private void updateLoadingState(boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.GONE);
        }
    }

    private void showError(String error) {
        if (error != null && !error.isEmpty()) {
            Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
        }
    }

    private void onDeviceClick(DeviceItem device) {
        Log.d(TAG, "设备点击: " + device.getName());
        String type = device.getType() == null ? "" : device.getType().toLowerCase();
        if (isActuator(type)) {
            navigateToDeviceControl(device);
        } else {
            Snackbar.make(binding.getRoot(), "该设备为传感器，仅支持查看", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void onDeviceToggle(DeviceItem device, boolean isEnabled) {
        Log.d(TAG, "切换设备状态: " + device.getName() + " 到 " + isEnabled);
        homeViewModel.toggleDeviceState(device, isEnabled);
    }

    private void navigateToDeviceControl(DeviceItem device) {
        Bundle bundle = new Bundle();
        bundle.putString("device_id", device.getDeviceId());
        bundle.putString("device_type", device.getType());
        androidx.fragment.app.Fragment fragment = com.example.smarthome.ui.control.DeviceControlFragment.newInstance(device.getDeviceId());
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    private boolean isActuator(String type) {
        if (type == null) return false;
        switch (type) {
            case "actuator":
            case "light":
            case "outlet":
            case "humidifier":
            case "curtain":
            case "lock":
            case "buzzer":
                return true;
            default:
                return false;
        }
    }

    private void showAddDeviceDialog() {
        // 显示添加设备对话框
        Toast.makeText(requireContext(), "添加新设备功能待实现", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
