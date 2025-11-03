package com.smarthome.mqtt.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * MQTT消息实体类
 */
@Data
@Entity
@Table(name = "mqtt_messages")
public class MqttMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String topic;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;
    
    @Column(nullable = false)
    private Integer qos = 0;
    
    @Column(nullable = false)
    private Boolean retained = false;
    
    @Column(length = 100)
    private String clientId;
    
    @Column(length = 100)
    private String deviceId;
    
    @Column(length = 50)
    private String messageType;
    
    @Column(nullable = false)
    private LocalDateTime createTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}