package com.smarthome.sensor.service.impl;

import com.smarthome.sensor.entity.Sensor;
import com.smarthome.sensor.repository.SensorRepository;
import com.smarthome.sensor.service.SensorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 传感器服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SensorServiceImpl implements SensorService {
    
    private final SensorRepository sensorRepository;
    
    @Override
    public List<Sensor> getAllSensors() {
        return sensorRepository.findAll();
    }
    
    @Override
    public Sensor getSensorById(Long id) {
        return sensorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("传感器不存在，ID: " + id));
    }
    
    @Override
    public Sensor getSensorBySensorId(String sensorId) {
        return sensorRepository.findBySensorId(sensorId)
                .orElseThrow(() -> new RuntimeException("传感器不存在，传感器ID: " + sensorId));
    }
    
    @Override
    public List<Sensor> getSensorsByDeviceId(Long deviceId) {
        return sensorRepository.findByDeviceId(deviceId);
    }
    
    @Override
    public List<Sensor> getSensorsByDeviceIdAndType(Long deviceId, String type) {
        return sensorRepository.findByDeviceIdAndType(deviceId, type);
    }
    
    @Override
    public Sensor createSensor(Sensor sensor) {
        // 检查传感器ID是否已存在
        if (sensorRepository.existsBySensorId(sensor.getSensorId())) {
            throw new RuntimeException("传感器ID已存在: " + sensor.getSensorId());
        }
        
        // 设置默认值
        if (sensor.getEnabled() == null) {
            sensor.setEnabled(true);
        }
        
        Sensor savedSensor = sensorRepository.save(sensor);
        log.info("创建传感器成功，ID: {}, 传感器ID: {}", savedSensor.getId(), savedSensor.getSensorId());
        return savedSensor;
    }
    
    @Override
    public Sensor updateSensor(Long id, Sensor sensor) {
        Sensor existingSensor = getSensorById(id);
        
        // 检查传感器ID是否重复（如果修改了传感器ID）
        if (!existingSensor.getSensorId().equals(sensor.getSensorId()) && 
            sensorRepository.existsBySensorId(sensor.getSensorId())) {
            throw new RuntimeException("传感器ID已存在: " + sensor.getSensorId());
        }
        
        // 更新字段
        existingSensor.setName(sensor.getName());
        existingSensor.setType(sensor.getType());
        existingSensor.setSensorId(sensor.getSensorId());
        existingSensor.setDeviceId(sensor.getDeviceId());
        existingSensor.setDescription(sensor.getDescription());
        existingSensor.setEnabled(sensor.getEnabled());
        existingSensor.setUnit(sensor.getUnit());
        existingSensor.setMinValue(sensor.getMinValue());
        existingSensor.setMaxValue(sensor.getMaxValue());
        existingSensor.setCurrentValue(sensor.getCurrentValue());
        existingSensor.setLastReadingTime(sensor.getLastReadingTime());
        
        Sensor updatedSensor = sensorRepository.save(existingSensor);
        log.info("更新传感器成功，ID: {}", id);
        return updatedSensor;
    }
    
    @Override
    public Sensor updateSensorStatus(Long id, Boolean enabled) {
        Sensor sensor = getSensorById(id);
        sensor.setEnabled(enabled);
        
        Sensor updatedSensor = sensorRepository.save(sensor);
        log.info("更新传感器状态成功，ID: {}, 状态: {}", id, enabled);
        return updatedSensor;
    }
    
    @Override
    public Sensor updateSensorValue(String sensorId, String value) {
        Sensor sensor = getSensorBySensorId(sensorId);
        
        try {
            BigDecimal numericValue = new BigDecimal(value);
            sensor.setCurrentValue(numericValue);
            sensor.setLastReadingTime(LocalDateTime.now());
            
            Sensor updatedSensor = sensorRepository.save(sensor);
            log.debug("更新传感器值成功，传感器ID: {}, 值: {}", sensorId, value);
            return updatedSensor;
        } catch (NumberFormatException e) {
            throw new RuntimeException("传感器值格式错误: " + value);
        }
    }
    
    @Override
    public void deleteSensor(Long id) {
        Sensor sensor = getSensorById(id);
        sensorRepository.delete(sensor);
        log.info("删除传感器成功，ID: {}", id);
    }
    
    @Override
    public boolean existsBySensorId(String sensorId) {
        return sensorRepository.existsBySensorId(sensorId);
    }
    
    @Override
    public List<Sensor> getEnabledSensors() {
        return sensorRepository.findByEnabledTrue();
    }
    
    @Override
    public List<Sensor> searchSensorsByName(String name) {
        return sensorRepository.findByNameContaining(name);
    }
    
    @Override
    public long countSensorsByDeviceId(Long deviceId) {
        return sensorRepository.countByDeviceId(deviceId);
    }
    
    @Override
    public long countSensorsByDeviceIdAndType(Long deviceId, String type) {
        return sensorRepository.countByDeviceIdAndType(deviceId, type);
    }
}