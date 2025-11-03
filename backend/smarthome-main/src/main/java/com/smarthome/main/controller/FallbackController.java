package com.smarthome.main.controller;

import com.smarthome.common.model.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * 熔断降级控制器
 * 处理服务不可用时的降级响应
 */
@RestController
public class FallbackController {
    
    /**
     * 全局降级处理
     */
    @RequestMapping("/fallback")
    public Mono<ApiResponse<String>> fallback() {
        return Mono.just(ApiResponse.error(
            HttpStatus.SERVICE_UNAVAILABLE.value(), 
            "服务暂时不可用，请稍后重试"
        ));
    }
}