package com.smarthome.device.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 设备实体类
 */
@Data
@Entity
@Table(name = "devices")
public class Device {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false, length = 50)
    private String type;
    
    @Column(unique = true, nullable = false, length = 100)
    private String deviceId;
    
    @Column(length = 200)
    private String description;
    
    @Column(nullable = false)
    private Boolean online = false;
    
    @Column(nullable = false)
    private Boolean enabled = true;
    
    @Column(length = 50)
    private String status;
    
    @Column(length = 50)
    private String firmwareVersion;
    
    @Column(length = 50)
    private String hardwareVersion;
    
    @Column(length = 100)
    private String ipAddress;
    
    @Column
    private Integer port;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private LocalDateTime createTime;
    
    @Column(nullable = false)
    private LocalDateTime updateTime;
    
    @Column
    private LocalDateTime lastOnlineTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}