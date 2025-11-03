package com.smarthome.mqtt.repository;

import com.smarthome.mqtt.entity.MqttMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MQTT消息数据访问接口
 */
@Repository
public interface MqttMessageRepository extends JpaRepository<MqttMessage, Long> {
    
    /**
     * 根据主题查找消息列表
     */
    List<MqttMessage> findByTopic(String topic);
    
    /**
     * 根据设备ID查找消息列表
     */
    List<MqttMessage> findByDeviceId(String deviceId);
    
    /**
     * 根据客户端ID查找消息列表
     */
    List<MqttMessage> findByClientId(String clientId);
    
    /**
     * 根据消息类型查找消息列表
     */
    List<MqttMessage> findByMessageType(String messageType);
    
    /**
     * 根据时间范围查找消息列表
     */
    @Query("SELECT m FROM MqttMessage m WHERE m.createTime BETWEEN :startTime AND :endTime")
    List<MqttMessage> findByCreateTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                             @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据主题和时间范围查找消息列表
     */
    @Query("SELECT m FROM MqttMessage m WHERE m.topic = :topic AND m.createTime BETWEEN :startTime AND :endTime")
    List<MqttMessage> findByTopicAndCreateTimeBetween(@Param("topic") String topic, 
                                                     @Param("startTime") LocalDateTime startTime, 
                                                     @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据设备ID和时间范围查找消息列表
     */
    @Query("SELECT m FROM MqttMessage m WHERE m.deviceId = :deviceId AND m.createTime BETWEEN :startTime AND :endTime")
    List<MqttMessage> findByDeviceIdAndCreateTimeBetween(@Param("deviceId") String deviceId, 
                                                         @Param("startTime") LocalDateTime startTime, 
                                                         @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计特定主题的消息数量
     */
    @Query("SELECT COUNT(m) FROM MqttMessage m WHERE m.topic = :topic")
    long countByTopic(@Param("topic") String topic);
    
    /**
     * 统计特定设备的消息数量
     */
    @Query("SELECT COUNT(m) FROM MqttMessage m WHERE m.deviceId = :deviceId")
    long countByDeviceId(@Param("deviceId") String deviceId);
}