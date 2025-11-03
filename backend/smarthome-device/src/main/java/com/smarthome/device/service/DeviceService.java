package com.smarthome.device.service;

import com.smarthome.device.entity.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 设备服务接口
 */
public interface DeviceService {
    
    /**
     * 根据ID获取设备
     */
    Device getDeviceById(Long id);
    
    /**
     * 根据设备ID获取设备
     */
    Device getDeviceByDeviceId(String deviceId);
    
    /**
     * 创建设备
     */
    Device createDevice(Device device);
    
    /**
     * 更新设备信息
     */
    Device updateDevice(Long id, Device device);
    
    /**
     * 删除设备
     */
    void deleteDevice(Long id);
    
    /**
     * 根据用户ID获取设备列表
     */
    List<Device> getDevicesByUserId(Long userId);
    
    /**
     * 根据用户ID和设备类型获取设备列表
     */
    List<Device> getDevicesByUserIdAndType(Long userId, String type);
    
    /**
     * 获取所有设备
     */
    List<Device> getAllDevices();
    
    /**
     * 分页获取设备
     */
    Page<Device> getDevicesByPage(Pageable pageable);
    
    /**
     * 根据设备名称搜索设备
     */
    List<Device> searchDevicesByName(String name);
    
    /**
     * 根据用户ID和设备名称搜索设备
     */
    List<Device> searchDevicesByUserIdAndName(Long userId, String name);
    
    /**
     * 启用/禁用设备
     */
    void toggleDeviceStatus(Long id, boolean enabled);
    
    /**
     * 更新设备在线状态
     */
    void updateDeviceOnlineStatus(String deviceId, boolean online);
    
    /**
     * 检查设备ID是否已存在
     */
    boolean isDeviceIdExists(String deviceId);
    
    /**
     * 统计用户设备数量
     */
    long countDevicesByUserId(Long userId);
    
    /**
     * 统计在线设备数量
     */
    long countOnlineDevices();
}