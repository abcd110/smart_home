package com.smarthome.auth.controller;

import com.smarthome.common.model.ApiResponse;
import com.smarthome.auth.dto.LoginRequest;
import com.smarthome.auth.dto.RegisterRequest;
import com.smarthome.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ApiResponse<?> register(@Valid @RequestBody RegisterRequest request) {
        log.info("用户注册请求: {}", request.getUsername());
        authService.register(request);
        ApiResponse<?> response = ApiResponse.success("注册成功");
        response.setMessage("用户注册成功");
        return response;
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse<?> login(@Valid @RequestBody LoginRequest request) {
        log.info("用户登录请求: {}", request.getUsername());
        String token = authService.login(request);
        ApiResponse<?> response = ApiResponse.success(token);
        response.setMessage("用户登录成功");
        return response;
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    public ApiResponse<?> refreshToken(@RequestHeader("Authorization") String token) {
        log.info("刷新Token请求");
        String newToken = authService.refreshToken(token);
        ApiResponse<?> response = ApiResponse.success(newToken);
        response.setMessage("Token刷新成功");
        return response;
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ApiResponse<?> logout(@RequestHeader("Authorization") String token) {
        log.info("用户登出请求");
        authService.logout(token);
        ApiResponse<?> response = ApiResponse.success("登出成功");
        response.setMessage("用户登出成功");
        return response;
    }
}