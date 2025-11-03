package com.smarthome.security.event.repository;

import com.smarthome.security.event.entity.SecurityEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 安全事件数据访问接口
 */
@Repository
public interface SecurityEventRepository extends JpaRepository<SecurityEvent, Long> {
    
    /**
     * 根据设备ID查找安全事件列表
     */
    List<SecurityEvent> findByDeviceId(Long deviceId);
    
    /**
     * 根据用户ID查找安全事件列表
     */
    List<SecurityEvent> findByUserId(Long userId);
    
    /**
     * 根据事件类型查找安全事件列表
     */
    List<SecurityEvent> findByEventType(String eventType);
    
    /**
     * 根据严重程度查找安全事件列表
     */
    List<SecurityEvent> findBySeverity(String severity);
    
    /**
     * 查找未处理的安全事件
     */
    List<SecurityEvent> findByHandledFalse();
    
    /**
     * 根据设备ID和事件类型查找安全事件
     */
    List<SecurityEvent> findByDeviceIdAndEventType(Long deviceId, String eventType);
    
    /**
     * 根据时间范围查找安全事件
     */
    @Query("SELECT se FROM SecurityEvent se WHERE se.eventTime BETWEEN :startTime AND :endTime")
    List<SecurityEvent> findByEventTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                               @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据设备ID和时间范围查找安全事件
     */
    @Query("SELECT se FROM SecurityEvent se WHERE se.deviceId = :deviceId AND se.eventTime BETWEEN :startTime AND :endTime")
    List<SecurityEvent> findByDeviceIdAndEventTimeBetween(@Param("deviceId") Long deviceId, 
                                                         @Param("startTime") LocalDateTime startTime, 
                                                         @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计未处理的安全事件数量
     */
    @Query("SELECT COUNT(se) FROM SecurityEvent se WHERE se.handled = false")
    long countByHandledFalse();
    
    /**
     * 根据严重程度统计未处理的安全事件数量
     */
    @Query("SELECT COUNT(se) FROM SecurityEvent se WHERE se.handled = false AND se.severity = :severity")
    long countByHandledFalseAndSeverity(@Param("severity") String severity);
    
    /**
     * 根据设备ID统计安全事件数量
     */
    @Query("SELECT COUNT(se) FROM SecurityEvent se WHERE se.deviceId = :deviceId")
    long countByDeviceId(@Param("deviceId") Long deviceId);
    
    /**
     * 根据用户ID统计安全事件数量
     */
    @Query("SELECT COUNT(se) FROM SecurityEvent se WHERE se.userId = :userId")
    long countByUserId(@Param("userId") Long userId);
}