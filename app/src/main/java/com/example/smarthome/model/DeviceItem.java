package com.example.smarthome.model;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.example.smarthome.BR;

import java.io.Serializable;

/**
 * 设备列表项模型类，用于UI显示
 */
public class DeviceItem extends BaseObservable implements Serializable {
    private String deviceId;
    private String name;
    private String type;
    private boolean isOnline;
    private boolean isActive;
    private String status;
    private long lastUpdateTime;
    private Object value;

    public DeviceItem() {
    }

    public DeviceItem(String deviceId, String name, String type, boolean isOnline, 
                     boolean isActive, String status, long lastUpdateTime, Object value) {
        this.deviceId = deviceId;
        this.name = name;
        this.type = type;
        this.isOnline = isOnline;
        this.isActive = isActive;
        this.status = status;
        this.lastUpdateTime = lastUpdateTime;
        this.value = value;
    }

    @Bindable
    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        notifyPropertyChanged(BR.deviceId);
    }

    @Bindable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        notifyPropertyChanged(BR.name);
    }

    @Bindable
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
        notifyPropertyChanged(BR.type);
    }

    @Bindable
    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
        notifyPropertyChanged(BR.online);
    }

    @Bindable
    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
        notifyPropertyChanged(BR.active);
    }

    @Bindable
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        notifyPropertyChanged(BR.status);
    }

    @Bindable
    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
        notifyPropertyChanged(BR.lastUpdateTime);
    }

    @Bindable
    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
        notifyPropertyChanged(BR.value);
    }

    /**
     * 获取设备友好类型名称
     */
    @Bindable
    public String getFriendlyType() {
        switch (type) {
            case "light": return "灯具";
            case "air_conditioner": return "空调";
            case "sensor": return "传感器";
            case "lock": return "门锁";
            case "curtain": return "窗帘";
            case "humidifier": return "加湿器";
            case "outlet": return "插座";
            case "switch": return "开关";
            case "thermostat": return "温控器";
            default: return type;
        }
    }

    /**
     * 获取状态颜色（用于UI显示）
     */
    public int getStatusColor() {
        if (!isOnline) return 0xFF757575; // 灰色 - 离线
        if (isActive) return 0xFF4CAF50;  // 绿色 - 开启
        return 0xFF9E9E9E;                // 浅灰色 - 关闭
    }

    /**
     * 格式化设备值
     */
    public String getFormattedValue() {
        if (value == null) return "";
        
        if (value instanceof Number) {
            if (value instanceof Integer || value instanceof Long) {
                return String.valueOf(value);
            } else {
                return String.format("%.1f", ((Number) value).doubleValue());
            }
        }
        return value.toString();
    }

    @Override
    public String toString() {
        return "DeviceItem{" +
                "deviceId='" + deviceId + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", isOnline=" + isOnline +
                ", isActive=" + isActive +
                ", status='" + status + '\'' +
                ", value=" + value +
                '}';
    }
}