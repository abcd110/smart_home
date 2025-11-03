package com.smarthome.common.constant;

/**
 * 错误码枚举
 */
public enum ErrorCode {
    
    // 成功
    SUCCESS(200, "成功"),
    
    // 通用错误
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "方法不允许"),
    INTERNAL_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),
    
    // 业务错误
    USER_NOT_EXIST(1001, "用户不存在"),
    USER_EXISTS(1002, "用户已存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    TOKEN_EXPIRED(1004, "Token已过期"),
    TOKEN_INVALID(1005, "Token无效"),
    DEVICE_NOT_EXIST(2001, "设备不存在"),
    DEVICE_OFFLINE(2002, "设备离线"),
    DEVICE_CONTROL_FAILED(2003, "设备控制失败"),
    SENSOR_DATA_ERROR(3001, "传感器数据错误"),
    MQTT_CONNECTION_ERROR(4001, "MQTT连接错误"),
    MQTT_PUBLISH_ERROR(4002, "MQTT发布错误"),
    MQTT_SUBSCRIBE_ERROR(4003, "MQTT订阅错误");
    
    private final int code;
    private final String message;
    
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}