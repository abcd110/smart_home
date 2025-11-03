package com.smarthome.user.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String username;
    
    @Column(nullable = false, length = 100)
    private String password;
    
    @Column(unique = true, nullable = false, length = 100)
    private String email;
    
    @Column(unique = true, nullable = false, length = 20)
    private String phone;
    
    @Column(length = 50)
    private String nickname;
    
    @Column(length = 200)
    private String avatar;
    
    @Column(nullable = false)
    private Boolean enabled = true;
    
    @Column(nullable = false)
    private Boolean accountNonExpired = true;
    
    @Column(nullable = false)
    private Boolean credentialsNonExpired = true;
    
    @Column(nullable = false)
    private Boolean accountNonLocked = true;
    
    @Column(nullable = false)
    private LocalDateTime createTime;
    
    @Column(nullable = false)
    private LocalDateTime updateTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}