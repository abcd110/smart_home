package com.smarthome.device.controller;

import com.smarthome.common.model.ApiResponse;
import com.smarthome.device.entity.Device;
import com.smarthome.device.service.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 设备控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    /**
     * 根据ID获取设备
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Device>> getDeviceById(@PathVariable Long id) {
        log.info("获取设备信息: {}", id);
        Device device = deviceService.getDeviceById(id);
        ApiResponse<Device> response = ApiResponse.success(device);
        response.setMessage("获取设备信息成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 根据设备ID获取设备
     */
    @GetMapping("/device-id/{deviceId}")
    public ResponseEntity<ApiResponse<Device>> getDeviceByDeviceId(@PathVariable String deviceId) {
        log.info("根据设备ID获取设备: {}", deviceId);
        Device device = deviceService.getDeviceByDeviceId(deviceId);
        ApiResponse<Device> response = ApiResponse.success(device);
        response.setMessage("根据设备ID获取设备成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 创建设备
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Device>> createDevice(@RequestBody Device device) {
        log.info("创建设备: {}", device.getName());
        Device createdDevice = deviceService.createDevice(device);
        ApiResponse<Device> response = ApiResponse.success(createdDevice);
        response.setMessage("创建设备成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 更新设备信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Device>> updateDevice(@PathVariable Long id, @RequestBody Device device) {
        log.info("更新设备信息: {}", id);
        Device updatedDevice = deviceService.updateDevice(id, device);
        ApiResponse<Device> response = ApiResponse.success(updatedDevice);
        response.setMessage("更新设备信息成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 删除设备
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDevice(@PathVariable Long id) {
        log.info("删除设备: {}", id);
        deviceService.deleteDevice(id);
        ApiResponse<Void> response = ApiResponse.success(null);
        response.setMessage("删除设备成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 根据用户ID获取设备列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Device>>> getDevicesByUserId(@PathVariable Long userId) {
        log.info("根据用户ID获取设备列表: {}", userId);
        List<Device> devices = deviceService.getDevicesByUserId(userId);
        ApiResponse<List<Device>> response = ApiResponse.success(devices);
        response.setMessage("获取用户设备列表成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 根据用户ID和设备类型获取设备列表
     */
    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<ApiResponse<List<Device>>> getDevicesByUserIdAndType(
            @PathVariable Long userId, @PathVariable String type) {
        log.info("根据用户ID和设备类型获取设备列表: userId={}, type={}", userId, type);
        List<Device> devices = deviceService.getDevicesByUserIdAndType(userId, type);
        ApiResponse<List<Device>> response = ApiResponse.success(devices);
        response.setMessage("获取用户设备类型列表成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 获取所有设备
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Device>>> getAllDevices() {
        log.info("获取所有设备");
        List<Device> devices = deviceService.getAllDevices();
        ApiResponse<List<Device>> response = ApiResponse.success(devices);
        response.setMessage("获取所有设备成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 分页获取设备
     */
    @GetMapping("/page")
    public ResponseEntity<ApiResponse<Page<Device>>> getDevicesByPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("分页获取设备 - 页码: {}, 大小: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Device> devices = deviceService.getDevicesByPage(pageable);
        ApiResponse<Page<Device>> response = ApiResponse.success(devices);
        response.setMessage("分页获取设备成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 根据设备名称搜索设备
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Device>>> searchDevicesByName(@RequestParam String name) {
        log.info("搜索设备: {}", name);
        List<Device> devices = deviceService.searchDevicesByName(name);
        ApiResponse<List<Device>> response = ApiResponse.success(devices);
        response.setMessage("搜索设备成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 根据用户ID和设备名称搜索设备
     */
    @GetMapping("/user/{userId}/search")
    public ResponseEntity<ApiResponse<List<Device>>> searchDevicesByUserIdAndName(
            @PathVariable Long userId, @RequestParam String name) {
        log.info("根据用户ID搜索设备: userId={}, name={}", userId, name);
        List<Device> devices = deviceService.searchDevicesByUserIdAndName(userId, name);
        ApiResponse<List<Device>> response = ApiResponse.success(devices);
        response.setMessage("根据用户ID搜索设备成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 启用/禁用设备
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> toggleDeviceStatus(@PathVariable Long id, @RequestParam boolean enabled) {
        log.info("{}设备: {}", enabled ? "启用" : "禁用", id);
        deviceService.toggleDeviceStatus(id, enabled);
        ApiResponse<Void> response = ApiResponse.success(null);
        response.setMessage(enabled ? "启用设备成功" : "禁用设备成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 更新设备在线状态
     */
    @PatchMapping("/{deviceId}/online")
    public ResponseEntity<ApiResponse<Void>> updateDeviceOnlineStatus(
            @PathVariable String deviceId, @RequestParam boolean online) {
        log.info("更新设备在线状态: deviceId={}, online={}", deviceId, online);
        deviceService.updateDeviceOnlineStatus(deviceId, online);
        ApiResponse<Void> response = ApiResponse.success(null);
        response.setMessage(online ? "设备上线成功" : "设备下线成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 检查设备ID是否已存在
     */
    @GetMapping("/check-device-id")
    public ResponseEntity<ApiResponse<Boolean>> checkDeviceIdExists(@RequestParam String deviceId) {
        log.info("检查设备ID是否存在: {}", deviceId);
        boolean exists = deviceService.isDeviceIdExists(deviceId);
        ApiResponse<Boolean> response = ApiResponse.success(exists);
        response.setMessage("检查设备ID完成");
        return ResponseEntity.ok(response);
    }

    /**
     * 统计用户设备数量
     */
    @GetMapping("/user/{userId}/count")
    public ResponseEntity<ApiResponse<Long>> countDevicesByUserId(@PathVariable Long userId) {
        log.info("统计用户设备数量: {}", userId);
        long count = deviceService.countDevicesByUserId(userId);
        ApiResponse<Long> response = ApiResponse.success(count);
        response.setMessage("统计用户设备数量成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 统计在线设备数量
     */
    @GetMapping("/count/online")
    public ResponseEntity<ApiResponse<Long>> countOnlineDevices() {
        log.info("统计在线设备数量");
        long count = deviceService.countOnlineDevices();
        ApiResponse<Long> response = ApiResponse.success(count);
        response.setMessage("统计在线设备数量成功");
        return ResponseEntity.ok(response);
    }
}