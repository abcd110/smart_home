package com.example.smarthome.model;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import com.google.gson.annotations.SerializedName;

import com.example.smarthome.BR;

import java.io.Serializable;
import java.util.Date;

/**
 * 设备模型类，用于表示智能家居设备
 */
public class Device extends BaseObservable implements Serializable {
    private String id;
    private String name;
    @SerializedName("type")
    private String deviceType;
    @SerializedName("device_id")
    private String deviceId;
    private String room;
    private boolean isOn;
    private boolean isOnline;
    @SerializedName("latest_sensor_data")
    private String latestSensorData;
    private Date lastActiveTime;
    private String userId;
    private Date createdAt;
    private Date updatedAt;

    public Device() {
    }

    public Device(String id, String name, String deviceType, String deviceId, String room) {
        this.id = id;
        this.name = name;
        this.deviceType = deviceType;
        this.deviceId = deviceId;
        this.room = room;
        this.isOn = false;
        this.isOnline = false;
        this.lastActiveTime = new Date();
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    @Bindable
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        notifyPropertyChanged(BR.id);
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
    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
        notifyPropertyChanged(BR.deviceType);
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
    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
        notifyPropertyChanged(BR.room);
    }

    @Bindable
    public boolean isOn() {
        return isOn;
    }

    public void setOn(boolean on) {
        isOn = on;
        notifyPropertyChanged(BR.on);
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
    public String getLatestSensorData() {
        return latestSensorData;
    }

    public void setLatestSensorData(String latestSensorData) {
        this.latestSensorData = latestSensorData;
        notifyPropertyChanged(BR.latestSensorData);
    }

    @Bindable
    public Date getLastActiveTime() {
        return lastActiveTime;
    }

    public void setLastActiveTime(Date lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
        notifyPropertyChanged(BR.lastActiveTime);
    }

    @Bindable
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
        notifyPropertyChanged(BR.userId);
    }

    @Bindable
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        notifyPropertyChanged(BR.createdAt);
    }

    @Bindable
    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
        notifyPropertyChanged(BR.updatedAt);
    }

    /**
     * 切换设备状态
     * @param isChecked 新的状态
     */
    public void toggleStatus(boolean isChecked) {
        setOn(isChecked);
        setLastActiveTime(new Date());
        setUpdatedAt(new Date());
    }

    /**
     * 检查设备是否为传感器类型
     * @return 是否为传感器
     */
    public boolean isSensorType() {
        return "sensor".equals(deviceType) || "温度传感器".equals(deviceType) || "湿度传感器".equals(deviceType);
    }

    /**
     * 获取设备的友好名称
     * @return 设备友好名称
     */
    @Bindable
    public String getFriendlyDeviceType() {
        if ("light".equals(deviceType)) return "灯具";
        if ("sensor".equals(deviceType)) return "传感器";
        if ("outlet".equals(deviceType)) return "插座";
        if ("switch".equals(deviceType)) return "开关";
        if ("curtain".equals(deviceType)) return "窗帘";
        if ("thermostat".equals(deviceType)) return "温控器";
        return deviceType;
    }

    @Override
    public String toString() {
        return "Device{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", deviceType='" + deviceType + '\'' +
                ", isOn=" + isOn +
                ", isOnline=" + isOnline +
                '}';
    }
}
