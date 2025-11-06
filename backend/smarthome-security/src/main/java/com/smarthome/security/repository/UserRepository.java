package com.smarthome.security.repository;

import com.smarthome.security.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 用户仓库接口
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根据用户名查找用户
     * 
     * @param username 用户名
     * @return 用户
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     * 
     * @param email 邮箱
     * @return 用户
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 根据手机号查找用户
     * 
     * @param phone 手机号
     * @return 用户
     */
    Optional<User> findByPhone(String phone);
    
    /**
     * 根据用户名或邮箱查找用户
     * 
     * @param username 用户名
     * @param email 邮箱
     * @return 用户
     */
    Optional<User> findByUsernameOrEmail(String username, String email);
    
    /**
     * 检查用户名是否存在
     * 
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否存在
     * 
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 检查手机号是否存在
     * 
     * @param phone 手机号
     * @return 是否存在
     */
    boolean existsByPhone(String phone);
    
    /**
     * 根据密码重置令牌查找用户
     * 
     * @param resetToken 密码重置令牌
     * @return 用户
     */
    Optional<User> findByResetToken(String resetToken);
    
    /**
     * 根据邮箱验证令牌查找用户
     * 
     * @param emailVerificationToken 邮箱验证令牌
     * @return 用户
     */
    Optional<User> findByEmailVerificationToken(String emailVerificationToken);
    
    /**
     * 查找所有被锁定的用户
     * 
     * @param currentTime 当前时间
     * @return 被锁定的用户列表
     */
    @Query("SELECT u FROM User u WHERE u.accountNonLocked = false AND u.lockTime IS NOT NULL AND u.lockTime <= :currentTime")
    java.util.List<User> findLockedUsersWithExpiredLockTime(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * 根据角色名称查找用户
     * 
     * @param roleName 角色名称
     * @return 用户列表
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    java.util.List<User> findByRoleName(@Param("roleName") String roleName);
    
    /**
     * 查找邮箱未验证的用户
     * 
     * @param currentTime 当前时间
     * @return 邮箱未验证的用户列表
     */
    @Query("SELECT u FROM User u WHERE u.emailVerified = false AND u.emailVerificationTokenExpiry IS NOT NULL AND u.emailVerificationTokenExpiry <= :currentTime")
    java.util.List<User> findUsersWithExpiredEmailVerificationTokens(@Param("currentTime") LocalDateTime currentTime);
}