package com.smarthome.auth.service.impl;

import com.smarthome.auth.dto.LoginRequest;
import com.smarthome.auth.dto.RegisterRequest;
import com.smarthome.auth.service.AuthService;
import com.smarthome.security.entity.User;
import com.smarthome.security.repository.UserRepository;
import com.smarthome.security.util.JwtUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务实现类
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        log.info("处理用户注册: {}", request.getUsername());
        
        // 1. 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 2. 检查邮箱是否已存在
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }
        
        // 3. 检查手机号是否已存在
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("手机号已被注册");
        }
        
        // 4. 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        user.setAccountNonExpired(true);
        user.setCredentialsNonExpired(true);
        
        // 5. 加密密码
        String encryptedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(encryptedPassword);
        log.info("密码加密完成");
        
        // 6. 保存用户信息
        userRepository.save(user);
        log.info("用户注册成功: {}", user.getUsername());
    }

    @Override
    public String login(LoginRequest request) {
        log.info("处理用户登录: {}", request.getUsername());
        
        // 1. 查找用户
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));
        
        // 2. 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 3. 检查用户状态
        if (!user.isEnabled()) {
            throw new RuntimeException("账户已被禁用");
        }
        
        if (!user.isAccountNonLocked()) {
            throw new RuntimeException("账户已被锁定");
        }
        
        if (!user.isAccountNonExpired()) {
            throw new RuntimeException("账户已过期");
        }
        
        if (!user.isCredentialsNonExpired()) {
            throw new RuntimeException("凭证已过期");
        }
        
        // 4. 生成JWT Token
        String token = jwtUtils.generateToken(user.getUsername(), user.getId());
        log.info("用户登录成功: {}", user.getUsername());
        
        return token;
    }

    @Override
    public String refreshToken(String token) {
        log.info("处理Token刷新");
        
        // 处理Bearer前缀
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        // 1. 验证原Token
        if (!jwtUtils.validateToken(token)) {
            throw new RuntimeException("Token无效或已过期");
        }
        
        // 2. 检查Token是否过期
        if (jwtUtils.isTokenExpired(token)) {
            throw new RuntimeException("Token已过期，请重新登录");
        }
        
        // 3. 从原Token中获取用户信息
        String username = jwtUtils.getUsernameFromToken(token);
        Long userId = jwtUtils.getUserIdFromToken(token);
        
        // 4. 验证用户是否存在且状态正常
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (!user.isEnabled()) {
            throw new RuntimeException("账户已被禁用");
        }
        
        if (!user.isAccountNonLocked()) {
            throw new RuntimeException("账户已被锁定");
        }
        
        // 5. 生成新Token
        String newToken = jwtUtils.generateToken(username, userId);
        log.info("Token刷新成功，用户: {}", username);
        
        // TODO: 使原Token失效（需要实现Token黑名单机制）
        
        return newToken;
    }

    @Override
    public void logout(String token) {
        log.info("处理用户登出");
        
        // TODO: 实现用户登出逻辑
        // 1. 将Token加入黑名单
        // 2. 清除用户会话
        // 3. 记录登出日志
        
        log.info("用户登出成功");
    }
    
    @Override
    public boolean validateToken(String token) {
        log.info("验证Token有效性");
        return jwtUtils.validateToken(token);
    }
}