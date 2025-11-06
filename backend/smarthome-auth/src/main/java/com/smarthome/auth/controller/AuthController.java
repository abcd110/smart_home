package com.smarthome.auth.controller;

import com.smarthome.auth.dto.LoginRequest;
import com.smarthome.auth.dto.RegisterRequest;
import com.smarthome.auth.service.AuthService;
import com.smarthome.common.model.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

/**
 * 认证控制器
 */
@Tag(name = "认证管理", description = "用户认证相关接口，包括注册、登录、刷新令牌和登出功能")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    /**
     * 用户注册
     */
    @Operation(
        summary = "用户注册",
        description = "新用户注册接口，创建用户账户并分配初始权限",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "注册请求参数",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = RegisterRequest.class)
            )
        )
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "注册成功", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "请求参数错误", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "用户名已存在", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping("/register")
    public ApiResponse<?> register(@Valid @RequestBody RegisterRequest request) {
        log.info("用户注册请求: {}", request.getUsername());
        try {
            authService.register(request);
            ApiResponse<?> response = ApiResponse.success("注册成功");
            response.setMessage("用户注册成功");
            return response;
        } catch (RuntimeException e) {
            log.error("用户注册失败: {}", e.getMessage());
            ApiResponse<?> response = ApiResponse.error(e.getMessage());
            return response;
        }
    }

    /**
     * 用户登录
     */
    @Operation(
        summary = "用户登录",
        description = "用户登录接口，验证用户凭据并生成JWT令牌",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "登录请求参数",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = LoginRequest.class)
            )
        )
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "登录成功，返回JWT令牌", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "用户名或密码错误", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "账户已被锁定", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping("/login")
    public ApiResponse<?> login(@Valid @RequestBody LoginRequest request) {
        log.info("用户登录请求");
        String token = authService.login(request);
        ApiResponse<?> response = ApiResponse.success(token);
        response.setMessage("用户登录成功");
        return response;
    }

    /**
     * 刷新Token
     */
    @Operation(
        summary = "刷新令牌",
        description = "使用旧令牌刷新获取新的JWT令牌",
        parameters = @Parameter(
            name = "Authorization",
            description = "Bearer {旧令牌}",
            required = true,
            in = ParameterIn.HEADER
        )
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "令牌刷新成功，返回新令牌", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "令牌无效或已过期", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
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
    @Operation(
        summary = "用户登出",
        description = "用户登出接口，使当前令牌失效",
        parameters = @Parameter(
            name = "Authorization",
            description = "Bearer {令牌}",
            required = true,
            in = ParameterIn.HEADER
        )
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "登出成功", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "令牌无效", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping("/logout")
    public ApiResponse<?> logout(@RequestHeader("Authorization") String token) {
        log.info("用户登出请求");
        authService.logout(token);
        ApiResponse<?> response = ApiResponse.success("登出成功");
        response.setMessage("用户登出成功");
        return response;
    }

    /**
     * 验证Token
     */
    @Operation(
        summary = "验证令牌",
        description = "验证JWT令牌的有效性",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "验证请求参数",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = String.class)
            )
        )
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "令牌有效", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "令牌无效或已过期", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping("/verify")
    public ApiResponse<?> verifyToken(@RequestBody Map<String, String> request) {
        log.info("验证Token请求");
        String token = request.get("token");
        boolean isValid = authService.validateToken(token);
        ApiResponse<?> response = ApiResponse.success(isValid);
        response.setMessage(isValid ? "Token有效" : "Token无效");
        return response;
    }
}