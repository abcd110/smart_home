package com.smarthome.gateway.repository;

import com.smarthome.gateway.entity.GatewayDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 网关设备数据访问接口
 */
@Repository
public interface GatewayDeviceRepository extends JpaRepository<GatewayDevice, Long> {
    
    /**
     * 根据网关ID查找网关设备
     */
    Optional<GatewayDevice> findByGatewayId(String gatewayId);
    
    /**
     * 根据名称查找网关设备
     */
    List<GatewayDevice> findByNameContaining(String name);
    
    /**
     * 根据类型查找网关设备
     */
    List<GatewayDevice> findByType(String type);
    
    /**
     * 根据在线状态查找网关设备
     */
    List<GatewayDevice> findByOnline(Boolean online);
    
    /**
     * 根据启用状态查找网关设备
     */
    List<GatewayDevice> findByEnabled(Boolean enabled);
    
    /**
     * 根据IP地址查找网关设备
     */
    Optional<GatewayDevice> findByIpAddress(String ipAddress);
    
    /**
     * 查找最后心跳时间在指定时间之前的网关设备（离线设备）
     */
    @Query("SELECT g FROM GatewayDevice g WHERE g.lastHeartbeat < :thresholdTime")
    List<GatewayDevice> findOfflineDevices(@Param("thresholdTime") LocalDateTime thresholdTime);
    
    /**
     * 统计在线网关设备数量
     */
    @Query("SELECT COUNT(g) FROM GatewayDevice g WHERE g.online = true")
    long countOnlineDevices();
    
    /**
     * 统计离线网关设备数量
     */
    @Query("SELECT COUNT(g) FROM GatewayDevice g WHERE g.online = false")
    long countOfflineDevices();
    
    /**
     * 根据位置查找网关设备
     */
    List<GatewayDevice> findByLocation(String location);
    
    /**
     * 查找所有启用的网关设备
     */
    List<GatewayDevice> findByEnabledTrue();
    
    /**
     * 查找所有在线的网关设备
     */
    List<GatewayDevice> findByOnlineTrue();
}