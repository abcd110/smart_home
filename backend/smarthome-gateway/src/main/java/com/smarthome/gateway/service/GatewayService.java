package com.smarthome.gateway.service;

import com.smarthome.gateway.entity.GatewayDevice;

import java.util.List;

/**
 * 网关服务接口
 */
public interface GatewayService {
    
    /**
     * 获取所有网关设备
     */
    List<GatewayDevice> getAllGateways();
    
    /**
     * 根据ID获取网关设备
     */
    GatewayDevice getGatewayById(Long id);
    
    /**
     * 根据网关ID获取网关设备
     */
    GatewayDevice getGatewayByGatewayId(String gatewayId);
    
    /**
     * 创建网关设备
     */
    GatewayDevice createGateway(GatewayDevice gateway);
    
    /**
     * 更新网关设备
     */
    GatewayDevice updateGateway(Long id, GatewayDevice gateway);
    
    /**
     * 删除网关设备
     */
    boolean deleteGateway(Long id);
    
    /**
     * 更新网关在线状态
     */
    boolean updateOnlineStatus(String gatewayId, boolean online);
    
    /**
     * 更新网关心跳时间
     */
    boolean updateHeartbeat(String gatewayId);
    
    /**
     * 根据名称搜索网关设备
     */
    List<GatewayDevice> searchGatewaysByName(String name);
    
    /**
     * 根据类型获取网关设备
     */
    List<GatewayDevice> getGatewaysByType(String type);
    
    /**
     * 根据在线状态获取网关设备
     */
    List<GatewayDevice> getGatewaysByOnlineStatus(Boolean online);
    
    /**
     * 根据启用状态获取网关设备
     */
    List<GatewayDevice> getGatewaysByEnabledStatus(Boolean enabled);
    
    /**
     * 获取在线网关设备数量
     */
    long getOnlineGatewayCount();
    
    /**
     * 获取离线网关设备数量
     */
    long getOfflineGatewayCount();
    
    /**
     * 检查网关ID是否已存在
     */
    boolean isGatewayIdExists(String gatewayId);
    
    /**
     * 检查IP地址是否已存在
     */
    boolean isIpAddressExists(String ipAddress);
    
    /**
     * 启用网关设备
     */
    boolean enableGateway(Long id);
    
    /**
     * 禁用网关设备
     */
    boolean disableGateway(Long id);
}