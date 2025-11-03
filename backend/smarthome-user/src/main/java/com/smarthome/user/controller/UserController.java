package com.smarthome.user.controller;

import com.smarthome.common.model.ApiResponse;
import com.smarthome.user.entity.User;
import com.smarthome.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 根据ID获取用户
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        log.info("获取用户信息: {}", id);
        User user = userService.getUserById(id);
        ApiResponse<User> response = ApiResponse.success(user);
        response.setMessage("获取用户信息成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 根据用户名获取用户
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<User>> getUserByUsername(@PathVariable String username) {
        log.info("根据用户名获取用户: {}", username);
        User user = userService.getUserByUsername(username);
        ApiResponse<User> response = ApiResponse.success(user);
        response.setMessage("根据用户名获取用户成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 创建用户
     */
    @PostMapping
    public ResponseEntity<ApiResponse<User>> createUser(@RequestBody User user) {
        log.info("创建用户: {}", user.getUsername());
        User createdUser = userService.createUser(user);
        ApiResponse<User> response = ApiResponse.success(createdUser);
        response.setMessage("创建用户成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable Long id, @RequestBody User user) {
        log.info("更新用户信息: {}", id);
        User updatedUser = userService.updateUser(id, user);
        ApiResponse<User> response = ApiResponse.success(updatedUser);
        response.setMessage("更新用户信息成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        log.info("删除用户: {}", id);
        userService.deleteUser(id);
        ApiResponse<Void> response = ApiResponse.success(null);
        response.setMessage("删除用户成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 获取所有用户
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        log.info("获取所有用户");
        List<User> users = userService.getAllUsers();
        ApiResponse<List<User>> response = ApiResponse.success(users);
        response.setMessage("获取所有用户成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 分页获取用户
     */
    @GetMapping("/page")
    public ResponseEntity<ApiResponse<Page<User>>> getUsersByPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("分页获取用户 - 页码: {}, 大小: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userService.getUsersByPage(pageable);
        ApiResponse<Page<User>> response = ApiResponse.success(users);
        response.setMessage("分页获取用户成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 根据用户名搜索用户
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<User>>> searchUsersByUsername(@RequestParam String username) {
        log.info("搜索用户: {}", username);
        List<User> users = userService.searchUsersByUsername(username);
        ApiResponse<List<User>> response = ApiResponse.success(users);
        response.setMessage("搜索用户成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 启用/禁用用户
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> toggleUserStatus(@PathVariable Long id, @RequestParam boolean enabled) {
        log.info("{}用户: {}", enabled ? "启用" : "禁用", id);
        userService.toggleUserStatus(id, enabled);
        ApiResponse<Void> response = ApiResponse.success(null);
        response.setMessage(enabled ? "启用用户成功" : "禁用用户成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 修改用户密码
     */
    @PatchMapping("/{id}/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@PathVariable Long id, @RequestParam String newPassword) {
        log.info("修改用户密码: {}", id);
        userService.changePassword(id, newPassword);
        ApiResponse<Void> response = ApiResponse.success(null);
        response.setMessage("修改用户密码成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 检查用户名是否已存在
     */
    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse<Boolean>> checkUsernameExists(@RequestParam String username) {
        log.info("检查用户名是否存在: {}", username);
        boolean exists = userService.isUsernameExists(username);
        ApiResponse<Boolean> response = ApiResponse.success(exists);
        response.setMessage("检查用户名完成");
        return ResponseEntity.ok(response);
    }

    /**
     * 检查邮箱是否已存在
     */
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailExists(@RequestParam String email) {
        log.info("检查邮箱是否存在: {}", email);
        boolean exists = userService.isEmailExists(email);
        ApiResponse<Boolean> response = ApiResponse.success(exists);
        response.setMessage("检查邮箱完成");
        return ResponseEntity.ok(response);
    }

    /**
     * 检查手机号是否已存在
     */
    @GetMapping("/check-phone")
    public ResponseEntity<ApiResponse<Boolean>> checkPhoneExists(@RequestParam String phone) {
        log.info("检查手机号是否存在: {}", phone);
        boolean exists = userService.isPhoneExists(phone);
        ApiResponse<Boolean> response = ApiResponse.success(exists);
        response.setMessage("检查手机号完成");
        return ResponseEntity.ok(response);
    }
}