package com.smarthome.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 智能家居主应用启动类
 * 作为微服务架构的入口和网关服务
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.smarthome")
public class SmarthomeMainApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SmarthomeMainApplication.class, args);
    }
}