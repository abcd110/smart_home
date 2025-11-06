package com.smarthome.security.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 密码加密工具类
 */
@Component
public class PasswordUtil {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public PasswordUtil() {
        this.bCryptPasswordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * 加密密码
     */
    public String encode(String rawPassword) {
        return bCryptPasswordEncoder.encode(rawPassword);
    }

    /**
     * 验证密码
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        return bCryptPasswordEncoder.matches(rawPassword, encodedPassword);
    }
}