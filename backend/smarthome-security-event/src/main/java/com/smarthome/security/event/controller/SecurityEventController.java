package com.smarthome.security.event.controller;

import com.smarthome.common.model.ApiResponse;
import com.smarthome.security.event.entity.SecurityEvent;
import com.smarthome.security.event.service.SecurityEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 安全事件控制器
 */
@RestController
@RequestMapping("/api/security-events")
@RequiredArgsConstructor
public class SecurityEventController {
    
    private final SecurityEventService securityEventService;
    
    /**
     * 获取所有安全事件
     */
    @GetMapping
    public ApiResponse<List<SecurityEvent>> getAllSecurityEvents() {
        List<SecurityEvent> events = securityEventService.getAllSecurityEvents();
        return ApiResponse.success(events);
    }
    
    /**
     * 根据ID获取安全事件
     */
    @GetMapping("/{id}")
    public ApiResponse<SecurityEvent> getSecurityEventById(@PathVariable Long id) {
        SecurityEvent event = securityEventService.getSecurityEventById(id);
        return ApiResponse.success(event);
    }
    
    /**
     * 根据设备ID获取安全事件列表
     */
    @GetMapping("/device/{deviceId}")
    public ApiResponse<List<SecurityEvent>> getSecurityEventsByDeviceId(@PathVariable Long deviceId) {
        List<SecurityEvent> events = securityEventService.getSecurityEventsByDeviceId(deviceId);
        return ApiResponse.success(events);
    }
    
    /**
     * 根据用户ID获取安全事件列表
     */
    @GetMapping("/user/{userId}")
    public ApiResponse<List<SecurityEvent>> getSecurityEventsByUserId(@PathVariable Long userId) {
        List<SecurityEvent> events = securityEventService.getSecurityEventsByUserId(userId);
        return ApiResponse.success(events);
    }
    
    /**
     * 根据事件类型获取安全事件列表
     */
    @GetMapping("/type/{eventType}")
    public ApiResponse<List<SecurityEvent>> getSecurityEventsByEventType(@PathVariable String eventType) {
        List<SecurityEvent> events = securityEventService.getSecurityEventsByEventType(eventType);
        return ApiResponse.success(events);
    }
    
    /**
     * 根据严重程度获取安全事件列表
     */
    @GetMapping("/severity/{severity}")
    public ApiResponse<List<SecurityEvent>> getSecurityEventsBySeverity(@PathVariable String severity) {
        List<SecurityEvent> events = securityEventService.getSecurityEventsBySeverity(severity);
        return ApiResponse.success(events);
    }
    
    /**
     * 获取未处理的安全事件列表
     */
    @GetMapping("/unhandled")
    public ApiResponse<List<SecurityEvent>> getUnhandledSecurityEvents() {
        List<SecurityEvent> events = securityEventService.getUnhandledSecurityEvents();
        return ApiResponse.success(events);
    }
    
    /**
     * 根据设备ID和事件类型获取安全事件
     */
    @GetMapping("/device/{deviceId}/type/{eventType}")
    public ApiResponse<List<SecurityEvent>> getSecurityEventsByDeviceIdAndEventType(@PathVariable Long deviceId, 
                                                                                  @PathVariable String eventType) {
        List<SecurityEvent> events = securityEventService.getSecurityEventsByDeviceIdAndEventType(deviceId, eventType);
        return ApiResponse.success(events);
    }
    
    /**
     * 根据时间范围获取安全事件
     */
    @GetMapping("/time-range")
    public ApiResponse<List<SecurityEvent>> getSecurityEventsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<SecurityEvent> events = securityEventService.getSecurityEventsByTimeRange(startTime, endTime);
        return ApiResponse.success(events);
    }
    
    /**
     * 根据设备ID和时间范围获取安全事件
     */
    @GetMapping("/device/{deviceId}/time-range")
    public ApiResponse<List<SecurityEvent>> getSecurityEventsByDeviceIdAndTimeRange(
            @PathVariable Long deviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<SecurityEvent> events = securityEventService.getSecurityEventsByDeviceIdAndTimeRange(deviceId, startTime, endTime);
        return ApiResponse.success(events);
    }
    
    /**
     * 创建安全事件
     */
    @PostMapping
    public ApiResponse<SecurityEvent> createSecurityEvent(@RequestBody SecurityEvent securityEvent) {
        SecurityEvent createdEvent = securityEventService.createSecurityEvent(securityEvent);
        ApiResponse<SecurityEvent> response = ApiResponse.success(createdEvent);
        response.setMessage("安全事件创建成功");
        return response;
    }
    
    /**
     * 更新安全事件处理状态
     */
    @PatchMapping("/{id}/handled")
    public ApiResponse<SecurityEvent> updateSecurityEventHandledStatus(@PathVariable Long id,
                                                                      @RequestParam Boolean handled,
                                                                      @RequestParam(required = false) String handledBy,
                                                                      @RequestParam(required = false) String handledDescription) {
        SecurityEvent updatedEvent = securityEventService.updateSecurityEventHandledStatus(id, handled, handledBy, handledDescription);
        ApiResponse<SecurityEvent> response = ApiResponse.success(updatedEvent);
        response.setMessage("安全事件处理状态更新成功");
        return response;
    }
    
    /**
     * 删除安全事件
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteSecurityEvent(@PathVariable Long id) {
        securityEventService.deleteSecurityEvent(id);
        ApiResponse<Void> response = ApiResponse.success(null);
        response.setMessage("安全事件删除成功");
        return response;
    }
    
    /**
     * 统计未处理的安全事件数量
     */
    @GetMapping("/unhandled/count")
    public ApiResponse<Long> countUnhandledSecurityEvents() {
        long count = securityEventService.countUnhandledSecurityEvents();
        return ApiResponse.success(count);
    }
    
    /**
     * 根据严重程度统计未处理的安全事件数量
     */
    @GetMapping("/unhandled/severity/{severity}/count")
    public ApiResponse<Long> countUnhandledSecurityEventsBySeverity(@PathVariable String severity) {
        long count = securityEventService.countUnhandledSecurityEventsBySeverity(severity);
        return ApiResponse.success(count);
    }
    
    /**
     * 根据设备ID统计安全事件数量
     */
    @GetMapping("/device/{deviceId}/count")
    public ApiResponse<Long> countSecurityEventsByDeviceId(@PathVariable Long deviceId) {
        long count = securityEventService.countSecurityEventsByDeviceId(deviceId);
        return ApiResponse.success(count);
    }
    
    /**
     * 根据用户ID统计安全事件数量
     */
    @GetMapping("/user/{userId}/count")
    public ApiResponse<Long> countSecurityEventsByUserId(@PathVariable Long userId) {
        long count = securityEventService.countSecurityEventsByUserId(userId);
        return ApiResponse.success(count);
    }
}