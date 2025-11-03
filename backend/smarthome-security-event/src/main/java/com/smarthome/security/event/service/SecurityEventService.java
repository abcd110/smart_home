package com.smarthome.security.event.service;

import com.smarthome.security.event.entity.SecurityEvent;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 安全事件服务接口
 */
public interface SecurityEventService {
    
    /**
     * 获取所有安全事件
     */
    List<SecurityEvent> getAllSecurityEvents();
    
    /**
     * 根据ID获取安全事件
     */
    SecurityEvent getSecurityEventById(Long id);
    
    /**
     * 根据设备ID获取安全事件列表
     */
    List<SecurityEvent> getSecurityEventsByDeviceId(Long deviceId);
    
    /**
     * 根据用户ID获取安全事件列表
     */
    List<SecurityEvent> getSecurityEventsByUserId(Long userId);
    
    /**
     * 根据事件类型获取安全事件列表
     */
    List<SecurityEvent> getSecurityEventsByEventType(String eventType);
    
    /**
     * 根据严重程度获取安全事件列表
     */
    List<SecurityEvent> getSecurityEventsBySeverity(String severity);
    
    /**
     * 获取未处理的安全事件列表
     */
    List<SecurityEvent> getUnhandledSecurityEvents();
    
    /**
     * 根据设备ID和事件类型获取安全事件
     */
    List<SecurityEvent> getSecurityEventsByDeviceIdAndEventType(Long deviceId, String eventType);
    
    /**
     * 根据时间范围获取安全事件
     */
    List<SecurityEvent> getSecurityEventsByTimeRange(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据设备ID和时间范围获取安全事件
     */
    List<SecurityEvent> getSecurityEventsByDeviceIdAndTimeRange(Long deviceId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 创建安全事件
     */
    SecurityEvent createSecurityEvent(SecurityEvent securityEvent);
    
    /**
     * 更新安全事件处理状态
     */
    SecurityEvent updateSecurityEventHandledStatus(Long id, Boolean handled, String handledBy, String handledDescription);
    
    /**
     * 删除安全事件
     */
    void deleteSecurityEvent(Long id);
    
    /**
     * 统计未处理的安全事件数量
     */
    long countUnhandledSecurityEvents();
    
    /**
     * 根据严重程度统计未处理的安全事件数量
     */
    long countUnhandledSecurityEventsBySeverity(String severity);
    
    /**
     * 根据设备ID统计安全事件数量
     */
    long countSecurityEventsByDeviceId(Long deviceId);
    
    /**
     * 根据用户ID统计安全事件数量
     */
    long countSecurityEventsByUserId(Long userId);
}