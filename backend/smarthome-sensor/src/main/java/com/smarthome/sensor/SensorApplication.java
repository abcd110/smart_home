package com.smarthome.sensor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 传感器模块启动类
 */
@SpringBootApplication
public class SensorApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SensorApplication.class, args);
    }
}