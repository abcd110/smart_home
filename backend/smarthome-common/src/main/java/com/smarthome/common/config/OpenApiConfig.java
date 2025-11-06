package com.smarthome.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 全局OpenAPI配置类
 * 为智慧社区系统提供统一的API文档配置
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "智慧社区系统 API",
        description = "智慧社区系统后端API文档",
        version = "1.0.0",
        contact = @Contact(
            name = "SmartHome Team",
            email = "team@smarthome.com",
            url = "http://www.smarthome.com"
        ),
        license = @License(
            name = "Apache 2.0",
            url = "http://www.apache.org/licenses/LICENSE-2.0.html"
        )
    ),
    servers = {
        @Server(url = "/", description = "本地开发环境"),
        @Server(url = "http://8.134.63.151:8080", description = "生产环境")
    },
    security = @SecurityRequirement(name = "BearerAuth")
)
@SecuritySchemes({
    @SecurityScheme(
        name = "BearerAuth",
        type = io.swagger.v3.oas.annotations.enums.SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT令牌认证",
        in = io.swagger.v3.oas.annotations.enums.SecuritySchemeIn.HEADER
    )
})
public class OpenApiConfig {

    /**
     * 认证模块API分组
     */
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("认证模块")
                .pathsToMatch("/api/auth/**")
                .build();
    }

    /**
     * 用户模块API分组
     */
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("用户管理")
                .pathsToMatch("/api/users/**")
                .build();
    }

    /**
     * 设备模块API分组
     */
    @Bean
    public GroupedOpenApi deviceApi() {
        return GroupedOpenApi.builder()
                .group("设备管理")
                .pathsToMatch("/api/devices/**")
                .build();
    }

    /**
     * 传感器模块API分组
     */
    @Bean
    public GroupedOpenApi sensorApi() {
        return GroupedOpenApi.builder()
                .group("传感器管理")
                .pathsToMatch("/api/sensors/**")
                .build();
    }

    /**
     * 安全事件模块API分组
     */
    @Bean
    public GroupedOpenApi securityEventApi() {
        return GroupedOpenApi.builder()
                .group("安全事件")
                .pathsToMatch("/api/security-events/**")
                .build();
    }

    /**
     * MQTT模块API分组
     */
    @Bean
    public GroupedOpenApi mqttApi() {
        return GroupedOpenApi.builder()
                .group("MQTT服务")
                .pathsToMatch("/api/mqtt/**")
                .build();
    }

    /**
     * 网关模块API分组
     */
    @Bean
    public GroupedOpenApi gatewayApi() {
        return GroupedOpenApi.builder()
                .group("网关管理")
                .pathsToMatch("/api/gateways/**")
                .build();
    }

    /**
     * 公共API分组
     */
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("公共接口")
                .pathsToMatch("/api/public/**", "/actuator/**")
                .build();
    }
}