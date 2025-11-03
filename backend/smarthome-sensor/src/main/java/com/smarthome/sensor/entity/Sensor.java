package com.smarthome.sensor.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 传感器实体类
 */
@Data
@Entity
@Table(name = "sensors")
public class Sensor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false, length = 50)
    private String type;
    
    @Column(unique = true, nullable = false, length = 100)
    private String sensorId;
    
    @Column(nullable = false)
    private Long deviceId;
    
    @Column(length = 200)
    private String description;
    
    @Column(nullable = false)
    private Boolean enabled = true;
    
    @Column(length = 50)
    private String unit;
    
    @Column(precision = 10, scale = 4)
    private BigDecimal minValue;
    
    @Column(precision = 10, scale = 4)
    private BigDecimal maxValue;
    
    @Column(precision = 10, scale = 4)
    private BigDecimal currentValue;
    
    @Column
    private LocalDateTime lastReadingTime;
    
    @Column(nullable = false)
    private LocalDateTime createTime;
    
    @Column(nullable = false)
    private LocalDateTime updateTime;
    
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