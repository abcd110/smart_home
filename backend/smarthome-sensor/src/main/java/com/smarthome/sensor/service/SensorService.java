package com.smarthome.sensor.service;

import com.smarthome.sensor.entity.Sensor;
import java.util.List;

/**
 * 传感器服务接口
 */
public interface SensorService {
    
    /**
     * 获取所有传感器
     */
    List<Sensor> getAllSensors();
    
    /**
     * 根据ID获取传感器
     */
    Sensor getSensorById(Long id);
    
    /**
     * 根据传感器ID获取传感器
     */
    Sensor getSensorBySensorId(String sensorId);
    
    /**
     * 根据设备ID获取传感器列表
     */
    List<Sensor> getSensorsByDeviceId(Long deviceId);
    
    /**
     * 根据设备ID和传感器类型获取传感器列表
     */
    List<Sensor> getSensorsByDeviceIdAndType(Long deviceId, String type);
    
    /**
     * 创建传感器
     */
    Sensor createSensor(Sensor sensor);
    
    /**
     * 更新传感器
     */
    Sensor updateSensor(Long id, Sensor sensor);
    
    /**
     * 更新传感器状态
     */
    Sensor updateSensorStatus(Long id, Boolean enabled);
    
    /**
     * 更新传感器当前值
     */
    Sensor updateSensorValue(String sensorId, String value);
    
    /**
     * 删除传感器
     */
    void deleteSensor(Long id);
    
    /**
     * 检查传感器ID是否存在
     */
    boolean existsBySensorId(String sensorId);
    
    /**
     * 获取所有启用的传感器
     */
    List<Sensor> getEnabledSensors();
    
    /**
     * 根据名称搜索传感器
     */
    List<Sensor> searchSensorsByName(String name);
    
    /**
     * 统计设备传感器数量
     */
    long countSensorsByDeviceId(Long deviceId);
    
    /**
     * 统计设备特定类型传感器数量
     */
    long countSensorsByDeviceIdAndType(Long deviceId, String type);
}