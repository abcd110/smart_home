package com.smarthome.security.repository;

import com.smarthome.security.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 权限仓库接口
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    
    /**
     * 根据权限编码查找权限
     * 
     * @param code 权限编码
     * @return 权限
     */
    Optional<Permission> findByCode(String code);
    
    /**
     * 根据权限名称查找权限
     * 
     * @param name 权限名称
     * @return 权限
     */
    Optional<Permission> findByName(String name);
    
    /**
     * 检查权限编码是否存在
     * 
     * @param code 权限编码
     * @return 是否存在
     */
    boolean existsByCode(String code);
    
    /**
     * 检查权限名称是否存在
     * 
     * @param name 权限名称
     * @return 是否存在
     */
    boolean existsByName(String name);
    
    /**
     * 根据权限类型查找权限
     * 
     * @param type 权限类型
     * @return 权限列表
     */
    List<Permission> findByType(Integer type);
    
    /**
     * 根据父权限ID查找子权限
     * 
     * @param parentId 父权限ID
     * @return 子权限列表
     */
    List<Permission> findByParentId(Long parentId);
    
    /**
     * 查找所有顶级权限（父权限ID为null）
     * 
     * @return 顶级权限列表
     */
    List<Permission> findByParentIdIsNull();
    
    /**
     * 根据用户ID查找权限
     * 
     * @param userId 用户ID
     * @return 权限列表
     */
    @Query("SELECT DISTINCT p FROM Permission p JOIN p.roles r JOIN r.users u WHERE u.id = :userId")
    List<Permission> findByUserId(@Param("userId") Long userId);
    
    /**
     * 根据角色ID查找权限
     * 
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Query("SELECT DISTINCT p FROM Permission p JOIN p.roles r WHERE r.id = :roleId")
    List<Permission> findByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 根据用户ID和权限类型查找权限
     * 
     * @param userId 用户ID
     * @param type 权限类型
     * @return 权限列表
     */
    @Query("SELECT DISTINCT p FROM Permission p JOIN p.roles r JOIN r.users u WHERE u.id = :userId AND p.type = :type")
    List<Permission> findByUserIdAndType(@Param("userId") Long userId, @Param("type") Integer type);
    
    /**
     * 根据用户ID查找所有权限编码
     * 
     * @param userId 用户ID
     * @return 权限编码列表
     */
    @Query("SELECT DISTINCT p.code FROM Permission p JOIN p.roles r JOIN r.users u WHERE u.id = :userId")
    List<String> findPermissionCodesByUserId(@Param("userId") Long userId);
}