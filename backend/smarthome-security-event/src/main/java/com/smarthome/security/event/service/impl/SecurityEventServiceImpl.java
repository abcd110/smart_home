package com.smarthome.security.event.service.impl;

import com.smarthome.security.event.entity.SecurityEvent;
import com.smarthome.security.event.repository.SecurityEventRepository;
import com.smarthome.security.event.service.SecurityEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 安全事件服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityEventServiceImpl implements SecurityEventService {
    
    private final SecurityEventRepository securityEventRepository;
    
    @Override
    public List<SecurityEvent> getAllSecurityEvents() {
        return securityEventRepository.findAll();
    }
    
    @Override
    public SecurityEvent getSecurityEventById(Long id) {
        return securityEventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("安全事件不存在，ID: " + id));
    }
    
    @Override
    public List<SecurityEvent> getSecurityEventsByDeviceId(Long deviceId) {
        return securityEventRepository.findByDeviceId(deviceId);
    }
    
    @Override
    public List<SecurityEvent> getSecurityEventsByUserId(Long userId) {
        return securityEventRepository.findByUserId(userId);
    }
    
    @Override
    public List<SecurityEvent> getSecurityEventsByEventType(String eventType) {
        return securityEventRepository.findByEventType(eventType);
    }
    
    @Override
    public List<SecurityEvent> getSecurityEventsBySeverity(String severity) {
        return securityEventRepository.findBySeverity(severity);
    }
    
    @Override
    public List<SecurityEvent> getUnhandledSecurityEvents() {
        return securityEventRepository.findByHandledFalse();
    }
    
    @Override
    public List<SecurityEvent> getSecurityEventsByDeviceIdAndEventType(Long deviceId, String eventType) {
        return securityEventRepository.findByDeviceIdAndEventType(deviceId, eventType);
    }
    
    @Override
    public List<SecurityEvent> getSecurityEventsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return securityEventRepository.findByEventTimeBetween(startTime, endTime);
    }
    
    @Override
    public List<SecurityEvent> getSecurityEventsByDeviceIdAndTimeRange(Long deviceId, LocalDateTime startTime, LocalDateTime endTime) {
        return securityEventRepository.findByDeviceIdAndEventTimeBetween(deviceId, startTime, endTime);
    }
    
    @Override
    public SecurityEvent createSecurityEvent(SecurityEvent securityEvent) {
        // 设置默认值
        if (securityEvent.getHandled() == null) {
            securityEvent.setHandled(false);
        }
        if (securityEvent.getEventTime() == null) {
            securityEvent.setEventTime(LocalDateTime.now());
        }
        
        SecurityEvent savedEvent = securityEventRepository.save(securityEvent);
        log.info("创建安全事件成功，ID: {}, 事件类型: {}", savedEvent.getId(), savedEvent.getEventType());
        return savedEvent;
    }
    
    @Override
    public SecurityEvent updateSecurityEventHandledStatus(Long id, Boolean handled, String handledBy, String handledDescription) {
        SecurityEvent securityEvent = getSecurityEventById(id);
        
        securityEvent.setHandled(handled);
        if (handled) {
            securityEvent.setHandledBy(handledBy);
            securityEvent.setHandledTime(LocalDateTime.now());
            securityEvent.setHandledDescription(handledDescription);
        } else {
            securityEvent.setHandledBy(null);
            securityEvent.setHandledTime(null);
            securityEvent.setHandledDescription(null);
        }
        
        SecurityEvent updatedEvent = securityEventRepository.save(securityEvent);
        log.info("更新安全事件处理状态成功，ID: {}, 状态: {}", id, handled);
        return updatedEvent;
    }
    
    @Override
    public void deleteSecurityEvent(Long id) {
        SecurityEvent securityEvent = getSecurityEventById(id);
        securityEventRepository.delete(securityEvent);
        log.info("删除安全事件成功，ID: {}", id);
    }
    
    @Override
    public long countUnhandledSecurityEvents() {
        return securityEventRepository.countByHandledFalse();
    }
    
    @Override
    public long countUnhandledSecurityEventsBySeverity(String severity) {
        return securityEventRepository.countByHandledFalseAndSeverity(severity);
    }
    
    @Override
    public long countSecurityEventsByDeviceId(Long deviceId) {
        return securityEventRepository.countByDeviceId(deviceId);
    }
    
    @Override
    public long countSecurityEventsByUserId(Long userId) {
        return securityEventRepository.countByUserId(userId);
    }
}