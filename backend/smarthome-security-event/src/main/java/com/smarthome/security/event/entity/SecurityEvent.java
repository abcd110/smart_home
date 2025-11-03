package com.smarthome.security.event.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 安全事件实体类
 */
@Data
@Entity
@Table(name = "security_events")
public class SecurityEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String eventType;
    
    @Column(nullable = false, length = 200)
    private String eventDescription;
    
    @Column(nullable = false)
    private String severity;
    
    @Column(nullable = false)
    private Long deviceId;
    
    @Column(length = 100)
    private String sensorId;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(length = 50)
    private String location;
    
    @Column(length = 500)
    private String eventData;
    
    @Column(nullable = false)
    private Boolean handled = false;
    
    @Column(length = 200)
    private String handledBy;
    
    @Column
    private LocalDateTime handledTime;
    
    @Column(length = 500)
    private String handledDescription;
    
    @Column(nullable = false)
    private LocalDateTime eventTime;
    
    @Column(nullable = false)
    private LocalDateTime createTime;
    
    @PrePersist
    protected void onCreate() {
        if (eventTime == null) {
            eventTime = LocalDateTime.now();
        }
        createTime = LocalDateTime.now();
    }
}