package com.example.smarthome.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
    }

    private void initUI() {
        // 设置设备列表
        deviceAdapter = new DeviceAdapter(this::onDeviceClick, this::onDeviceToggle);
        binding.recyclerViewDevices.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewDevices.setAdapter(deviceAdapter);
        binding.recyclerViewDevices.setHasFixedSize(true);

        // 设置下拉刷新
        binding.swipeRefreshLayout.setOnRefreshListener(this::refreshDevices);

        // 设置添加设备按钮
        binding.fabAddDevice.setOnClickListener(v -> showAddDeviceDialog());
    }

    private void loadDevices() {
        homeViewModel.loadDevices();
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
        deviceAdapter.notifyDataSetChanged();
        
        // 更新统计信息
        updateDeviceStats(devices);
        
        // 显示空状态或设备列表
        if (devices.isEmpty()) {
            binding.textViewEmptyState.setVisibility(View.VISIBLE);
            binding.recyclerViewDevices.setVisibility(View.GONE);
        } else {
            binding.textViewEmptyState.setVisibility(View.GONE);
            binding.recyclerViewDevices.setVisibility(View.VISIBLE);
        }
    }

    private void updateDeviceStats(List<DeviceItem> devices) {
        int totalDevices = devices.size();
        int onlineDevices = 0;
        int activeDevices = 0;
        
        for (DeviceItem device : devices) {
            if (device.isOnline()) {
                onlineDevices++;
            }
            if (device.isActive()) {
                activeDevices++;
            }
        }
        
        binding.textViewDeviceCount.setText(getString(R.string.device_count, totalDevices));
        binding.textViewOnlineCount.setText(getString(R.string.online_count, onlineDevices));
        binding.textViewActiveCount.setText(getString(R.string.active_count, activeDevices));
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
        // 跳转到设备详情页
        Log.d(TAG, "设备点击: " + device.getName());
        navigateToDeviceControl(device);
    }

    private void onDeviceToggle(DeviceItem device, boolean isEnabled) {
        Log.d(TAG, "切换设备状态: " + device.getName() + " 到 " + isEnabled);
        homeViewModel.toggleDeviceState(device, isEnabled);
    }

    private void navigateToDeviceControl(DeviceItem device) {
        // 使用Bundle传递设备信息
        Bundle bundle = new Bundle();
        bundle.putSerializable("device", device);
        
        // 跳转到设备控制Fragment
        // 这里将在DeviceControlFragment创建后实现完整的导航逻辑
        Toast.makeText(requireContext(), "打开设备控制: " + device.getName(), Toast.LENGTH_SHORT).show();
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