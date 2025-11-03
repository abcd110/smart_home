package com.smarthome.sensor.repository;

import com.smarthome.sensor.entity.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 传感器数据访问接口
 */
@Repository
public interface SensorRepository extends JpaRepository<Sensor, Long> {
    
    /**
     * 根据传感器ID查找传感器
     */
    Optional<Sensor> findBySensorId(String sensorId);
    
    /**
     * 根据设备ID查找传感器列表
     */
    List<Sensor> findByDeviceId(Long deviceId);
    
    /**
     * 根据设备ID和传感器类型查找传感器列表
     */
    List<Sensor> findByDeviceIdAndType(Long deviceId, String type);
    
    /**
     * 根据传感器ID检查传感器是否存在
     */
    boolean existsBySensorId(String sensorId);
    
    /**
     * 查找所有启用的传感器
     */
    List<Sensor> findByEnabledTrue();
    
    /**
     * 根据传感器名称模糊搜索
     */
    List<Sensor> findByNameContaining(String name);
    
    /**
     * 根据设备ID和传感器名称模糊搜索
     */
    @Query("SELECT s FROM Sensor s WHERE s.deviceId = :deviceId AND s.name LIKE %:name%")
    List<Sensor> findByDeviceIdAndNameContaining(@Param("deviceId") Long deviceId, @Param("name") String name);
    
    /**
     * 统计设备传感器数量
     */
    @Query("SELECT COUNT(s) FROM Sensor s WHERE s.deviceId = :deviceId")
    long countByDeviceId(@Param("deviceId") Long deviceId);
    
    /**
     * 根据设备ID和类型统计传感器数量
     */
    @Query("SELECT COUNT(s) FROM Sensor s WHERE s.deviceId = :deviceId AND s.type = :type")
    long countByDeviceIdAndType(@Param("deviceId") Long deviceId, @Param("type") String type);
}