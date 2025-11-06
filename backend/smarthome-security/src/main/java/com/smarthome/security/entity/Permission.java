package com.smarthome.security.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.Objects;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 权限实体类
 */
@Entity
@Table(name = "permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {
    
    /**
     * 权限ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 权限名称
     */
    @NotBlank(message = "权限名称不能为空")
    @Size(min = 2, max = 100, message = "权限名称长度必须在2-100个字符之间")
    @Column(unique = true, nullable = false, length = 100)
    private String name;
    
    /**
     * 权限编码
     */
    @NotBlank(message = "权限编码不能为空")
    @Size(min = 2, max = 100, message = "权限编码长度必须在2-100个字符之间")
    @Column(unique = true, nullable = false, length = 100)
    private String code;
    
    /**
     * 权限描述
     */
    @Size(max = 200, message = "权限描述长度不能超过200个字符")
    private String description;
    
    /**
     * 权限类型
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer type = 1; // 1:菜单 2:按钮 3:接口
    
    /**
     * 父权限ID
     */
    private Long parentId;
    
    /**
     * 权限路径
     */
    @Size(max = 255, message = "权限路径长度不能超过255个字符")
    private String path;
    
    /**
     * 权限图标
     */
    @Size(max = 100, message = "权限图标长度不能超过100个字符")
    private String icon;
    
    /**
     * 排序号
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;
    
    /**
     * 权限状态
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
     * 权限与角色多对多关系
     */
    @ManyToMany(mappedBy = "permissions")
    private Set<Role> roles;
    
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
     * 重写equals方法，避免循环引用
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission permission = (Permission) o;
        return Objects.equals(id, permission.id);
    }
    
    /**
     * 重写hashCode方法，避免循环引用
     * 使用固定值31，与User和Role实体类保持一致，完全避免任何形式的循环引用
     */
    @Override
    public int hashCode() {
        // 使用一个固定值作为哈希码，完全避免任何形式的循环引用
        // 这是最安全的方式来解决Hibernate PersistentSet的循环引用问题
        return 31;
    }
}