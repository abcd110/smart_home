package com.smarthome.auth.service;

import com.smarthome.auth.dto.LoginRequest;
import com.smarthome.auth.dto.RegisterRequest;

/**
 * 认证服务接口
 */
public interface AuthService {
    
    /**
     * 用户注册
     */
    void register(RegisterRequest request);
    
    /**
     * 用户登录
     */
    String login(LoginRequest request);
    
    /**
     * 刷新Token
     */
    String refreshToken(String token);
    
    /**
     * 用户登出
     */
    void logout(String token);
    
    /**
     * 验证Token
     */
    boolean validateToken(String token);
}