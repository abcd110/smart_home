package com.smarthome.mqtt.service;

import com.smarthome.mqtt.entity.MqttMessage;

import java.util.List;

/**
 * MQTT服务接口
 */
public interface MqttService {
    
    /**
     * 发布消息到指定主题
     */
    boolean publish(String topic, String payload);
    
    /**
     * 发布消息到指定主题（带QoS和保留标志）
     */
    boolean publish(String topic, String payload, int qos, boolean retained);
    
    /**
     * 订阅指定主题
     */
    boolean subscribe(String topic);
    
    /**
     * 订阅指定主题（带QoS）
     */
    boolean subscribe(String topic, int qos);
    
    /**
     * 取消订阅指定主题
     */
    boolean unsubscribe(String topic);
    
    /**
     * 保存MQTT消息到数据库
     */
    MqttMessage saveMessage(MqttMessage message);
    
    /**
     * 根据ID获取消息
     */
    MqttMessage getMessageById(Long id);
    
    /**
     * 根据主题获取消息列表
     */
    List<MqttMessage> getMessagesByTopic(String topic);
    
    /**
     * 根据设备ID获取消息列表
     */
    List<MqttMessage> getMessagesByDeviceId(String deviceId);
    
    /**
     * 根据时间范围获取消息列表
     */
    List<MqttMessage> getMessagesByTimeRange(String startTime, String endTime);
    
    /**
     * 获取所有消息列表
     */
    List<MqttMessage> getAllMessages();
    
    /**
     * 删除指定ID的消息
     */
    boolean deleteMessage(Long id);
    
    /**
     * 检查MQTT连接状态
     */
    boolean isConnected();
    
    /**
     * 断开MQTT连接
     */
    void disconnect();
}