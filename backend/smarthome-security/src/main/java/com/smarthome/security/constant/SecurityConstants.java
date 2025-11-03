package com.smarthome.security.constant;

/**
 * 安全相关常量
 */
public class SecurityConstants {
    
    /**
     * JWT相关常量
     */
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String TOKEN_TYPE = "JWT";
    
    /**
     * 权限相关常量
     */
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_DEVICE = "ROLE_DEVICE";
    
    /**
     * 权限字符串
     */
    public static final String PERMISSION_READ = "READ";
    public static final String PERMISSION_WRITE = "WRITE";
    public static final String PERMISSION_DELETE = "DELETE";
    public static final String PERMISSION_MANAGE = "MANAGE";
    
    /**
     * 安全配置常量
     */
    public static final String[] PUBLIC_URLS = {
        "/api/auth/**",
        "/api/public/**",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/webjars/**",
        "/actuator/health"
    };
}