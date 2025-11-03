package com.smarthome.gateway.controller;

import com.smarthome.common.model.ApiResponse;
import com.smarthome.gateway.entity.GatewayDevice;
import com.smarthome.gateway.service.GatewayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 网关控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/gateways")
public class GatewayController {
    
    @Autowired
    private GatewayService gatewayService;
    
    /**
     * 获取所有网关设备
     */
    @GetMapping
    public ApiResponse<List<GatewayDevice>> getAllGateways() {
        try {
            List<GatewayDevice> gateways = gatewayService.getAllGateways();
            ApiResponse<List<GatewayDevice>> response = ApiResponse.success(gateways);
            response.setMessage("获取网关设备列表成功");
            return response;
        } catch (Exception e) {
            log.error("获取网关设备列表异常: {}", e.getMessage());
            return ApiResponse.error("获取网关设备列表异常: " + e.getMessage());
        }
    }
    
    /**
     * 根据ID获取网关设备
     */
    @GetMapping("/{id}")
    public ApiResponse<GatewayDevice> getGatewayById(@PathVariable Long id) {
        try {
            GatewayDevice gateway = gatewayService.getGatewayById(id);
            if (gateway != null) {
                ApiResponse<GatewayDevice> response = ApiResponse.success(gateway);
                response.setMessage("获取网关设备成功");
                return response;
            } else {
                return ApiResponse.error("网关设备不存在");
            }
        } catch (Exception e) {
            log.error("获取网关设备异常: {}", e.getMessage());
            return ApiResponse.error("获取网关设备异常: " + e.getMessage());
        }
    }
    
    /**
     * 根据网关ID获取网关设备
     */
    @GetMapping("/gateway-id/{gatewayId}")
    public ApiResponse<GatewayDevice> getGatewayByGatewayId(@PathVariable String gatewayId) {
        try {
            GatewayDevice gateway = gatewayService.getGatewayByGatewayId(gatewayId);
            if (gateway != null) {
                ApiResponse<GatewayDevice> response = ApiResponse.success(gateway);
                response.setMessage("获取网关设备成功");
                return response;
            } else {
                return ApiResponse.error("网关设备不存在");
            }
        } catch (Exception e) {
            log.error("获取网关设备异常: {}", e.getMessage());
            return ApiResponse.error("获取网关设备异常: " + e.getMessage());
        }
    }
    
    /**
     * 创建网关设备
     */
    @PostMapping
    public ApiResponse<GatewayDevice> createGateway(@RequestBody GatewayDevice gateway) {
        try {
            GatewayDevice createdGateway = gatewayService.createGateway(gateway);
            ApiResponse<GatewayDevice> response = ApiResponse.success(createdGateway);
            response.setMessage("网关设备创建成功");
            return response;
        } catch (Exception e) {
            log.error("创建网关设备异常: {}", e.getMessage());
            return ApiResponse.error("创建网关设备异常: " + e.getMessage());
        }
    }
    
    /**
     * 更新网关设备
     */
    @PutMapping("/{id}")
    public ApiResponse<GatewayDevice> updateGateway(@PathVariable Long id, @RequestBody GatewayDevice gateway) {
        try {
            GatewayDevice updatedGateway = gatewayService.updateGateway(id, gateway);
            if (updatedGateway != null) {
                ApiResponse<GatewayDevice> response = ApiResponse.success(updatedGateway);
                response.setMessage("更新网关设备成功");
                return response;
            } else {
                return ApiResponse.error("更新网关设备失败，网关设备不存在或网关ID/IP地址已存在");
            }
        } catch (Exception e) {
            log.error("更新网关设备异常: {}", e.getMessage());
            return ApiResponse.error("更新网关设备异常: " + e.getMessage());
        }
    }
    
    /**
     * 删除网关设备
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> deleteGateway(@PathVariable Long id) {
        try {
            boolean success = gatewayService.deleteGateway(id);
            if (success) {
                ApiResponse<Boolean> response = ApiResponse.success(true);
                response.setMessage("删除网关设备成功");
                return response;
            } else {
                return ApiResponse.error("删除网关设备失败，网关设备不存在");
            }
        } catch (Exception e) {
            log.error("删除网关设备异常: {}", e.getMessage());
            return ApiResponse.error("删除网关设备异常: " + e.getMessage());
        }
    }
    
    /**
     * 更新网关在线状态
     */
    @PostMapping("/{gatewayId}/online-status")
    public ApiResponse<Boolean> updateOnlineStatus(@PathVariable String gatewayId, @RequestBody OnlineStatusRequest request) {
        try {
            boolean success = gatewayService.updateOnlineStatus(gatewayId, request.isOnline());
            if (success) {
                ApiResponse<Boolean> response = ApiResponse.success(true);
                response.setMessage("更新网关在线状态成功");
                return response;
            } else {
                return ApiResponse.error("更新网关在线状态失败，网关设备不存在");
            }
        } catch (Exception e) {
            log.error("更新网关在线状态异常: {}", e.getMessage());
            return ApiResponse.error("更新网关在线状态异常: " + e.getMessage());
        }
    }
    
    /**
     * 更新网关心跳时间
     */
    @PostMapping("/{gatewayId}/heartbeat")
    public ApiResponse<Boolean> updateHeartbeat(@PathVariable String gatewayId) {
        try {
            boolean success = gatewayService.updateHeartbeat(gatewayId);
            if (success) {
                ApiResponse<Boolean> response = ApiResponse.success(true);
                response.setMessage("更新网关心跳时间成功");
                return response;
            } else {
                return ApiResponse.error("更新网关心跳时间失败，网关设备不存在");
            }
        } catch (Exception e) {
            log.error("更新网关心跳时间异常: {}", e.getMessage());
            return ApiResponse.error("更新网关心跳时间异常: " + e.getMessage());
        }
    }
    
    /**
     * 根据名称搜索网关设备
     */
    @GetMapping("/search")
    public ApiResponse<List<GatewayDevice>> searchGatewaysByName(@RequestParam String name) {
        try {
            List<GatewayDevice> gateways = gatewayService.searchGatewaysByName(name);
            ApiResponse<List<GatewayDevice>> response = ApiResponse.success(gateways);
            response.setMessage("搜索网关设备成功");
            return response;
        } catch (Exception e) {
            log.error("搜索网关设备异常: {}", e.getMessage());
            return ApiResponse.error("搜索网关设备异常: " + e.getMessage());
        }
    }
    
    /**
     * 根据类型获取网关设备
     */
    @GetMapping("/type/{type}")
    public ApiResponse<List<GatewayDevice>> getGatewaysByType(@PathVariable String type) {
        try {
            List<GatewayDevice> gateways = gatewayService.getGatewaysByType(type);
            ApiResponse<List<GatewayDevice>> response = ApiResponse.success(gateways);
            response.setMessage("获取类型网关设备成功");
            return response;
        } catch (Exception e) {
            log.error("获取类型网关设备异常: {}", e.getMessage());
            return ApiResponse.error("获取类型网关设备异常: " + e.getMessage());
        }
    }
    
    /**
     * 根据在线状态获取网关设备
     */
    @GetMapping("/status/{online}")
    public ApiResponse<List<GatewayDevice>> getGatewaysByOnlineStatus(@PathVariable Boolean online) {
        try {
            List<GatewayDevice> gateways = gatewayService.getGatewaysByOnlineStatus(online);
            ApiResponse<List<GatewayDevice>> response = ApiResponse.success(gateways);
            response.setMessage("获取在线状态网关设备成功");
            return response;
        } catch (Exception e) {
            log.error("获取在线状态网关设备异常: {}", e.getMessage());
            return ApiResponse.error("获取在线状态网关设备异常: " + e.getMessage());
        }
    }
    
    /**
     * 获取在线网关设备数量
     */
    @GetMapping("/count/online")
    public ApiResponse<Long> getOnlineGatewayCount() {
        try {
            long count = gatewayService.getOnlineGatewayCount();
            ApiResponse<Long> response = ApiResponse.success(count);
            response.setMessage("获取在线网关设备数量成功");
            return response;
        } catch (Exception e) {
            log.error("获取在线网关设备数量异常: {}", e.getMessage());
            return ApiResponse.error("获取在线网关设备数量异常: " + e.getMessage());
        }
    }
    
    /**
     * 获取离线网关设备数量
     */
    @GetMapping("/count/offline")
    public ApiResponse<Long> getOfflineGatewayCount() {
        try {
            long count = gatewayService.getOfflineGatewayCount();
            ApiResponse<Long> response = ApiResponse.success(count);
            response.setMessage("获取离线网关设备数量成功");
            return response;
        } catch (Exception e) {
            log.error("获取离线网关设备数量异常: {}", e.getMessage());
            return ApiResponse.error("获取离线网关设备数量异常: " + e.getMessage());
        }
    }
    
    /**
     * 启用网关设备
     */
    @PostMapping("/{id}/enable")
    public ApiResponse<Boolean> enableGateway(@PathVariable Long id) {
        try {
            boolean success = gatewayService.enableGateway(id);
            if (success) {
                ApiResponse<Boolean> response = ApiResponse.success(true);
                response.setMessage("启用网关设备成功");
                return response;
            } else {
                return ApiResponse.error("启用网关设备失败，网关设备不存在");
            }
        } catch (Exception e) {
            log.error("启用网关设备异常: {}", e.getMessage());
            return ApiResponse.error("启用网关设备异常: " + e.getMessage());
        }
    }
    
    /**
     * 禁用网关设备
     */
    @PostMapping("/{id}/disable")
    public ApiResponse<Boolean> disableGateway(@PathVariable Long id) {
        try {
            boolean success = gatewayService.disableGateway(id);
            if (success) {
                ApiResponse<Boolean> response = ApiResponse.success(true);
                response.setMessage("禁用网关设备成功");
                return response;
            } else {
                return ApiResponse.error("禁用网关设备失败，网关设备不存在");
            }
        } catch (Exception e) {
            log.error("禁用网关设备异常: {}", e.getMessage());
            return ApiResponse.error("禁用网关设备异常: " + e.getMessage());
        }
    }
    
    // 请求参数类
    public static class OnlineStatusRequest {
        private boolean online;
        
        // getter和setter
        public boolean isOnline() { return online; }
        public void setOnline(boolean online) { this.online = online; }
    }
}