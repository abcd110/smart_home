package com.smarthome.mqtt.service.impl;

import com.smarthome.mqtt.entity.MqttMessage;
import com.smarthome.mqtt.repository.MqttMessageRepository;
import com.smarthome.mqtt.service.MqttService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * MQTT服务实现类
 */
@Slf4j
@Service
public class MqttServiceImpl implements MqttService, MqttCallback {
    
    @Autowired
    private MqttPahoClientFactory clientFactory;
    
    @Autowired
    private MqttMessageRepository messageRepository;
    
    private MqttClient mqttClient;
    private boolean connected = false;
    
    @Override
    public boolean publish(String topic, String payload) {
        return publish(topic, payload, 0, false);
    }
    
    @Override
    public boolean publish(String topic, String payload, int qos, boolean retained) {
        try {
            if (!isConnected()) {
                connect();
            }
            
            org.eclipse.paho.client.mqttv3.MqttMessage message = new org.eclipse.paho.client.mqttv3.MqttMessage(payload.getBytes());
            message.setQos(qos);
            message.setRetained(retained);
            
            mqttClient.publish(topic, message);
            
            // 保存消息到数据库
            saveMessageToDatabase(topic, payload, qos, retained, null, null, "PUBLISH");
            
            log.info("消息发布成功 - 主题: {}, 载荷: {}, QoS: {}, 保留: {}", topic, payload, qos, retained);
            return true;
        } catch (MqttException e) {
            log.error("消息发布失败 - 主题: {}, 错误: {}", topic, e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean subscribe(String topic) {
        return subscribe(topic, 0);
    }
    
    @Override
    public boolean subscribe(String topic, int qos) {
        try {
            if (!isConnected()) {
                connect();
            }
            
            mqttClient.subscribe(topic, qos);
            log.info("订阅主题成功 - 主题: {}, QoS: {}", topic, qos);
            return true;
        } catch (MqttException e) {
            log.error("订阅主题失败 - 主题: {}, 错误: {}", topic, e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean unsubscribe(String topic) {
        try {
            if (!isConnected()) {
                connect();
            }
            
            mqttClient.unsubscribe(topic);
            log.info("取消订阅成功 - 主题: {}", topic);
            return true;
        } catch (MqttException e) {
            log.error("取消订阅失败 - 主题: {}, 错误: {}", topic, e.getMessage());
            return false;
        }
    }
    
    @Override
    public MqttMessage saveMessage(MqttMessage message) {
        return messageRepository.save(message);
    }
    
    @Override
    public MqttMessage getMessageById(Long id) {
        Optional<MqttMessage> message = messageRepository.findById(id);
        return message.orElse(null);
    }
    
    @Override
    public List<MqttMessage> getMessagesByTopic(String topic) {
        return messageRepository.findByTopic(topic);
    }
    
    @Override
    public List<MqttMessage> getMessagesByDeviceId(String deviceId) {
        return messageRepository.findByDeviceId(deviceId);
    }
    
    @Override
    public List<MqttMessage> getMessagesByTimeRange(String startTime, String endTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime start = LocalDateTime.parse(startTime, formatter);
        LocalDateTime end = LocalDateTime.parse(endTime, formatter);
        return messageRepository.findByCreateTimeBetween(start, end);
    }
    
    @Override
    public List<MqttMessage> getAllMessages() {
        return messageRepository.findAll();
    }
    
    @Override
    public boolean deleteMessage(Long id) {
        try {
            messageRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            log.error("删除消息失败 - ID: {}, 错误: {}", id, e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean isConnected() {
        return connected && mqttClient != null && mqttClient.isConnected();
    }
    
    @Override
    public void disconnect() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                connected = false;
                log.info("MQTT连接已断开");
            }
        } catch (MqttException e) {
            log.error("断开MQTT连接失败: {}", e.getMessage());
        }
    }
    
    private void connect() {
        try {
            if (mqttClient == null) {
                mqttClient = new MqttClient(clientFactory.getConnectionOptions().getServerURIs()[0], 
                                          clientFactory.getConnectionOptions().getUserName() + "-" + System.currentTimeMillis());
                mqttClient.setCallback(this);
            }
            
            if (!mqttClient.isConnected()) {
                mqttClient.connect(clientFactory.getConnectionOptions());
                connected = true;
                log.info("MQTT连接成功 - Broker: {}", clientFactory.getConnectionOptions().getServerURIs()[0]);
            }
        } catch (MqttException e) {
            log.error("MQTT连接失败: {}", e.getMessage());
            connected = false;
        }
    }
    
    private void saveMessageToDatabase(String topic, String payload, int qos, boolean retained, 
                                      String clientId, String deviceId, String messageType) {
        try {
            MqttMessage message = new MqttMessage();
            message.setTopic(topic);
            message.setPayload(payload);
            message.setQos(qos);
            message.setRetained(retained);
            message.setClientId(clientId);
            message.setDeviceId(deviceId);
            message.setMessageType(messageType);
            
            messageRepository.save(message);
        } catch (Exception e) {
            log.error("保存MQTT消息到数据库失败: {}", e.getMessage());
        }
    }
    
    // MQTT回调方法
    @Override
    public void connectionLost(Throwable cause) {
        log.warn("MQTT连接丢失: {}", cause.getMessage());
        connected = false;
        // 尝试重新连接
        try {
            Thread.sleep(5000);
            connect();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public void messageArrived(String topic, org.eclipse.paho.client.mqttv3.MqttMessage message) throws Exception {
        String payload = new String(message.getPayload());
        log.info("收到MQTT消息 - 主题: {}, 载荷: {}, QoS: {}", topic, payload, message.getQos());
        
        // 保存接收到的消息到数据库
        saveMessageToDatabase(topic, payload, message.getQos(), message.isRetained(), 
                            mqttClient.getClientId(), extractDeviceId(topic), "RECEIVE");
    }
    
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        log.debug("消息投递完成 - 消息ID: {}", token.getMessageId());
    }
    
    private String extractDeviceId(String topic) {
        // 从主题中提取设备ID，例如：smarthome/device/12345/status -> 12345
        String[] parts = topic.split("/");
        if (parts.length >= 3 && "device".equals(parts[1])) {
            return parts[2];
        }
        return null;
    }
}