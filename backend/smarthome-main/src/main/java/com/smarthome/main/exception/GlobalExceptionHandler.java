package com.smarthome.main.exception;

import com.smarthome.common.model.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebInputException;

/**
 * 全局异常处理器
 * 统一处理网关层面的异常
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(ServerWebInputException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidationException(ServerWebInputException e) {
        log.warn("参数验证异常: {}", e.getMessage());
        return ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "请求参数不合法");
    }
    
    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: {}", e.getMessage(), e);
        return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "系统内部错误");
    }
    
    /**
     * 处理所有其他异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "系统异常，请稍后重试");
    }
}