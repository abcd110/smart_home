package com.smarthome.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

/**
 * 验证配置类
 * 
 * @author SmartHome Team
 * @since 1.0.0
 */
@Configuration
public class ValidationConfig {
    
    /**
     * 配置方法验证后处理器
     * 启用方法级别的参数验证
     */
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }
}