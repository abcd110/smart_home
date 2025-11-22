package com.example.smarthome.ui.control;

import android.os.Bundle;
import androidx.core.os.BundleCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.SeekBar;
import android.text.InputType;
import android.widget.LinearLayout;
import androidx.appcompat.app.AlertDialog;

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
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });

        // 处理参数并加载设备数据
        Bundle arguments = getArguments();
        if (arguments != null) {
            if (arguments.containsKey(ARG_DEVICE)) {
                Device device = BundleCompat.getSerializable(arguments, ARG_DEVICE, Device.class);
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
            viewModel.pingDevice(device);
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

        String subtype = classifySubtype(device);
        int icon = R.drawable.ic_device_default;
        if ("temp_humidity".equals(subtype)) icon = R.drawable.wenshidu;
        else if ("gas".equals(subtype)) icon = R.drawable.meiqi;
        else if ("pir".equals(subtype)) icon = R.drawable.rentihongwaiganying;
        else if ("door".equals(subtype)) icon = R.drawable.security_lock;
        else if ("light".equals(subtype)) icon = R.drawable.zhinengdengkong;
        else if ("buzzer".equals(subtype)) icon = R.drawable.jiadian_fengmingqi;
        else if ("humidifier".equals(subtype)) icon = R.drawable.jiashiqi;
        binding.imageViewDeviceIcon.setImageResource(icon);
    }

    /**
     * 设置传感器类型设备的控制界面
     * @param device 设备对象
     */
    private void setupSensorControl(Device device) {
        // 显示传感器数据区域，隐藏控制按钮区域
        binding.constraintLayoutSensorData.setVisibility(View.VISIBLE);
        binding.constraintLayoutControlActions.setVisibility(View.GONE);

        String name = device.getName() != null ? device.getName() : device.getFriendlyDeviceType();
        binding.textViewSensorDataTitle.setText("传感器数据");
        String subtype = classifySubtype(device);
        String dataText = "暂无数据";
        if (device.getLatestSensorData() != null) {
            dataText = device.getLatestSensorData();
        }
        if ("temp_humidity".equals(subtype)) {
            binding.textViewSensorDataTitle.setText("温湿度数据");
        } else if ("gas".equals(subtype)) {
            binding.textViewSensorDataTitle.setText("可燃气浓度");
        } else if ("pir".equals(subtype)) {
            binding.textViewSensorDataTitle.setText("人体红外状态");
        } else if ("door".equals(subtype)) {
            binding.textViewSensorDataTitle.setText("门磁状态");
        }
        binding.textViewSensorData.setText(dataText);

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
        String subtype = classifySubtype(device);
        // 根据设备类型设置不同的操作按钮
        if ("light".equals(device.getDeviceType()) || "灯具".equals(device.getDeviceType()) || "light".equals(subtype)) {
            // 灯具特有的操作按钮
            binding.buttonAction1.setText(R.string.action_brightness);
            binding.buttonAction2.setText(R.string.action_color);
            binding.buttonAction3.setText(R.string.action_timer);
            binding.buttonAction1.setIconResource(R.drawable.liangdu);
            binding.buttonAction2.setIconResource(R.drawable.yanse);
            binding.buttonAction3.setIconResource(R.drawable.ic_timer);
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
            
        } else if ("buzzer".equals(subtype) || "蜂鸣器".equals(device.getDeviceType())) {
            binding.buttonAction1.setText("响铃");
            binding.buttonAction2.setText("停止");
            binding.buttonAction3.setText("定时响铃");
            binding.buttonAction3.setVisibility(View.VISIBLE);
            binding.buttonAction1.setIconResource(R.drawable.bell);
            binding.buttonAction2.setIconResource(R.drawable.tingzhi);
            binding.buttonAction3.setIconResource(R.drawable.ic_timer);
        } else if ("humidifier".equals(subtype) || "加湿器".equals(device.getDeviceType())) {
            binding.buttonAction1.setText("开启");
            binding.buttonAction2.setText("关闭");
            binding.buttonAction3.setText("定时加湿");
            binding.buttonAction3.setVisibility(View.VISIBLE);
            binding.buttonAction1.setIconResource(R.drawable.jiashiqi);
            binding.buttonAction2.setIconResource(R.drawable.jiashiqi);
            binding.buttonAction3.setIconResource(R.drawable.ic_timer);
        }

        // 设置操作按钮点击事件
        binding.buttonAction1.setOnClickListener(v -> {
            if ("light".equals(subtype)) {
                showBrightnessDialog(device);
            } else if ("curtain".equals(device.getDeviceType())) {
                showCurtainPositionDialog(device);
            } else if ("buzzer".equals(subtype)) {
                viewModel.buzzerOn(device);
            } else if ("humidifier".equals(subtype)) {
                viewModel.humidifierOn(device);
            } else {
                viewModel.performDeviceAction(device, 1);
            }
        });
        binding.buttonAction2.setOnClickListener(v -> {
            if ("light".equals(subtype)) {
                showColorDialog(device);
            } else if ("buzzer".equals(subtype)) {
                viewModel.buzzerOff(device);
            } else if ("humidifier".equals(subtype)) {
                viewModel.humidifierOff(device);
            } else if ("curtain".equals(device.getDeviceType())) {
                viewModel.curtainClose(device);
            } else {
                viewModel.performDeviceAction(device, 2);
            }
        });
        binding.buttonAction3.setOnClickListener(v -> {
            if ("light".equals(subtype)) {
                showTimerDialog(device);
            } else if ("buzzer".equals(subtype)) {
                showBuzzerScheduleDialog(device);
            } else if ("humidifier".equals(subtype)) {
                showHumidifierScheduleDialog(device);
            } else if ("curtain".equals(device.getDeviceType())) {
                showCurtainPositionDialog(device);
            } else {
                viewModel.performDeviceAction(device, 3);
            }
        });
    }

    private String classifySubtype(Device device) {
        String name = device.getName() != null ? device.getName() : "";
        String type = device.getDeviceType() != null ? device.getDeviceType().toLowerCase() : "";
        String n = name.toLowerCase();
        if (n.contains("温湿度")) return "temp_humidity";
        if (n.contains("可燃气") || n.contains("煤气")) return "gas";
        if (n.contains("人体红外") || n.contains("pir")) return "pir";
        if (n.contains("门磁") || n.contains("door")) return "door";
        if (n.contains("蜂鸣器") || n.contains("buzzer")) return "buzzer";
        if (n.contains("灯")) return "light";
        if (n.contains("加湿器") || n.contains("humidifier")) return "humidifier";
        return type;
    }

    private void showBrightnessDialog(Device device) {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        SeekBar seekBar = new SeekBar(requireContext());
        seekBar.setMax(100);
        seekBar.setProgress(80);
        layout.addView(seekBar);
        new AlertDialog.Builder(requireContext())
                .setTitle("设置亮度")
                .setView(layout)
                .setPositiveButton("确定", (d, w) -> viewModel.setLightBrightness(device, seekBar.getProgress()))
                .setNegativeButton("取消", null)
                .show();
    }

    private void showColorDialog(Device device) {
        String[] options = new String[]{"暖光", "自然光", "冷光"};
        final int[] selected = {1};
        new AlertDialog.Builder(requireContext())
                .setTitle("选择灯光颜色")
                .setSingleChoiceItems(options, selected[0], (dialog, which) -> selected[0] = which)
                .setPositiveButton("确定", (d, w) -> {
                    String preset = options[selected[0]];
                    viewModel.setLightColorPreset(device, preset);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showTimerDialog(Device device) {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        EditText minutesInput = new EditText(requireContext());
        minutesInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        minutesInput.setHint("分钟数");
        String[] actions = new String[]{"定时开启", "定时关闭"};
        final int[] selectedAction = {0};
        new AlertDialog.Builder(requireContext())
                .setTitle("设置定时")
                .setView(minutesInput)
                .setSingleChoiceItems(actions, selectedAction[0], (dialog, which) -> selectedAction[0] = which)
                .setPositiveButton("确定", (d, w) -> {
                    try {
                        int minutes = Integer.parseInt(minutesInput.getText().toString());
                        long ms = System.currentTimeMillis() + minutes * 60L * 1000L;
                        String at = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault()).format(new java.util.Date(ms));
                        String action = selectedAction[0] == 0 ? "ON" : "OFF";
                        viewModel.setTimer(device, at, action);
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "输入无效", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showCurtainPositionDialog(Device device) {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        SeekBar seekBar = new SeekBar(requireContext());
        seekBar.setMax(100);
        seekBar.setProgress(50);
        layout.addView(seekBar);
        new AlertDialog.Builder(requireContext())
                .setTitle("设置位置")
                .setView(layout)
                .setPositiveButton("确定", (d, w) -> viewModel.curtainPosition(device, seekBar.getProgress()))
                .setNegativeButton("取消", null)
                .show();
    }

    private void showBuzzerScheduleDialog(Device device) {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        EditText hourInput = new EditText(requireContext());
        hourInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        hourInput.setHint("小时(0-23)");
        EditText minuteInput = new EditText(requireContext());
        minuteInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        minuteInput.setHint("分钟(0-59)");
        EditText durationInput = new EditText(requireContext());
        durationInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        durationInput.setHint("持续秒数");
        layout.addView(hourInput);
        layout.addView(minuteInput);
        layout.addView(durationInput);
        new AlertDialog.Builder(requireContext())
                .setTitle("定时响铃")
                .setView(layout)
                .setPositiveButton("确定", (d, w) -> {
                    try {
                        int h = Integer.parseInt(hourInput.getText().toString());
                        int m = Integer.parseInt(minuteInput.getText().toString());
                        int sec = Integer.parseInt(durationInput.getText().toString());
                        viewModel.buzzerScheduleAt(device, h, m, sec);
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "输入无效", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showHumidifierScheduleDialog(Device device) {
        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("持续秒数，例如 600");
        new AlertDialog.Builder(requireContext())
                .setTitle("定时加湿")
                .setView(input)
                .setPositiveButton("确定", (d, w) -> {
                    try {
                        int seconds = Integer.parseInt(input.getText().toString());
                        viewModel.humidifierSchedule(device, seconds);
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "输入无效", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
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
