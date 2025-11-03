package com.smarthome.gateway.service.impl;

import com.smarthome.gateway.entity.GatewayDevice;
import com.smarthome.gateway.repository.GatewayDeviceRepository;
import com.smarthome.gateway.service.GatewayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 网关服务实现类
 */
@Slf4j
@Service
public class GatewayServiceImpl implements GatewayService {
    
    @Autowired
    private GatewayDeviceRepository gatewayDeviceRepository;
    
    @Override
    public List<GatewayDevice> getAllGateways() {
        return gatewayDeviceRepository.findAll();
    }
    
    @Override
    public GatewayDevice getGatewayById(Long id) {
        Optional<GatewayDevice> gateway = gatewayDeviceRepository.findById(id);
        return gateway.orElse(null);
    }
    
    @Override
    public GatewayDevice getGatewayByGatewayId(String gatewayId) {
        Optional<GatewayDevice> gateway = gatewayDeviceRepository.findByGatewayId(gatewayId);
        return gateway.orElse(null);
    }
    
    @Override
    public GatewayDevice createGateway(GatewayDevice gateway) {
        try {
            // 检查网关ID是否已存在
            if (isGatewayIdExists(gateway.getGatewayId())) {
                log.warn("网关ID已存在: {}", gateway.getGatewayId());
                return null;
            }
            
            // 检查IP地址是否已存在
            if (isIpAddressExists(gateway.getIpAddress())) {
                log.warn("IP地址已存在: {}", gateway.getIpAddress());
                return null;
            }
            
            // 设置默认值
            if (gateway.getOnline() == null) {
                gateway.setOnline(false);
            }
            if (gateway.getEnabled() == null) {
                gateway.setEnabled(true);
            }
            
            GatewayDevice savedGateway = gatewayDeviceRepository.save(gateway);
            log.info("创建网关设备成功 - ID: {}, 网关ID: {}", savedGateway.getId(), savedGateway.getGatewayId());
            return savedGateway;
        } catch (Exception e) {
            log.error("创建网关设备失败 - 网关ID: {}, 错误: {}", gateway.getGatewayId(), e.getMessage());
            return null;
        }
    }
    
    @Override
    public GatewayDevice updateGateway(Long id, GatewayDevice gateway) {
        try {
            Optional<GatewayDevice> existingGateway = gatewayDeviceRepository.findById(id);
            if (existingGateway.isPresent()) {
                GatewayDevice gatewayToUpdate = existingGateway.get();
                
                // 检查网关ID是否已存在（排除当前网关）
                if (!gatewayToUpdate.getGatewayId().equals(gateway.getGatewayId()) && 
                    isGatewayIdExists(gateway.getGatewayId())) {
                    log.warn("网关ID已存在: {}", gateway.getGatewayId());
                    return null;
                }
                
                // 检查IP地址是否已存在（排除当前网关）
                if (!gatewayToUpdate.getIpAddress().equals(gateway.getIpAddress()) && 
                    isIpAddressExists(gateway.getIpAddress())) {
                    log.warn("IP地址已存在: {}", gateway.getIpAddress());
                    return null;
                }
                
                // 更新字段
                gatewayToUpdate.setName(gateway.getName());
                gatewayToUpdate.setDescription(gateway.getDescription());
                gatewayToUpdate.setType(gateway.getType());
                gatewayToUpdate.setIpAddress(gateway.getIpAddress());
                gatewayToUpdate.setPort(gateway.getPort());
                gatewayToUpdate.setProtocol(gateway.getProtocol());
                gatewayToUpdate.setLocation(gateway.getLocation());
                gatewayToUpdate.setEnabled(gateway.getEnabled());
                
                GatewayDevice updatedGateway = gatewayDeviceRepository.save(gatewayToUpdate);
                log.info("更新网关设备成功 - ID: {}, 网关ID: {}", id, updatedGateway.getGatewayId());
                return updatedGateway;
            } else {
                log.warn("网关设备不存在 - ID: {}", id);
                return null;
            }
        } catch (Exception e) {
            log.error("更新网关设备失败 - ID: {}, 错误: {}", id, e.getMessage());
            return null;
        }
    }
    
    @Override
    public boolean deleteGateway(Long id) {
        try {
            if (gatewayDeviceRepository.existsById(id)) {
                gatewayDeviceRepository.deleteById(id);
                log.info("删除网关设备成功 - ID: {}", id);
                return true;
            } else {
                log.warn("网关设备不存在 - ID: {}", id);
                return false;
            }
        } catch (Exception e) {
            log.error("删除网关设备失败 - ID: {}, 错误: {}", id, e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean updateOnlineStatus(String gatewayId, boolean online) {
        try {
            Optional<GatewayDevice> gateway = gatewayDeviceRepository.findByGatewayId(gatewayId);
            if (gateway.isPresent()) {
                GatewayDevice gatewayToUpdate = gateway.get();
                gatewayToUpdate.setOnline(online);
                if (online) {
                    gatewayToUpdate.setLastHeartbeat(LocalDateTime.now());
                }
                gatewayDeviceRepository.save(gatewayToUpdate);
                log.info("更新网关在线状态成功 - 网关ID: {}, 状态: {}", gatewayId, online);
                return true;
            } else {
                log.warn("网关设备不存在 - 网关ID: {}", gatewayId);
                return false;
            }
        } catch (Exception e) {
            log.error("更新网关在线状态失败 - 网关ID: {}, 错误: {}", gatewayId, e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean updateHeartbeat(String gatewayId) {
        try {
            Optional<GatewayDevice> gateway = gatewayDeviceRepository.findByGatewayId(gatewayId);
            if (gateway.isPresent()) {
                GatewayDevice gatewayToUpdate = gateway.get();
                gatewayToUpdate.setLastHeartbeat(LocalDateTime.now());
                gatewayToUpdate.setOnline(true);
                gatewayDeviceRepository.save(gatewayToUpdate);
                log.debug("更新网关心跳时间成功 - 网关ID: {}", gatewayId);
                return true;
            } else {
                log.warn("网关设备不存在 - 网关ID: {}", gatewayId);
                return false;
            }
        } catch (Exception e) {
            log.error("更新网关心跳时间失败 - 网关ID: {}, 错误: {}", gatewayId, e.getMessage());
            return false;
        }
    }
    
    @Override
    public List<GatewayDevice> searchGatewaysByName(String name) {
        return gatewayDeviceRepository.findByNameContaining(name);
    }
    
    @Override
    public List<GatewayDevice> getGatewaysByType(String type) {
        return gatewayDeviceRepository.findByType(type);
    }
    
    @Override
    public List<GatewayDevice> getGatewaysByOnlineStatus(Boolean online) {
        return gatewayDeviceRepository.findByOnline(online);
    }
    
    @Override
    public List<GatewayDevice> getGatewaysByEnabledStatus(Boolean enabled) {
        return gatewayDeviceRepository.findByEnabled(enabled);
    }
    
    @Override
    public long getOnlineGatewayCount() {
        return gatewayDeviceRepository.countOnlineDevices();
    }
    
    @Override
    public long getOfflineGatewayCount() {
        return gatewayDeviceRepository.countOfflineDevices();
    }
    
    @Override
    public boolean isGatewayIdExists(String gatewayId) {
        return gatewayDeviceRepository.findByGatewayId(gatewayId).isPresent();
    }
    
    @Override
    public boolean isIpAddressExists(String ipAddress) {
        return gatewayDeviceRepository.findByIpAddress(ipAddress).isPresent();
    }
    
    @Override
    public boolean enableGateway(Long id) {
        try {
            Optional<GatewayDevice> gateway = gatewayDeviceRepository.findById(id);
            if (gateway.isPresent()) {
                GatewayDevice gatewayToUpdate = gateway.get();
                gatewayToUpdate.setEnabled(true);
                gatewayDeviceRepository.save(gatewayToUpdate);
                log.info("启用网关设备成功 - ID: {}", id);
                return true;
            } else {
                log.warn("网关设备不存在 - ID: {}", id);
                return false;
            }
        } catch (Exception e) {
            log.error("启用网关设备失败 - ID: {}, 错误: {}", id, e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean disableGateway(Long id) {
        try {
            Optional<GatewayDevice> gateway = gatewayDeviceRepository.findById(id);
            if (gateway.isPresent()) {
                GatewayDevice gatewayToUpdate = gateway.get();
                gatewayToUpdate.setEnabled(false);
                gatewayToUpdate.setOnline(false); // 禁用时同时设为离线
                gatewayDeviceRepository.save(gatewayToUpdate);
                log.info("禁用网关设备成功 - ID: {}", id);
                return true;
            } else {
                log.warn("网关设备不存在 - ID: {}", id);
                return false;
            }
        } catch (Exception e) {
            log.error("禁用网关设备失败 - ID: {}, 错误: {}", id, e.getMessage());
            return false;
        }
    }
}