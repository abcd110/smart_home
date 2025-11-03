package com.smarthome.main.controller;

import com.smarthome.common.model.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查控制器
 * 提供网关服务的健康状态检查接口
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {
    
    /**
     * 健康检查接口
     */
    @GetMapping
    public ApiResponse<String> health() {
        return ApiResponse.success("Gateway service is healthy");
    }
    
    /**
     * 就绪检查接口
     */
    @GetMapping("/ready")
    public ApiResponse<String> ready() {
        return ApiResponse.success("Gateway service is ready");
    }
    
    /**
     * 存活检查接口
     */
    @GetMapping("/live")
    public ApiResponse<String> live() {
        return ApiResponse.success("Gateway service is alive");
    }
}