package com.smarthome.gateway.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 网关设备实体类
 */
@Data
@Entity
@Table(name = "gateway_devices")
public class GatewayDevice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String gatewayId;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(length = 200)
    private String description;
    
    @Column(nullable = false, length = 50)
    private String type;
    
    @Column(nullable = false, length = 15)
    private String ipAddress;
    
    @Column(nullable = false)
    private Integer port;
    
    @Column(nullable = false, length = 50)
    private String protocol;
    
    @Column(nullable = false)
    private Boolean online = false;
    
    @Column(nullable = false)
    private Boolean enabled = true;
    
    @Column(length = 100)
    private String location;
    
    @Column(nullable = false)
    private LocalDateTime lastHeartbeat;
    
    @Column(nullable = false)
    private LocalDateTime createTime;
    
    @Column(nullable = false)
    private LocalDateTime updateTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        lastHeartbeat = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}