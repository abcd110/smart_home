package com.example.smarthome.ui.control;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.smarthome.model.Device;
import com.example.smarthome.supabase.SupabaseClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 设备控制页面的ViewModel，处理设备控制的业务逻辑
 */
public class DeviceControlViewModel extends AndroidViewModel {

    private static final String TAG = "DeviceControlViewModel";
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final SupabaseClient supabaseClient;
    private final MutableLiveData<Device> device = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> statusMessage = new MutableLiveData<>();
    private final Gson gson = new Gson();

    public DeviceControlViewModel(@NonNull Application application) {
        super(application);
        this.supabaseClient = SupabaseClient.getInstance(application);
    }

    /**
     * 获取设备LiveData
     * @return 设备LiveData
     */
    public LiveData<Device> getDevice() {
        return device;
    }

    /**
     * 获取错误消息LiveData
     * @return 错误消息LiveData
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * 获取状态消息LiveData
     * @return 状态消息LiveData
     */
    public LiveData<String> getStatusMessage() {
        return statusMessage;
    }

    /**
     * 根据ID加载设备数据
     * @param deviceId 设备ID
     */
    public void loadDeviceById(String deviceId) {
        disposables.add(
                supabaseClient.getDeviceById(deviceId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    try {
                                        Device loadedDevice = gson.fromJson(response, Device.class);
                                        device.setValue(loadedDevice);
                                        statusMessage.setValue("设备数据加载成功");
                                    } catch (Exception e) {
                                        Log.e(TAG, "解析设备数据失败: " + e.getMessage());
                                        errorMessage.setValue("设备数据解析失败");
                                    }
                                },
                                throwable -> {
                                    Log.e(TAG, "加载设备失败: " + throwable.getMessage());
                                    errorMessage.setValue("加载设备失败: " + throwable.getMessage());
                                }
                        )
        );
    }

    /**
     * 切换设备状态
     * @param device 设备对象
     * @param isChecked 是否开启
     */
    public void toggleDeviceStatus(Device device, boolean isChecked) {
        // 更新本地状态
        device.setOn(isChecked);
        device.setLastActiveTime(new Date());
        device.setUpdatedAt(new Date());
        this.device.setValue(device);

        // 准备更新数据
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("is_on", isChecked);
        updateData.put("last_active_time", device.getLastActiveTime());
        updateData.put("updated_at", device.getUpdatedAt());

        // 发送MQTT命令
        sendDeviceCommand(device, isChecked ? "ON" : "OFF");

        // 更新Supabase数据库
        updateDeviceInDatabase(device.getId(), updateData);
    }

    /**
     * 发送设备控制命令
     * @param device 设备对象
     * @param command 命令内容
     */
    private void sendDeviceCommand(Device device, String command) {
        disposables.add(
                supabaseClient.sendMqttCommand(device.getDeviceId(), command)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    Log.d(TAG, "MQTT命令发送成功: " + command);
                                    statusMessage.setValue("控制命令已发送");
                                },
                                throwable -> {
                                    Log.e(TAG, "MQTT命令发送失败: " + throwable.getMessage());
                                    errorMessage.setValue("命令发送失败，请重试");
                                    // 恢复设备状态
                                    Device currentDevice = this.device.getValue();
                                    if (currentDevice != null) {
                                        currentDevice.setOn(!command.equals("ON"));
                                        this.device.setValue(currentDevice);
                                    }
                                }
                        )
        );
    }

    /**
     * 在数据库中更新设备信息
     * @param deviceId 设备ID
     * @param updateData 更新数据
     */
    private void updateDeviceInDatabase(String deviceId, Map<String, Object> updateData) {
        disposables.add(
                supabaseClient.updateDevice(deviceId, updateData)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    Log.d(TAG, "设备数据库更新成功");
                                },
                                throwable -> {
                                    Log.e(TAG, "设备数据库更新失败: " + throwable.getMessage());
                                    // 数据库更新失败不影响UI状态，仅记录日志
                                }
                        )
        );
    }

    /**
     * 刷新设备状态
     * @param deviceId 设备ID
     */
    public void refreshDeviceStatus(String deviceId) {
        loadDeviceById(deviceId);
    }

    /**
     * 执行设备特定操作
     * @param device 设备对象
     * @param actionType 操作类型
     */
    public void performDeviceAction(Device device, int actionType) {
        // 根据设备类型和操作类型执行不同的操作
        String command = ""; 
        String actionDescription = "";

        switch (actionType) {
            case 1: // 第一个按钮操作
                if (device.isSensorType()) {
                    viewSensorHistory(device);
                    return;
                } else if ("light".equals(device.getDeviceType()) || "灯具".equals(device.getDeviceType())) {
                    command = "BRIGHTNESS_SET";
                    actionDescription = "亮度调节";
                } else if ("outlet".equals(device.getDeviceType()) || "插座".equals(device.getDeviceType())) {
                    command = "POWER_CONSUMPTION";
                    actionDescription = "查看功耗";
                } else if ("curtain".equals(device.getDeviceType()) || "窗帘".equals(device.getDeviceType())) {
                    command = "OPEN";
                    actionDescription = "打开窗帘";
                }
                break;
            case 2: // 第二个按钮操作
                if ("light".equals(device.getDeviceType()) || "灯具".equals(device.getDeviceType())) {
                    command = "COLOR_SET";
                    actionDescription = "颜色设置";
                } else if ("outlet".equals(device.getDeviceType()) || "插座".equals(device.getDeviceType())) {
                    command = "TIMER_SET";
                    actionDescription = "定时设置";
                } else if ("curtain".equals(device.getDeviceType()) || "窗帘".equals(device.getDeviceType())) {
                    command = "CLOSE";
                    actionDescription = "关闭窗帘";
                }
                break;
            case 3: // 第三个按钮操作
                if ("light".equals(device.getDeviceType()) || "灯具".equals(device.getDeviceType())) {
                    command = "TIMER_SET";
                    actionDescription = "定时设置";
                } else if ("curtain".equals(device.getDeviceType()) || "窗帘".equals(device.getDeviceType())) {
                    command = "POSITION_SET";
                    actionDescription = "位置设置";
                }
                break;
        }

        if (!command.isEmpty()) {
            // 发送MQTT命令
            sendDeviceCommand(device, command);
            statusMessage.setValue("" + actionDescription + "已触发");
        }
    }

    /**
     * 查看传感器历史数据
     * @param device 设备对象
     */
    public void viewSensorHistory(Device device) {
        // 这里可以导航到历史数据页面或显示历史数据图表
        statusMessage.setValue("加载传感器历史数据...");
        
        // 获取传感器历史数据
        disposables.add(
                supabaseClient.getSensorHistory(device.getId(), 100) // 获取最近100条数据
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    Log.d(TAG, "获取传感器历史数据成功，共 " + response.length() + " 条记录");
                                    // 这里可以处理历史数据，如显示图表等
                                    statusMessage.setValue("历史数据已加载");
                                },
                                throwable -> {
                                    Log.e(TAG, "获取传感器历史数据失败: " + throwable.getMessage());
                                    errorMessage.setValue("获取历史数据失败");
                                }
                        )
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // 清理所有订阅，防止内存泄漏
        disposables.clear();
    }
}