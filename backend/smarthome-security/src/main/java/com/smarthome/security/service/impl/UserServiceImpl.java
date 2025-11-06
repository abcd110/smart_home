package com.smarthome.security.service.impl;

import com.smarthome.security.entity.User;
import com.smarthome.security.entity.Role;
import com.smarthome.security.repository.UserRepository;
import com.smarthome.security.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 * 
 * @author SmartHome Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        log.debug("根据用户名或邮箱查找用户: {}", usernameOrEmail);
        return userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);
    }
    
    @Override
    public UserDetails createUserDetails(User user) {
        log.debug("创建用户详情对象: {}", user.getUsername());
        
        // 完全避免使用Hibernate的PersistentSet对象，手动收集角色和权限名称
        Set<GrantedAuthority> authorities = new HashSet<>();
        
        // 手动遍历角色，避免使用可能触发循环引用的集合操作
        if (user.getRoles() != null) {
            for (Role role : user.getRoles()) {
                // 只添加角色名称，不触发对角色对象的进一步操作
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
            }
        }
        
        // 创建UserDetails对象，使用简单的HashSet而不是Hibernate的PersistentSet
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(!user.isAccountNonExpired())
                .accountLocked(!user.isAccountNonLocked())
                .credentialsExpired(!user.isCredentialsNonExpired())
                .disabled(!user.isEnabled())
                .build();
    }
}