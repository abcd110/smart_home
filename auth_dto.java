// 认证相关DTO
package com.smarthome.dto.auth;

import lombok.Data;

/**
 * 注册请求DTO
 */
@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String username;
    private String fullName;
    private String phone;
}

/**
 * 登录请求DTO
 */
@Data
public class LoginRequest {
    private String email;
    private String password;
    private String deviceId;
    private String platform; // android, ios
}

/**
 * 密码重置请求DTO
 */
@Data
public class ResetPasswordRequest {
    private String token;
    private String newPassword;
    private String confirmPassword;
}

/**
 * 第三方登录请求DTO
 */
@Data
public class SocialLoginRequest {
    private String provider; // google, wechat, qq
    private String accessToken;
    private String refreshToken;
    private String userInfo;
}

/**
 * 令牌刷新请求DTO
 */
@Data
public class RefreshTokenRequest {
    private String refreshToken;
    private String deviceId;
}

/**
 * 更新用户信息请求DTO
 */
@Data
public class UpdateUserRequest {
    private String fullName;
    private String phone;
    private String username;
    private String avatar;
}

/**
 * 认证响应DTO
 */
@Data
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private long expiresIn;
    private String userId;
    private String email;
    private String username;
    private String fullName;
    private String role;
    private boolean success;
    private String message;
    private String error;
    
    public static AuthResponse success(String accessToken, String refreshToken, UserInfo userInfo) {
        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(3600);
        response.setUserId(userInfo.getUserId());
        response.setEmail(userInfo.getEmail());
        response.setUsername(userInfo.getUsername());
        response.setFullName(userInfo.getFullName());
        response.setRole(userInfo.getRole());
        response.setSuccess(true);
        response.setMessage("登录成功");
        return response;
    }
    
    public static AuthResponse error(String error) {
        AuthResponse response = new AuthResponse();
        response.setSuccess(false);
        response.setMessage("登录失败");
        response.setError(error);
        return response;
    }
}

/**
 * 用户信息DTO
 */
@Data
class UserInfo {
    private String userId;
    private String email;
    private String username;
    private String fullName;
    private String role;
    private String avatar;
    private String phone;
}