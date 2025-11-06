package com.smarthome.security.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 角色实体类
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@ToString(exclude = {"users", "permissions"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    
    /**
     * 角色ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    @Size(min = 2, max = 50, message = "角色名称长度必须在2-50个字符之间")
    @Column(unique = true, nullable = false, length = 50)
    private String name;
    
    /**
     * 角色描述
     */
    @Size(max = 200, message = "角色描述长度不能超过200个字符")
    private String description;
    
    /**
     * 角色状态
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;
    
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
     * 角色权限
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;
    
    /**
     * 角色用户
     */
    @ManyToMany(mappedBy = "roles")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Set<User> users;
    
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
    
    /**
     * 自定义equals方法，避免Hibernate PersistentSet循环引用
     * 仅基于主键id判断，不使用任何关联集合
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        // 仅使用id字段，完全避免引用users集合
        return id != null ? id.equals(role.id) : false;
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

}