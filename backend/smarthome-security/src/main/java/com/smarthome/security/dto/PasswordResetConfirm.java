package com.smarthome.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 密码重置确认DTO
 * 
 * @author SmartHome Team
 * @since 1.0.0
 */
@Data
public class PasswordResetConfirm {
    
    /**
     * 重置令牌
     */
    @NotBlank(message = "重置令牌不能为空")
    private String token;
    
    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 40, message = "密码长度必须在6-40个字符之间")
    private String newPassword;
    
    /**
     * 确认新密码
     */
    @NotBlank(message = "确认新密码不能为空")
    private String confirmNewPassword;
}