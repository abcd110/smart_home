package com.smarthome.security.repository;

import com.smarthome.security.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 角色仓库接口
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * 根据角色名称查找角色
     * 
     * @param name 角色名称
     * @return 角色
     */
    Optional<Role> findByName(String name);
    
    /**
     * 检查角色名称是否存在
     * 
     * @param name 角色名称
     * @return 是否存在
     */
    boolean existsByName(String name);
    
    /**
     * 查找所有启用的角色
     * 
     * @return 启用的角色列表
     */
    List<Role> findByEnabledTrue();
    
    /**
     * 根据权限编码查找角色
     * 
     * @param permissionCode 权限编码
     * @return 角色列表
     */
    @Query("SELECT DISTINCT r FROM Role r JOIN r.permissions p WHERE p.code = :permissionCode AND r.enabled = true")
    List<Role> findByPermissionCode(@Param("permissionCode") String permissionCode);
    
    /**
     * 根据用户ID查找角色
     * 
     * @param userId 用户ID
     * @return 角色列表
     */
    @Query("SELECT DISTINCT r FROM Role r JOIN r.users u WHERE u.id = :userId AND r.enabled = true")
    List<Role> findByUserId(@Param("userId") Long userId);
}