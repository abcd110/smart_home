package com.smarthome.mqtt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MQTT模块启动类
 */
@SpringBootApplication
public class MqttApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MqttApplication.class, args);
    }
}