package com.smarthome.user.service;

import com.smarthome.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 根据ID获取用户
     */
    User getUserById(Long id);
    
    /**
     * 根据用户名获取用户
     */
    User getUserByUsername(String username);
    
    /**
     * 创建用户
     */
    User createUser(User user);
    
    /**
     * 更新用户信息
     */
    User updateUser(Long id, User user);
    
    /**
     * 删除用户
     */
    void deleteUser(Long id);
    
    /**
     * 获取所有用户
     */
    List<User> getAllUsers();
    
    /**
     * 分页获取用户
     */
    Page<User> getUsersByPage(Pageable pageable);
    
    /**
     * 根据用户名搜索用户
     */
    List<User> searchUsersByUsername(String username);
    
    /**
     * 启用/禁用用户
     */
    void toggleUserStatus(Long id, boolean enabled);
    
    /**
     * 修改用户密码
     */
    void changePassword(Long id, String newPassword);
    
    /**
     * 检查用户名是否已存在
     */
    boolean isUsernameExists(String username);
    
    /**
     * 检查邮箱是否已存在
     */
    boolean isEmailExists(String email);
    
    /**
     * 检查手机号是否已存在
     */
    boolean isPhoneExists(String phone);
}