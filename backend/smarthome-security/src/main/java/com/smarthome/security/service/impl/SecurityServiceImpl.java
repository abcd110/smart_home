package com.smarthome.security.service.impl;

import com.smarthome.security.dto.JwtAuthenticationResponse;
import com.smarthome.security.dto.LoginRequest;
import com.smarthome.security.dto.RegisterRequest;
import com.smarthome.security.entity.User;
import com.smarthome.security.exception.AuthenticationException;
import com.smarthome.security.exception.ResourceNotFoundException;
import com.smarthome.security.repository.UserRepository;
import com.smarthome.security.service.AuthService;
import com.smarthome.security.service.UserService;
import com.smarthome.security.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import com.smarthome.security.util.PasswordUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 安全认证服务实现类
 * 
 * @author SmartHome Team
 * @since 1.0.0
 */
@Service("securityAuthService")
@RequiredArgsConstructor
@Slf4j
public class SecurityServiceImpl implements AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final PasswordUtil passwordEncoder;
    
    @Override
    @Transactional
    public JwtAuthenticationResponse login(LoginRequest loginRequest) {
        String usernameOrEmail = loginRequest.getUsernameOrEmail();
        String password = loginRequest.getPassword();
        
        log.debug("尝试登录用户: {}", usernameOrEmail);
        
        // 查找用户
        User user = userService.findByUsernameOrEmail(usernameOrEmail)
                .orElseThrow(() -> new AuthenticationException("用户名或密码错误"));
        
        // 检查用户状态
        if (!user.isEnabled()) {
            throw new AuthenticationException("账户已被禁用");
        }
        
        if (!user.isAccountNonLocked()) {
            throw new AuthenticationException("账户已被锁定");
        }
        
        if (!user.isAccountNonExpired()) {
            throw new AuthenticationException("账户已过期");
        }
        
        if (!user.isCredentialsNonExpired()) {
            throw new AuthenticationException("凭证已过期");
        }
        
        // 进行身份验证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(usernameOrEmail, password));
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // 生成令牌
        String accessToken = jwtTokenUtil.generateToken(user.getUsername());
        String refreshToken = jwtTokenUtil.generateRefreshToken(user.getUsername());
        
        // 获取用户角色
        List<String> roles = user.getRoles().stream()
                .map(role -> "ROLE_" + role.getName())
                .collect(Collectors.toList());
        
        // 构建响应
        JwtAuthenticationResponse response = new JwtAuthenticationResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(jwtTokenUtil.getExpirationInSeconds());
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRoles(roles);
        
        log.info("用户登录成功: {}", user.getUsername());
        return response;
    }
    
    @Override
    @Transactional
    public JwtAuthenticationResponse register(RegisterRequest registerRequest) {
        String username = registerRequest.getUsername();
        String email = registerRequest.getEmail();
        String password = registerRequest.getPassword();
        
        log.debug("尝试注册用户: {}", username);
        
        // 验证密码确认
        if (!password.equals(registerRequest.getConfirmPassword())) {
            throw new AuthenticationException("密码和确认密码不匹配");
        }
        
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(username)) {
            throw new AuthenticationException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(email)) {
            throw new AuthenticationException("邮箱已被注册");
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setPhone(registerRequest.getPhone());
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        user.setAccountNonExpired(true);
        user.setCredentialsNonExpired(true);
        
        // 保存用户
        user = userRepository.save(user);
        
        // 自动登录
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsernameOrEmail(username);
        loginRequest.setPassword(password);
        
        return login(loginRequest);
    }
    
    @Override
    @Transactional
    public JwtAuthenticationResponse refreshToken(String refreshToken) {
        log.debug("刷新令牌请求");
        
        // 验证刷新令牌格式
        if (!jwtTokenUtil.validateTokenFormat(refreshToken)) {
            throw new AuthenticationException("无效的刷新令牌");
        }
        
        // 检查是否为刷新令牌
        if (!jwtTokenUtil.isRefreshToken(refreshToken)) {
            throw new AuthenticationException("无效的刷新令牌类型");
        }
        
        // 从令牌中获取用户名
        String username = jwtTokenUtil.getUsernameFromToken(refreshToken);
        
        // 查找用户
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("用户不存在"));
        
        // 验证令牌
        UserDetails userDetails = userService.createUserDetails(user);
        if (!jwtTokenUtil.validateToken(refreshToken, userDetails)) {
            throw new AuthenticationException("刷新令牌已过期或无效");
        }
        
        // 生成新的访问令牌
        String newAccessToken = jwtTokenUtil.generateToken(username);
        
        // 获取用户角色
        List<String> roles = user.getRoles().stream()
                .map(role -> "ROLE_" + role.getName())
                .collect(Collectors.toList());
        
        // 构建响应
        JwtAuthenticationResponse response = new JwtAuthenticationResponse();
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(refreshToken); // 保持相同的刷新令牌
        response.setExpiresIn(jwtTokenUtil.getExpirationInSeconds());
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRoles(roles);
        
        log.info("令牌刷新成功: {}", user.getUsername());
        return response;
    }
    
    @Override
    public void logout() {
        // 清除安全上下文
        SecurityContextHolder.clearContext();
        log.info("用户已登出");
    }
    
    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        log.debug("请求密码重置: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("邮箱不存在"));
        
        // 生成重置令牌
        String resetToken = jwtTokenUtil.generatePasswordResetToken(user.getUsername());
        
        // TODO: 发送重置邮件
        log.info("密码重置令牌已生成: {}", resetToken);
    }
    
    @Override
    @Transactional
    public void confirmPasswordReset(String token, String newPassword) {
        log.debug("确认密码重置");
        
        // 验证令牌格式
        if (!jwtTokenUtil.validateTokenFormat(token)) {
            throw new AuthenticationException("无效的重置令牌");
        }
        
        // 检查是否为密码重置令牌
        if (!jwtTokenUtil.isPasswordResetToken(token)) {
            throw new AuthenticationException("无效的令牌类型");
        }
        
        // 从令牌中获取用户名
        String username = jwtTokenUtil.getUsernameFromToken(token);
        
        // 查找用户
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("用户不存在"));
        
        // 验证令牌
        UserDetails userDetails = userService.createUserDetails(user);
        if (!jwtTokenUtil.validateToken(token, userDetails)) {
            throw new AuthenticationException("重置令牌已过期或无效");
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        log.info("密码重置成功: {}", user.getUsername());
    }
    
    @Override
    public boolean validateToken(String token) {
        try {
            // 验证令牌格式
            if (!jwtTokenUtil.validateTokenFormat(token)) {
                return false;
            }
            
            // 从令牌中获取用户名
            String username = jwtTokenUtil.getUsernameFromToken(token);
            
            // 查找用户
            User user = userRepository.findByUsername(username)
                    .orElse(null);
            
            if (user == null) {
                return false;
            }
            
            // 验证令牌
            UserDetails userDetails = userService.createUserDetails(user);
            return jwtTokenUtil.validateToken(token, userDetails);
        } catch (Exception e) {
            log.debug("令牌验证失败: {}", e.getMessage());
            return false;
        }
    }
}