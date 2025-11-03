package com.smarthome.device.service.impl;

import com.smarthome.device.entity.Device;
import com.smarthome.device.repository.DeviceRepository;
import com.smarthome.device.service.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;

    @Override
    public Device getDeviceById(Long id) {
        log.info("根据ID获取设备: {}", id);
        return deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("设备不存在"));
    }

    @Override
    public Device getDeviceByDeviceId(String deviceId) {
        log.info("根据设备ID获取设备: {}", deviceId);
        return deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new RuntimeException("设备不存在"));
    }

    @Override
    public Device createDevice(Device device) {
        log.info("创建设备: {}", device.getName());
        
        // 检查设备ID是否已存在
        if (deviceRepository.existsByDeviceId(device.getDeviceId())) {
            throw new RuntimeException("设备ID已存在");
        }
        
        return deviceRepository.save(device);
    }

    @Override
    public Device updateDevice(Long id, Device device) {
        log.info("更新设备信息: {}", id);
        
        Device existingDevice = getDeviceById(id);
        
        // 更新基本信息
        existingDevice.setName(device.getName());
        existingDevice.setType(device.getType());
        existingDevice.setDescription(device.getDescription());
        existingDevice.setFirmwareVersion(device.getFirmwareVersion());
        existingDevice.setHardwareVersion(device.getHardwareVersion());
        existingDevice.setIpAddress(device.getIpAddress());
        existingDevice.setPort(device.getPort());
        
        // 如果提供了新设备ID，检查是否重复
        if (device.getDeviceId() != null && !device.getDeviceId().equals(existingDevice.getDeviceId())) {
            if (deviceRepository.existsByDeviceId(device.getDeviceId())) {
                throw new RuntimeException("设备ID已存在");
            }
            existingDevice.setDeviceId(device.getDeviceId());
        }
        
        return deviceRepository.save(existingDevice);
    }

    @Override
    public void deleteDevice(Long id) {
        log.info("删除设备: {}", id);
        Device device = getDeviceById(id);
        deviceRepository.delete(device);
    }

    @Override
    public List<Device> getDevicesByUserId(Long userId) {
        log.info("根据用户ID获取设备列表: {}", userId);
        return deviceRepository.findByUserId(userId);
    }

    @Override
    public List<Device> getDevicesByUserIdAndType(Long userId, String type) {
        log.info("根据用户ID和设备类型获取设备列表: userId={}, type={}", userId, type);
        return deviceRepository.findByUserIdAndType(userId, type);
    }

    @Override
    public List<Device> getAllDevices() {
        log.info("获取所有设备");
        return deviceRepository.findAll();
    }

    @Override
    public Page<Device> getDevicesByPage(Pageable pageable) {
        log.info("分页获取设备");
        return deviceRepository.findAll(pageable);
    }

    @Override
    public List<Device> searchDevicesByName(String name) {
        log.info("根据设备名称搜索设备: {}", name);
        return deviceRepository.findByNameContaining(name);
    }

    @Override
    public List<Device> searchDevicesByUserIdAndName(Long userId, String name) {
        log.info("根据用户ID和设备名称搜索设备: userId={}, name={}", userId, name);
        return deviceRepository.findByUserIdAndNameContaining(userId, name);
    }

    @Override
    public void toggleDeviceStatus(Long id, boolean enabled) {
        log.info("{}设备: {}", enabled ? "启用" : "禁用", id);
        Device device = getDeviceById(id);
        device.setEnabled(enabled);
        deviceRepository.save(device);
    }

    @Override
    public void updateDeviceOnlineStatus(String deviceId, boolean online) {
        log.info("更新设备在线状态: deviceId={}, online={}", deviceId, online);
        Device device = getDeviceByDeviceId(deviceId);
        device.setOnline(online);
        if (online) {
            device.setLastOnlineTime(LocalDateTime.now());
        }
        deviceRepository.save(device);
    }

    @Override
    public boolean isDeviceIdExists(String deviceId) {
        return deviceRepository.existsByDeviceId(deviceId);
    }

    @Override
    public long countDevicesByUserId(Long userId) {
        return deviceRepository.countByUserId(userId);
    }

    @Override
    public long countOnlineDevices() {
        return deviceRepository.countOnlineDevices();
    }
}