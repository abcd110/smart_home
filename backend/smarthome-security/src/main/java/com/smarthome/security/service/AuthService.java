package com.smarthome.security.service;

import com.smarthome.security.dto.JwtAuthenticationResponse;
import com.smarthome.security.dto.LoginRequest;
import com.smarthome.security.dto.RegisterRequest;

/**
 * 认证服务接口
 * 
 * @author SmartHome Team
 * @since 1.0.0
 */
public interface AuthService {
    
    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求
     * @return JWT认证响应
     */
    JwtAuthenticationResponse login(LoginRequest loginRequest);
    
    /**
     * 用户注册
     * 
     * @param registerRequest 注册请求
     * @return JWT认证响应
     */
    JwtAuthenticationResponse register(RegisterRequest registerRequest);
    
    /**
     * 刷新令牌
     * 
     * @param refreshToken 刷新令牌
     * @return 新的JWT认证响应
     */
    JwtAuthenticationResponse refreshToken(String refreshToken);
    
    /**
     * 用户登出
     */
    void logout();
    
    /**
     * 请求密码重置
     * 
     * @param email 邮箱
     */
    void requestPasswordReset(String email);
    
    /**
     * 确认密码重置
     * 
     * @param token 重置令牌
     * @param newPassword 新密码
     */
    void confirmPasswordReset(String token, String newPassword);
    
    /**
     * 验证令牌有效性
     * 
     * @param token JWT令牌
     * @return 是否有效
     */
    boolean validateToken(String token);
}