# 智能家居系统配置文档

## 系统概述
智能家居管理系统采用前后端分离架构，前端基于React + TypeScript，后端使用Supabase，并通过MQTT中间件实现设备通信。

## 系统组件

### 1. Web前端 (web-simulator)
- **技术栈**: React 18 + TypeScript + Vite + Ant Design
- **端口**: 5173 (开发模式)
- **状态**: ✅ 运行正常，TypeScript构建成功
- **功能模块**:
  - Dashboard 仪表板
  - DeviceManager 设备管理
  - RealTimeData 实时数据监控

### 2. MQTT中间件 (mqtt-supabase-middleware)
- **技术栈**: Node.js + Express + MQTT.js
- **端口**: 3000
- **状态**: ✅ 运行正常
- **功能**:
  - MQTT消息转发和处理
  - Supabase数据同步
  - RESTful API服务
  - 设备状态管理

### 3. 数据库 (Supabase)
- **URL**: https://znarfgnwmbsawgndeuzh.supabase.co
- **Publishable Key**: sb_publishable_MMGYn93wCO4nsFuAWIzWNw_IaFHMO4W
- **状态**: ✅ 连接正常

## 当前状态

### 已完成修复项目
1. **TypeScript类型错误修复**
   - ✅ 修复Stats接口，添加total_alerts和energy_consumption属性
   - ✅ 修复DeviceStore接口，添加addDevice和toggleDeviceStatus方法定义
   - ✅ 为函数参数添加明确的类型声明
   - ✅ 构建成功，无TypeScript错误

2. **MQTT消息集成**
   - ✅ MQTT中间件服务启动成功
   - ✅ Supabase连接正常
   - ✅ MQTT连接成功，已订阅主题: smarthome/+/in/# 和 smarthome/+/in/status
   - ✅ HTTP服务正常运行

### 服务端点
- 健康检查: http://localhost:3000/health
- 状态查询: http://localhost:3000/status
- 统计数据: http://localhost:3000/stats
- 设备列表: http://localhost:3000/devices

## 关键配置信息

### Supabase配置
```
URL: https://znarfgnwmbsawgndeuzh.supabase.co
Publishable Key: sb_publishable_MMGYn93wCO4nsFuAWIzWNw_IaFHMO4W
```

### MQTT配置
```
Broker: mqtts://z01b0909.ala.asia-southeast1.emqxsl.com:8883
Username: APP
Client ID: middleware_[timestamp]
Topics:
  - smarthome/+/in/# (所有设备输入)
  - smarthome/+/in/status (设备状态)
```

### 统计数据结构 (Stats)
```typescript
interface Stats {
  devices: {
    total: number;
    online: number;
    offline: number;
  };
  messages: {
    total: number;
    last_hour: number;
  };
  sensor_data: {
    total: number;
    last_hour: number;
  };
  total_alerts: number; // 新增
  energy_consumption: { // 新增
    today: number;
    month: number;
  };
}
```

## 下一步计划
1. 测试MQTT消息发送和接收功能
2. 验证设备状态同步功能
3. 测试前端与中间件的API集成
4. 完善实时数据监控功能

---
*文档生成时间: 2025-11-11 16:29*
*系统版本: v1.0.0*