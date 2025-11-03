package com.smarthome.sensor.controller;

import com.smarthome.common.model.ApiResponse;
import com.smarthome.sensor.entity.Sensor;
import com.smarthome.sensor.service.SensorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 传感器控制器
 */
@RestController
@RequestMapping("/api/sensors")
@RequiredArgsConstructor
public class SensorController {
    
    private final SensorService sensorService;
    
    /**
     * 获取所有传感器
     */
    @GetMapping
    public ApiResponse<List<Sensor>> getAllSensors() {
        List<Sensor> sensors = sensorService.getAllSensors();
        return ApiResponse.success(sensors);
    }
    
    /**
     * 根据ID获取传感器
     */
    @GetMapping("/{id}")
    public ApiResponse<Sensor> getSensorById(@PathVariable Long id) {
        Sensor sensor = sensorService.getSensorById(id);
        return ApiResponse.success(sensor);
    }
    
    /**
     * 根据传感器ID获取传感器
     */
    @GetMapping("/sensor-id/{sensorId}")
    public ApiResponse<Sensor> getSensorBySensorId(@PathVariable String sensorId) {
        Sensor sensor = sensorService.getSensorBySensorId(sensorId);
        return ApiResponse.success(sensor);
    }
    
    /**
     * 根据设备ID获取传感器列表
     */
    @GetMapping("/device/{deviceId}")
    public ApiResponse<List<Sensor>> getSensorsByDeviceId(@PathVariable Long deviceId) {
        List<Sensor> sensors = sensorService.getSensorsByDeviceId(deviceId);
        return ApiResponse.success(sensors);
    }
    
    /**
     * 根据设备ID和传感器类型获取传感器列表
     */
    @GetMapping("/device/{deviceId}/type/{type}")
    public ApiResponse<List<Sensor>> getSensorsByDeviceIdAndType(@PathVariable Long deviceId, 
                                                                @PathVariable String type) {
        List<Sensor> sensors = sensorService.getSensorsByDeviceIdAndType(deviceId, type);
        return ApiResponse.success(sensors);
    }
    
    /**
     * 创建传感器
     */
    @PostMapping
    public ApiResponse<Sensor> createSensor(@RequestBody Sensor sensor) {
        Sensor createdSensor = sensorService.createSensor(sensor);
        ApiResponse<Sensor> response = ApiResponse.success(createdSensor);
        response.setMessage("传感器创建成功");
        return response;
    }
    
    /**
     * 更新传感器
     */
    @PutMapping("/{id}")
    public ApiResponse<Sensor> updateSensor(@PathVariable Long id, @RequestBody Sensor sensor) {
        Sensor updatedSensor = sensorService.updateSensor(id, sensor);
        ApiResponse<Sensor> response = ApiResponse.success(updatedSensor);
        response.setMessage("传感器更新成功");
        return response;
    }
    
    /**
     * 更新传感器状态
     */
    @PatchMapping("/{id}/status")
    public ApiResponse<Sensor> updateSensorStatus(@PathVariable Long id, @RequestParam Boolean enabled) {
        Sensor updatedSensor = sensorService.updateSensorStatus(id, enabled);
        ApiResponse<Sensor> response = ApiResponse.success(updatedSensor);
        response.setMessage("传感器状态更新成功");
        return response;
    }
    
    /**
     * 更新传感器值
     */
    @PatchMapping("/{sensorId}/value")
    public ApiResponse<Sensor> updateSensorValue(@PathVariable String sensorId, 
                                                @RequestParam String value) {
        Sensor updatedSensor = sensorService.updateSensorValue(sensorId, value);
        ApiResponse<Sensor> response = ApiResponse.success(updatedSensor);
        response.setMessage("传感器值更新成功");
        return response;
    }
    
    /**
     * 删除传感器
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteSensor(@PathVariable Long id) {
        sensorService.deleteSensor(id);
        ApiResponse<Void> response = ApiResponse.success(null);
        response.setMessage("传感器删除成功");
        return response;
    }
    
    /**
     * 检查传感器ID是否存在
     */
    @GetMapping("/exists/{sensorId}")
    public ApiResponse<Boolean> existsBySensorId(@PathVariable String sensorId) {
        boolean exists = sensorService.existsBySensorId(sensorId);
        return ApiResponse.success(exists);
    }
    
    /**
     * 获取所有启用的传感器
     */
    @GetMapping("/enabled")
    public ApiResponse<List<Sensor>> getEnabledSensors() {
        List<Sensor> sensors = sensorService.getEnabledSensors();
        return ApiResponse.success(sensors);
    }
    
    /**
     * 根据名称搜索传感器
     */
    @GetMapping("/search")
    public ApiResponse<List<Sensor>> searchSensorsByName(@RequestParam String name) {
        List<Sensor> sensors = sensorService.searchSensorsByName(name);
        return ApiResponse.success(sensors);
    }
    
    /**
     * 统计设备传感器数量
     */
    @GetMapping("/device/{deviceId}/count")
    public ApiResponse<Long> countSensorsByDeviceId(@PathVariable Long deviceId) {
        long count = sensorService.countSensorsByDeviceId(deviceId);
        return ApiResponse.success(count);
    }
    
    /**
     * 统计设备特定类型传感器数量
     */
    @GetMapping("/device/{deviceId}/type/{type}/count")
    public ApiResponse<Long> countSensorsByDeviceIdAndType(@PathVariable Long deviceId, 
                                                          @PathVariable String type) {
        long count = sensorService.countSensorsByDeviceIdAndType(deviceId, type);
        return ApiResponse.success(count);
    }
}