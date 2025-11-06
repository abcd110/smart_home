package com.smarthome.security.service;

import com.smarthome.security.entity.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

/**
 * 用户服务接口
 * 
 * @author SmartHome Team
 * @since 1.0.0
 */
public interface UserService {
    
    /**
     * 根据用户名或邮箱查找用户
     * 
     * @param usernameOrEmail 用户名或邮箱
     * @return 用户
     */
    Optional<User> findByUsernameOrEmail(String usernameOrEmail);
    
    /**
     * 创建用户详情对象
     * 
     * @param user 用户实体
     * @return 用户详情
     */
    UserDetails createUserDetails(User user);
}