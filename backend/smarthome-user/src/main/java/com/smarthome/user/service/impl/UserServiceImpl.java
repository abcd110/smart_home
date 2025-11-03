package com.smarthome.user.service.impl;

import com.smarthome.user.entity.User;
import com.smarthome.user.repository.UserRepository;
import com.smarthome.user.service.UserService;
import com.smarthome.security.util.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User getUserById(Long id) {
        log.info("根据ID获取用户: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @Override
    public User getUserByUsername(String username) {
        log.info("根据用户名获取用户: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @Override
    public User createUser(User user) {
        log.info("创建用户: {}", user.getUsername());
        
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }
        
        // 检查手机号是否已存在
        if (userRepository.existsByPhone(user.getPhone())) {
            throw new RuntimeException("手机号已存在");
        }
        
        // 加密密码
        String encryptedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);
        
        return userRepository.save(user);
    }

    @Override
    public User updateUser(Long id, User user) {
        log.info("更新用户信息: {}", id);
        
        User existingUser = getUserById(id);
        
        // 更新基本信息
        existingUser.setNickname(user.getNickname());
        existingUser.setAvatar(user.getAvatar());
        
        // 如果提供了新邮箱，检查是否重复
        if (user.getEmail() != null && !user.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new RuntimeException("邮箱已存在");
            }
            existingUser.setEmail(user.getEmail());
        }
        
        // 如果提供了新手机号，检查是否重复
        if (user.getPhone() != null && !user.getPhone().equals(existingUser.getPhone())) {
            if (userRepository.existsByPhone(user.getPhone())) {
                throw new RuntimeException("手机号已存在");
            }
            existingUser.setPhone(user.getPhone());
        }
        
        return userRepository.save(existingUser);
    }

    @Override
    public void deleteUser(Long id) {
        log.info("删除用户: {}", id);
        User user = getUserById(id);
        userRepository.delete(user);
    }

    @Override
    public List<User> getAllUsers() {
        log.info("获取所有用户");
        return userRepository.findAll();
    }

    @Override
    public Page<User> getUsersByPage(Pageable pageable) {
        log.info("分页获取用户");
        return userRepository.findAll(pageable);
    }

    @Override
    public List<User> searchUsersByUsername(String username) {
        log.info("根据用户名搜索用户: {}", username);
        return userRepository.findByUsernameContaining(username);
    }

    @Override
    public void toggleUserStatus(Long id, boolean enabled) {
        log.info("{}用户: {}", enabled ? "启用" : "禁用", id);
        User user = getUserById(id);
        user.setEnabled(enabled);
        userRepository.save(user);
    }

    @Override
    public void changePassword(Long id, String newPassword) {
        log.info("修改用户密码: {}", id);
        User user = getUserById(id);
        String encryptedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encryptedPassword);
        userRepository.save(user);
    }

    @Override
    public boolean isUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean isPhoneExists(String phone) {
        return userRepository.existsByPhone(phone);
    }
}