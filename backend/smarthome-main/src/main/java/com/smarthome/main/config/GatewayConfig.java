package com.smarthome.main.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 网关路由配置类
 * 配置各个微服务的路由规则
 */
@Configuration
public class GatewayConfig {
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // 用户服务路由
                .route("user-service", r -> r.path("/api/users/**")
                        .uri("lb://smarthome-user"))
                
                // 设备服务路由
                .route("device-service", r -> r.path("/api/devices/**")
                        .uri("lb://smarthome-device"))
                
                // 场景服务路由
                .route("scene-service", r -> r.path("/api/scenes/**")
                        .uri("lb://smarthome-scene"))
                
                // 告警服务路由
                .route("alarm-service", r -> r.path("/api/alarms/**")
                        .uri("lb://smarthome-alarm"))
                
                // 传感器服务路由
                .route("sensor-service", r -> r.path("/api/sensors/**")
                        .uri("lb://smarthome-sensor"))
                
                // 安全事件服务路由
                .route("security-event-service", r -> r.path("/api/security-events/**")
                        .uri("lb://smarthome-security-event"))
                
                // MQTT服务路由
                .route("mqtt-service", r -> r.path("/api/mqtt/**")
                        .uri("lb://smarthome-mqtt"))
                
                // 网关服务路由
                .route("gateway-service", r -> r.path("/api/gateways/**")
                        .uri("lb://smarthome-gateway"))
                
                .build();
    }
}