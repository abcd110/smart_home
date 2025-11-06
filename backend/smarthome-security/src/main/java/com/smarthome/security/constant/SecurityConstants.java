package com.smarthome.security.constant;

/**
 * 安全相关常量
 */
public class SecurityConstants {
    
    /**
     * JWT相关常量
     */
    public static final String TOKEN_HEADER = "Authorization";
    public static final String HEADER_STRING = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String TOKEN_TYPE = "JWT";
    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";
    public static final String TOKEN_TYPE_PASSWORD_RESET = "password_reset";
    public static final String SECRET = "secret";
    public static final long EXPIRATION_TIME = 86400000; // 24小时
    public static final long REFRESH_TOKEN_EXPIRATION_TIME = 86400000 * 7; // 7天
    public static final long PASSWORD_RESET_TOKEN_EXPIRATION_TIME = 3600000; // 1小时
    
    /**
     * 权限相关常量
     */
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_DEVICE = "ROLE_DEVICE";
    public static final String ROLE_GUEST = "ROLE_GUEST";
    
    /**
     * 权限字符串
     */
    public static final String PERMISSION_READ = "READ";
    public static final String PERMISSION_WRITE = "WRITE";
    public static final String PERMISSION_DELETE = "DELETE";
    public static final String PERMISSION_MANAGE = "MANAGE";
    
    /**
     * URL路径常量
     */
    public static final String AUTH_PATH = "/api/auth/**";
    public static final String PUBLIC_PATH = "/api/public/**";
    public static final String ADMIN_PATH = "/api/admin/**";
    
    /**
     * 密码相关常量
     */
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 128;
    
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