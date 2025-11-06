package com.smarthome.security.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 用户实体类
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@ToString(exclude = {"roles"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    /**
     * 用户ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Column(unique = true, nullable = false, length = 50)
    private String username;
    
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
    @Column(nullable = false)
    private String password;
    
    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Column(unique = true, nullable = false, length = 100)
    private String email;
    
    /**
     * 手机号
     */
    @Size(max = 20, message = "手机号长度不能超过20个字符")
    private String phone;
    
    /**
     * 真实姓名
     */
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;
    
    /**
     * 头像URL
     */
    @Size(max = 255, message = "头像URL长度不能超过255个字符")
    private String avatar;
    
    /**
     * 用户状态
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;
    
    /**
     * 账户是否未锁定
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean accountNonLocked = true;
    
    /**
     * 账户是否未过期
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean accountNonExpired = true;
    
    /**
     * 凭证是否未过期
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean credentialsNonExpired = true;
    
    /**
     * 创建时间
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;
    
    /**
     * 用户角色
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Set<Role> roles;
    
    /**
     * 密码重置令牌
     */
    @Size(max = 255, message = "密码重置令牌长度不能超过255个字符")
    private String resetToken;
    
    /**
     * 密码重置令牌过期时间
     */
    private LocalDateTime resetTokenExpiry;
    
    /**
     * 邮箱验证状态
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;
    
    /**
     * 邮箱验证令牌
     */
    @Size(max = 255, message = "邮箱验证令牌长度不能超过255个字符")
    private String emailVerificationToken;
    
    /**
     * 邮箱验证令牌过期时间
     */
    private LocalDateTime emailVerificationTokenExpiry;
    
    /**
     * 登录失败次数
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer loginAttempts = 0;
    
    /**
     * 账户锁定时间
     */
    private LocalDateTime lockTime;
    
    /**
     * 失败登录尝试次数
     */
    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private Integer failedLoginAttempts = 0;
    
    /**
     * 自定义equals方法，避免Hibernate PersistentSet循环引用
     * 仅基于主键id判断，不使用任何关联集合
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        // 仅使用id字段，完全避免引用roles集合
        return id != null ? id.equals(user.id) : false;
    }

    /**
     * 自定义hashCode方法，完全避免任何形式的循环引用
     * 使用非常简单的实现，不依赖于任何字段或关联
     */
    @Override
    public int hashCode() {
        // 使用一个固定值作为哈希码，完全避免任何形式的循环引用
        // 这是最安全的方式来解决Hibernate PersistentSet的循环引用问题
        return 31;
    }
    
    /**
     * 获取账户是否未锁定
     * 
     * @return 账户是否未锁定
     */
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }
    
    /**
     * 获取账户是否未过期
     * 
     * @return 账户是否未过期
     */
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }
    
    /**
     * 获取凭证是否未过期
     * 
     * @return 凭证是否未过期
     */
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }
    
    /**
     * 获取用户是否启用
     * 
     * @return 用户是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * 获取登录失败次数
     * 
     * @return 登录失败次数
     */
    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }
    
    /**
     * 设置登录失败次数
     * 
     * @param failedLoginAttempts 登录失败次数
     */
    public void setFailedLoginAttempts(Integer failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }
    
    /**
     * JPA生命周期回调 - 创建前
     */
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }
    
    /**
     * JPA生命周期回调 - 更新前
     */
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
    

}