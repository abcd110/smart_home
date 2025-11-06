package com.smarthome.security.controller;

import com.smarthome.security.dto.*;
import com.smarthome.security.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 安全认证控制器
 * 
 * @author SmartHome Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/security")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "安全认证管理", description = "用户安全认证相关接口")
public class SecurityController {
    
    private final com.smarthome.security.service.AuthService securityAuthService;
    
    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求
     * @return JWT认证响应
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户名/邮箱和密码登录，返回JWT令牌")
    public ResponseEntity<ApiResponse<JwtAuthenticationResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest) {
        log.info("用户登录请求: {}", loginRequest.getUsernameOrEmail());
        
        JwtAuthenticationResponse response = securityAuthService.login(loginRequest);
        
        return ResponseEntity.ok(ApiResponse.success("登录成功", response));
    }
    
    /**
     * 用户注册
     * 
     * @param registerRequest 注册请求
     * @return JWT认证响应
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "用户注册，注册成功后自动登录并返回JWT令牌")
    public ResponseEntity<ApiResponse<JwtAuthenticationResponse>> register(
            @Valid @RequestBody RegisterRequest registerRequest) {
        log.info("用户注册请求: {}", registerRequest.getUsername());
        
        JwtAuthenticationResponse response = securityAuthService.register(registerRequest);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("注册成功", response));
    }
    
    /**
     * 刷新令牌
     * 
     * @param refreshToken 刷新令牌
     * @return 新的JWT认证响应
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    public ResponseEntity<ApiResponse<JwtAuthenticationResponse>> refreshToken(
            @RequestParam(value = "refreshToken") @NotBlank(message = "刷新令牌不能为空") String refreshToken) {
        log.info("刷新令牌请求");
        
        JwtAuthenticationResponse response = securityAuthService.refreshToken(refreshToken);
        
        return ResponseEntity.ok(ApiResponse.success("令牌刷新成功", response));
    }
    
    /**
     * 用户登出
     * 
     * @return 响应结果
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户登出，使令牌失效")
    public ResponseEntity<ApiResponse<Void>> logout() {
        log.info("用户登出请求");
        
        securityAuthService.logout();
        
        return ResponseEntity.ok(ApiResponse.success("登出成功", null));
    }
    
    /**
     * 请求密码重置
     * 
     * @param passwordResetRequest 密码重置请求
     * @return 响应结果
     */
    @PostMapping("/password/reset-request")
    @Operation(summary = "请求密码重置", description = "通过邮箱请求密码重置链接")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequest passwordResetRequest) {
        log.info("密码重置请求: {}", passwordResetRequest.getEmail());
        
        securityAuthService.requestPasswordReset(passwordResetRequest.getEmail());
        
        return ResponseEntity.ok(ApiResponse.success("密码重置邮件已发送，请查收", null));
    }
    
    /**
     * 确认密码重置
     * 
     * @param passwordResetConfirm 密码重置确认
     * @return 响应结果
     */
    @PostMapping("/password/reset-confirm")
    @Operation(summary = "确认密码重置", description = "使用重置令牌设置新密码")
    public ResponseEntity<ApiResponse<Void>> confirmPasswordReset(
            @Valid @RequestBody PasswordResetConfirm passwordResetConfirm) {
        log.info("密码重置确认请求");
        
        securityAuthService.confirmPasswordReset(passwordResetConfirm.getToken(), 
                passwordResetConfirm.getNewPassword());
        
        return ResponseEntity.ok(ApiResponse.success("密码重置成功", null));
    }
    
    /**
     * 验证令牌有效性
     * 
     * @param token JWT令牌
     * @return 验证结果
     */
    @GetMapping("/validate")
    @Operation(summary = "验证令牌", description = "验证JWT令牌是否有效")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(
            @RequestParam(value = "token") @NotBlank(message = "令牌不能为空") String token) {
        log.info("验证令牌请求");
        
        boolean isValid = securityAuthService.validateToken(token);
        
        return ResponseEntity.ok(ApiResponse.success("令牌验证完成", isValid));
    }
}