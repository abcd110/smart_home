package com.example.smarthome.ui.home;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.smarthome.model.DeviceItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 首页ViewModel
 * 管理设备数据和业务逻辑
 */
public class HomeViewModel extends AndroidViewModel {

    private static final String TAG = "HomeViewModel";
    private final MutableLiveData<List<DeviceItem>> deviceListLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    public HomeViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * 获取设备列表LiveData
     */
    public MutableLiveData<List<DeviceItem>> getDeviceList() {
        return deviceListLiveData;
    }

    /**
     * 获取加载状态LiveData
     */
    public MutableLiveData<Boolean> getLoading() {
        return loadingLiveData;
    }

    /**
     * 获取错误信息LiveData
     */
    public MutableLiveData<String> getError() {
        return errorLiveData;
    }

    /**
     * 加载设备列表
     */
    public void loadDevices() {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);
        
        // 在实际应用中，这里应该从API或本地数据库加载设备数据
        executorService.execute(() -> {
            try {
                // 模拟网络请求延迟
                Thread.sleep(800);
                
                // 模拟设备数据
                List<DeviceItem> devices = generateMockDevices();
                
                // 在主线程更新UI
                deviceListLiveData.postValue(devices);
                loadingLiveData.postValue(false);
            } catch (Exception e) {
                Log.e(TAG, "加载设备失败: " + e.getMessage());
                String errorMessage = "加载设备失败，请稍后重试";
                
                errorLiveData.postValue(errorMessage);
                loadingLiveData.postValue(false);
            }
        });
    }

    /**
     * 切换设备状态
     */
    public void toggleDeviceState(DeviceItem device, boolean isEnabled) {
        // 在实际应用中，这里应该调用API来切换设备状态
        executorService.execute(() -> {
            try {
                // 模拟网络请求
                Thread.sleep(500);
                
                // 更新本地设备状态
                List<DeviceItem> currentDevices = deviceListLiveData.getValue();
                if (currentDevices != null) {
                    for (DeviceItem item : currentDevices) {
                        if (item.getDeviceId().equals(device.getDeviceId())) {
                            item.setActive(isEnabled);
                            break;
                        }
                    }
                    
                    // 在主线程更新UI
                    deviceListLiveData.postValue(new ArrayList<>(currentDevices));
                }
            } catch (Exception e) {
                Log.e(TAG, "切换设备状态失败: " + e.getMessage());
                String errorMessage = "控制设备失败，请稍后重试";
                
                errorLiveData.postValue(errorMessage);
            }
        });
    }

    /**
     * 模拟设备数据
     */
    private List<DeviceItem> generateMockDevices() {
        List<DeviceItem> devices = new ArrayList<>();
        
        // 模拟智能灯光
        devices.add(new DeviceItem(
                "device_001",
                "客厅灯光",
                "light",
                true,
                true,
                "开启",
                System.currentTimeMillis(),
                100
        ));
        
        // 模拟空调
        devices.add(new DeviceItem(
                "device_002",
                "主卧空调",
                "air_conditioner",
                true,
                false,
                "关闭",
                System.currentTimeMillis(),
                26
        ));
        
        // 模拟温湿度传感器
        devices.add(new DeviceItem(
                "device_003",
                "客厅温湿度",
                "sensor",
                true,
                true,
                "正常",
                System.currentTimeMillis(),
                25.5
        ));
        
        // 模拟智能门锁
        devices.add(new DeviceItem(
                "device_004",
                "前门智能锁",
                "lock",
                true,
                true,
                "已锁定",
                System.currentTimeMillis(),
                0
        ));
        
        // 模拟窗帘
        devices.add(new DeviceItem(
                "device_005",
                "主卧窗帘",
                "curtain",
                true,
                false,
                "关闭",
                System.currentTimeMillis(),
                0
        ));
        
        // 模拟加湿器
        devices.add(new DeviceItem(
                "device_006",
                "客厅加湿器",
                "humidifier",
                true,
                true,
                "开启",
                System.currentTimeMillis(),
                45
        ));
        
        return devices;
    }

    /**
     * 添加新设备
     */
    public void addDevice(DeviceItem device) {
        executorService.execute(() -> {
            try {
                // 模拟API调用
                Thread.sleep(800);
                
                List<DeviceItem> currentDevices = deviceListLiveData.getValue();
                if (currentDevices == null) {
                    currentDevices = new ArrayList<>();
                }
                
                // 检查设备ID是否已存在
                boolean deviceExists = currentDevices.stream()
                        .anyMatch(d -> d.getDeviceId().equals(device.getDeviceId()));
                
                if (!deviceExists) {
                    currentDevices.add(device);
                    
                    deviceListLiveData.postValue(new ArrayList<>(currentDevices));
                } else {
                    throw new Exception("设备ID已存在");
                }
            } catch (Exception e) {
                Log.e(TAG, "添加设备失败: " + e.getMessage());
                String errorMessage = "添加设备失败: " + e.getMessage();
                
                errorLiveData.postValue(errorMessage);
            }
        });
    }

    /**
     * 删除设备
     */
    public void deleteDevice(String deviceId) {
        executorService.execute(() -> {
            try {
                // 模拟API调用
                Thread.sleep(600);
                
                List<DeviceItem> currentDevices = deviceListLiveData.getValue();
                if (currentDevices != null) {
                    currentDevices.removeIf(d -> d.getDeviceId().equals(deviceId));
                    
                    deviceListLiveData.postValue(new ArrayList<>(currentDevices));
                }
            } catch (Exception e) {
                Log.e(TAG, "删除设备失败: " + e.getMessage());
                String errorMessage = "删除设备失败，请稍后重试";
                
                errorLiveData.postValue(errorMessage);
            }
        });
    }

    /**
     * 更新设备信息
     */
    public void updateDevice(DeviceItem updatedDevice) {
        executorService.execute(() -> {
            try {
                // 模拟API调用
                Thread.sleep(700);
                
                List<DeviceItem> currentDevices = deviceListLiveData.getValue();
                if (currentDevices != null) {
                    for (int i = 0; i < currentDevices.size(); i++) {
                        if (currentDevices.get(i).getDeviceId().equals(updatedDevice.getDeviceId())) {
                            currentDevices.set(i, updatedDevice);
                            break;
                        }
                    }
                    
                    deviceListLiveData.postValue(new ArrayList<>(currentDevices));
                }
            } catch (Exception e) {
                Log.e(TAG, "更新设备失败: " + e.getMessage());
                String errorMessage = "更新设备失败，请稍后重试";
                
                errorLiveData.postValue(errorMessage);
            }
        });
    }

    /**
     * 定期刷新设备状态
     */
    public void startPeriodicRefresh(long intervalMs) {
        executorService.scheduleAtFixedRate(this::loadDevices, intervalMs, intervalMs, TimeUnit.MILLISECONDS);
    }

    /**
     * 停止定期刷新
     */
    public void stopPeriodicRefresh() {
        executorService.shutdown();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopPeriodicRefresh();
    }
}