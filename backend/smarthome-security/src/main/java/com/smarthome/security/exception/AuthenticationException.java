package com.smarthome.security.exception;

/**
 * 认证异常
 * 
 * @author SmartHome Team
 * @since 1.0.0
 */
public class AuthenticationException extends RuntimeException {
    
    public AuthenticationException(String message) {
        super(message);
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}