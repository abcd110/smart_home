package com.smarthome.mqtt.controller;

import com.smarthome.common.model.ApiResponse;
import com.smarthome.mqtt.entity.MqttMessage;
import com.smarthome.mqtt.service.MqttService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * MQTT控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/mqtt")
public class MqttController {
    
    @Autowired
    private MqttService mqttService;
    
    /**
     * 发布消息
     */
    @PostMapping("/publish")
    public ApiResponse<Boolean> publish(@RequestBody PublishRequest request) {
        try {
            boolean success = mqttService.publish(request.getTopic(), request.getPayload(), 
                                                 request.getQos(), request.isRetained());
            if (success) {
                ApiResponse<Boolean> response = ApiResponse.success(true);
                response.setMessage("消息发布成功");
                return response;
            } else {
                return ApiResponse.error("消息发布失败");
            }
        } catch (Exception e) {
            log.error("发布消息异常: {}", e.getMessage());
            return ApiResponse.error("发布消息异常: " + e.getMessage());
        }
    }
    
    /**
     * 订阅主题
     */
    @PostMapping("/subscribe")
    public ApiResponse<Boolean> subscribe(@RequestBody SubscribeRequest request) {
        try {
            boolean success = mqttService.subscribe(request.getTopic(), request.getQos());
            if (success) {
                ApiResponse<Boolean> response = ApiResponse.success(true);
                response.setMessage("订阅主题成功");
                return response;
            } else {
                return ApiResponse.error("订阅主题失败");
            }
        } catch (Exception e) {
            log.error("订阅主题异常: {}", e.getMessage());
            return ApiResponse.error("订阅主题异常: " + e.getMessage());
        }
    }
    
    /**
     * 取消订阅
     */
    @PostMapping("/unsubscribe")
    public ApiResponse<Boolean> unsubscribe(@RequestBody UnsubscribeRequest request) {
        try {
            boolean success = mqttService.unsubscribe(request.getTopic());
            if (success) {
                ApiResponse<Boolean> response = ApiResponse.success(true);
                response.setMessage("取消订阅成功");
                return response;
            } else {
                return ApiResponse.error("取消订阅失败");
            }
        } catch (Exception e) {
            log.error("取消订阅异常: {}", e.getMessage());
            return ApiResponse.error("取消订阅异常: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有消息
     */
    @GetMapping("/messages")
    public ApiResponse<List<MqttMessage>> getAllMessages() {
        try {
            List<MqttMessage> messages = mqttService.getAllMessages();
            ApiResponse<List<MqttMessage>> response = ApiResponse.success(messages);
            response.setMessage("获取消息列表成功");
            return response;
        } catch (Exception e) {
            log.error("获取消息列表异常: {}", e.getMessage());
            return ApiResponse.error("获取消息列表异常: " + e.getMessage());
        }
    }
    
    /**
     * 根据ID获取消息
     */
    @GetMapping("/messages/{id}")
    public ApiResponse<MqttMessage> getMessageById(@PathVariable Long id) {
        try {
            MqttMessage message = mqttService.getMessageById(id);
            if (message != null) {
                ApiResponse<MqttMessage> response = ApiResponse.success(message);
                response.setMessage("获取消息成功");
                return response;
            } else {
                return ApiResponse.error("消息不存在");
            }
        } catch (Exception e) {
            log.error("获取消息异常: {}", e.getMessage());
            return ApiResponse.error("获取消息异常: " + e.getMessage());
        }
    }
    
    /**
     * 根据主题获取消息
     */
    @GetMapping("/messages/topic/{topic}")
    public ApiResponse<List<MqttMessage>> getMessagesByTopic(@PathVariable String topic) {
        try {
            List<MqttMessage> messages = mqttService.getMessagesByTopic(topic);
            ApiResponse<List<MqttMessage>> response = ApiResponse.success(messages);
            response.setMessage("获取主题消息成功");
            return response;
        } catch (Exception e) {
            log.error("获取主题消息异常: {}", e.getMessage());
            return ApiResponse.error("获取主题消息异常: " + e.getMessage());
        }
    }
    
    /**
     * 根据设备ID获取消息
     */
    @GetMapping("/messages/device/{deviceId}")
    public ApiResponse<List<MqttMessage>> getMessagesByDeviceId(@PathVariable String deviceId) {
        try {
            List<MqttMessage> messages = mqttService.getMessagesByDeviceId(deviceId);
            ApiResponse<List<MqttMessage>> response = ApiResponse.success(messages);
            response.setMessage("获取设备消息成功");
            return response;
        } catch (Exception e) {
            log.error("获取设备消息异常: {}", e.getMessage());
            return ApiResponse.error("获取设备消息异常: " + e.getMessage());
        }
    }
    
    /**
     * 删除消息
     */
    @DeleteMapping("/messages/{id}")
    public ApiResponse<Boolean> deleteMessage(@PathVariable Long id) {
        try {
            boolean success = mqttService.deleteMessage(id);
            if (success) {
                ApiResponse<Boolean> response = ApiResponse.success(true);
                response.setMessage("删除消息成功");
                return response;
            } else {
                return ApiResponse.error("删除消息失败");
            }
        } catch (Exception e) {
            log.error("删除消息异常: {}", e.getMessage());
            return ApiResponse.error("删除消息异常: " + e.getMessage());
        }
    }
    
    /**
     * 检查连接状态
     */
    @GetMapping("/status")
    public ApiResponse<Boolean> getConnectionStatus() {
        try {
            boolean connected = mqttService.isConnected();
            ApiResponse<Boolean> response = ApiResponse.success(connected);
            response.setMessage(connected ? "连接正常" : "连接断开");
            return response;
        } catch (Exception e) {
            log.error("检查连接状态异常: {}", e.getMessage());
            return ApiResponse.error("检查连接状态异常: " + e.getMessage());
        }
    }
    
    /**
     * 断开连接
     */
    @PostMapping("/disconnect")
    public ApiResponse<Boolean> disconnect() {
        try {
            mqttService.disconnect();
            ApiResponse<Boolean> response = ApiResponse.success(true);
            response.setMessage("断开连接成功");
            return response;
        } catch (Exception e) {
            log.error("断开连接异常: {}", e.getMessage());
            return ApiResponse.error("断开连接异常: " + e.getMessage());
        }
    }
    
    // 请求参数类
    public static class PublishRequest {
        private String topic;
        private String payload;
        private int qos = 0;
        private boolean retained = false;
        
        // getter和setter
        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }
        
        public String getPayload() { return payload; }
        public void setPayload(String payload) { this.payload = payload; }
        
        public int getQos() { return qos; }
        public void setQos(int qos) { this.qos = qos; }
        
        public boolean isRetained() { return retained; }
        public void setRetained(boolean retained) { this.retained = retained; }
    }
    
    public static class SubscribeRequest {
        private String topic;
        private int qos = 0;
        
        // getter和setter
        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }
        
        public int getQos() { return qos; }
        public void setQos(int qos) { this.qos = qos; }
    }
    
    public static class UnsubscribeRequest {
        private String topic;
        
        // getter和setter
        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }
    }
}