package com.smarthome.security.service;

import com.smarthome.security.entity.User;
import com.smarthome.security.entity.Role;
import com.smarthome.security.entity.Permission;
import com.smarthome.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户详情服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    /**
     * 根据用户名加载用户详情
     * 
     * @param username 用户名
     * @return 用户详情
     * @throws UsernameNotFoundException 用户名不存在异常
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("正在加载用户详情: {}", username);
        
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> {
                    log.warn("用户不存在: {}", username);
                    return new UsernameNotFoundException("用户不存在: " + username);
                });
        
        // 检查账户是否被锁定且锁定时间已过期
        if (!user.isAccountNonLocked() && user.getLockTime() != null && user.getLockTime().isBefore(LocalDateTime.now())) {
            user.setAccountNonLocked(true);
            user.setLockTime(null);
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
            log.info("用户账户锁定已自动解除: {}", username);
        }
        
        log.debug("成功加载用户详情: {}", username);
        return createUserDetails(user);
    }
    
    /**
     * 创建用户详情对象
     * 
     * @param user 用户实体
     * @return 用户详情对象
     */
    private UserDetails createUserDetails(User user) {
        // 完全避免使用Hibernate的PersistentSet对象，手动收集角色和权限名称
        Set<GrantedAuthority> authorities = new HashSet<>();
        
        // 手动遍历角色，避免使用可能触发循环引用的集合操作
        if (user.getRoles() != null) {
            for (Role role : user.getRoles()) {
                // 只添加角色名称，不触发对角色对象的进一步操作
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
                
                // 同样手动处理权限，避免复杂的嵌套集合操作
                if (role.getPermissions() != null) {
                    for (Permission permission : role.getPermissions()) {
                        authorities.add(new SimpleGrantedAuthority(permission.getName()));
                    }
                }
            }
        }
        
        // 创建UserDetails对象，使用简单的HashSet而不是Hibernate的PersistentSet
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            user.isEnabled(),
            user.isAccountNonExpired(),
            user.isCredentialsNonExpired(),
            user.isAccountNonLocked(),
            authorities
        );
    }
    
    /**
     * 根据用户ID加载用户详情
     * 
     * @param userId 用户ID
     * @return 用户详情
     * @throws UsernameNotFoundException 用户不存在异常
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        log.debug("正在根据ID加载用户详情: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("用户ID不存在: {}", userId);
                    return new UsernameNotFoundException("用户ID不存在: " + userId);
                });
        
        log.debug("成功根据ID加载用户详情: {}", userId);
        return createUserDetails(user);
    }
    
    /**
     * 检查用户是否存在
     * 
     * @param username 用户名
     * @return 是否存在
     */
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    /**
     * 检查邮箱是否存在
     * 
     * @param email 邮箱
     * @return 是否存在
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * 检查手机号是否存在
     * 
     * @param phone 手机号
     * @return 是否存在
     */
    @Transactional(readOnly = true)
    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }
}