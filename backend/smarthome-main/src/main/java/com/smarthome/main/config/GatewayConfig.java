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
                        .filters(f -> f.stripPrefix(1))
                        .uri("http://localhost:8082"))
                
                // 设备服务路由
                .route("device-service", r -> r.path("/api/devices/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("http://localhost:8083"))
                
                // 场景服务路由
                .route("scene-service", r -> r.path("/api/scenes/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("http://localhost:8084"))
                
                // 告警服务路由
                .route("alarm-service", r -> r.path("/api/alarms/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("http://localhost:8085"))
                
                // 传感器服务路由
                .route("sensor-service", r -> r.path("/api/sensors/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("http://localhost:8086"))
                
                // 安全事件服务路由
                .route("security-event-service", r -> r.path("/api/security-events/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("http://localhost:8087"))
                
                // MQTT服务路由
                .route("mqtt-service", r -> r.path("/api/mqtt/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("http://localhost:8088"))
                
                // 网关服务路由
                .route("gateway-service", r -> r.path("/api/gateways/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("http://localhost:8089"))
                
                // 认证服务路由
                .route("auth-service", r -> r.path("/api/auth/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("http://localhost:8082"))
                
                .build();
    }
}