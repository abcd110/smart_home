package com.example.smarthome.ui.control;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.smarthome.R;
import com.example.smarthome.databinding.FragmentDeviceControlBinding;
import com.example.smarthome.model.Device;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 设备控制页面，用于详细控制和监控单个设备
 */
public class DeviceControlFragment extends Fragment {

    private FragmentDeviceControlBinding binding;
    private DeviceControlViewModel viewModel;
    private static final String ARG_DEVICE_ID = "device_id";
    private static final String ARG_DEVICE = "device";

    public static DeviceControlFragment newInstance(String deviceId) {
        DeviceControlFragment fragment = new DeviceControlFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DEVICE_ID, deviceId);
        fragment.setArguments(args);
        return fragment;
    }

    public static DeviceControlFragment newInstance(Device device) {
        DeviceControlFragment fragment = new DeviceControlFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DEVICE, device);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 使用DataBinding初始化布局
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_device_control, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化ViewModel
        viewModel = new ViewModelProvider(this).get(DeviceControlViewModel.class);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());

        // 设置返回按钮点击事件
        binding.toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        // 处理参数并加载设备数据
        Bundle arguments = getArguments();
        if (arguments != null) {
            if (arguments.containsKey(ARG_DEVICE)) {
                Device device = (Device) arguments.getSerializable(ARG_DEVICE);
                if (device != null) {
                    setupDeviceControl(device);
                }
            } else if (arguments.containsKey(ARG_DEVICE_ID)) {
                String deviceId = arguments.getString(ARG_DEVICE_ID);
                if (deviceId != null) {
                    loadDeviceData(deviceId);
                }
            }
        }

        // 设置状态观察
        viewModel.getDevice().observe(getViewLifecycleOwner(), device -> {
            if (device != null) {
                setupDeviceControl(device);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getStatusMessage().observe(getViewLifecycleOwner(), statusMessage -> {
            if (statusMessage != null) {
                Toast.makeText(requireContext(), statusMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 加载设备数据
     * @param deviceId 设备ID
     */
    private void loadDeviceData(String deviceId) {
        viewModel.loadDeviceById(deviceId);
    }

    /**
     * 设置设备控制界面
     * @param device 设备对象
     */
    private void setupDeviceControl(Device device) {
        // 更新标题
        binding.toolbar.setTitle(device.getName());

        // 设置设备基本信息
        updateDeviceInfo(device);

        // 根据设备类型显示不同的控制界面
        if (device.isSensorType()) {
            setupSensorControl(device);
        } else {
            setupControlActions(device);
        }

        // 设置开关控制
        binding.switchDevice.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.toggleDeviceStatus(device, isChecked);
        });

        // 设置刷新按钮
        binding.imageButtonRefresh.setOnClickListener(v -> {
            viewModel.refreshDeviceStatus(device.getId());
        });
    }

    /**
     * 更新设备基本信息
     * @param device 设备对象
     */
    private void updateDeviceInfo(Device device) {
        binding.textViewDeviceId.setText(device.getDeviceId());
        binding.textViewDeviceType.setText(device.getFriendlyDeviceType());
        binding.textViewRoom.setText(device.getRoom());
        binding.textViewLastActive.setText(formatLastActiveTime(device.getLastActiveTime()));
        binding.switchDevice.setChecked(device.isOn());
        binding.switchDevice.setEnabled(device.isOnline());

        // 更新状态指示器
        if (device.isOnline()) {
            binding.imageViewStatusIndicator.setImageResource(R.drawable.ic_status_online);
            binding.textViewStatus.setText(R.string.status_online);
        } else {
            binding.imageViewStatusIndicator.setImageResource(R.drawable.ic_status_online);
            binding.textViewStatus.setText(R.string.status_offline);
        }
    }

    /**
     * 设置传感器类型设备的控制界面
     * @param device 设备对象
     */
    private void setupSensorControl(Device device) {
        // 显示传感器数据区域，隐藏控制按钮区域
        binding.constraintLayoutSensorData.setVisibility(View.VISIBLE);
        binding.constraintLayoutControlActions.setVisibility(View.GONE);

        // 更新传感器数据
        if (device.getLatestSensorData() != null) {
            binding.textViewSensorData.setText(device.getLatestSensorData());
        }

        // 设置查看历史数据按钮
        binding.buttonViewHistory.setOnClickListener(v -> {
            viewModel.viewSensorHistory(device);
        });
    }

    /**
     * 设置控制类型设备的操作按钮
     * @param device 设备对象
     */
    private void setupControlActions(Device device) {
        // 显示控制按钮区域，隐藏传感器数据区域
        binding.constraintLayoutSensorData.setVisibility(View.GONE);
        binding.constraintLayoutControlActions.setVisibility(View.VISIBLE);

        // 根据设备类型设置不同的操作按钮
        if ("light".equals(device.getDeviceType()) || "灯具".equals(device.getDeviceType())) {
            // 灯具特有的操作按钮
            binding.buttonAction1.setText(R.string.action_brightness);
            binding.buttonAction2.setText(R.string.action_color);
            binding.buttonAction3.setText(R.string.action_timer);
        } else if ("outlet".equals(device.getDeviceType()) || "插座".equals(device.getDeviceType())) {
            // 插座特有的操作按钮
            binding.buttonAction1.setText(R.string.action_power_consumption);
            binding.buttonAction2.setText(R.string.action_timer);
            binding.buttonAction3.setText(R.string.action_schedule);
            binding.buttonAction3.setVisibility(View.GONE);
        } else if ("curtain".equals(device.getDeviceType()) || "窗帘".equals(device.getDeviceType())) {
            // 窗帘特有的操作按钮
            binding.buttonAction1.setText(R.string.action_open);
            binding.buttonAction2.setText(R.string.action_close);
            binding.buttonAction3.setText(R.string.action_position);
        }

        // 设置操作按钮点击事件
        binding.buttonAction1.setOnClickListener(v -> {
            viewModel.performDeviceAction(device, 1);
        });
        binding.buttonAction2.setOnClickListener(v -> {
            viewModel.performDeviceAction(device, 2);
        });
        binding.buttonAction3.setOnClickListener(v -> {
            viewModel.performDeviceAction(device, 3);
        });
    }

    /**
     * 格式化最后活跃时间
     * @param date 日期对象
     * @return 格式化后的时间字符串
     */
    private String formatLastActiveTime(Date date) {
        if (date == null) return "未知";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(date);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}