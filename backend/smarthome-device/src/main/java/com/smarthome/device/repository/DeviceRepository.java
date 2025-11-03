package com.smarthome.device.repository;

import com.smarthome.device.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 设备数据访问接口
 */
@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    
    /**
     * 根据设备ID查找设备
     */
    Optional<Device> findByDeviceId(String deviceId);
    
    /**
     * 根据用户ID查找设备列表
     */
    List<Device> findByUserId(Long userId);
    
    /**
     * 根据用户ID和设备类型查找设备列表
     */
    List<Device> findByUserIdAndType(Long userId, String type);
    
    /**
     * 根据设备ID检查设备是否存在
     */
    boolean existsByDeviceId(String deviceId);
    
    /**
     * 查找所有在线的设备
     */
    List<Device> findByOnlineTrue();
    
    /**
     * 查找所有启用的设备
     */
    List<Device> findByEnabledTrue();
    
    /**
     * 根据设备名称模糊搜索
     */
    List<Device> findByNameContaining(String name);
    
    /**
     * 根据用户ID和设备名称模糊搜索
     */
    @Query("SELECT d FROM Device d WHERE d.userId = :userId AND d.name LIKE %:name%")
    List<Device> findByUserIdAndNameContaining(@Param("userId") Long userId, @Param("name") String name);
    
    /**
     * 统计用户设备数量
     */
    @Query("SELECT COUNT(d) FROM Device d WHERE d.userId = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    /**
     * 统计在线设备数量
     */
    @Query("SELECT COUNT(d) FROM Device d WHERE d.online = true")
    long countOnlineDevices();
}