# 智能家居系统

一个完整的智能家居解决方案，包含React前端和Node.js后端，基于Supabase数据库和MQTT通信，提供设备管理、实时监控和数据可视化等功能。

## 项目架构

```
Smarthome/
├── app/                          # Android移动应用 (开发中)
├── web-simulator/                # React前端界面
├── mqtt-supabase-middleware/     # Node.js + Express后端
├── nginx/                        # Nginx反向代理配置
└── docs/                         # 项目文档
```

## 技术栈

### 前端
- React 18
- TypeScript
- Ant Design (UI组件库)
- Vite (构建工具)
- Axios (HTTP客户端)

### 后端
- Node.js + Express
- MQTT (消息通信)
- Supabase (PostgreSQL数据库)
- RESTful API

### 通信与数据
- MQTT Broker (Eclipse Mosquitto)
- Supabase (实时数据库)
- REST API

## 项目特色

- 🟢 设备状态实时显示
- 📱 现代化响应式界面
- 🔄 设备状态切换
- 📊 实时数据刷新
- 🎨 直观的状态图标和颜色编码
- ⚡ 高性能的Web应用

## 快速开始

### 环境要求
- Node.js 18+
- Git
- Supabase账户
- MQTT Broker

### 安装与运行

1. **克隆项目**
```bash
git clone <repository-url>
cd Smarthome
```

2. **启动后端服务**
```bash
cd mqtt-supabase-middleware
npm install
node index.js
```

3. **启动前端服务**
```bash
cd web-simulator
npm install
npm run dev
```

### 访问应用
- 前端界面: http://localhost:5173
- 后端API: http://localhost:3000
- 健康检查: http://localhost:3000/health

## API接口

### 设备管理
- `GET /devices` - 获取设备列表
- `POST /devices` - 创建设备
- `PUT /devices/:deviceId` - 更新设备信息
- `DELETE /devices/:deviceId` - 删除设备

### 系统监控
- `GET /health` - 系统健康检查
- `GET /status` - 系统状态
- `GET /stats` - 统计数据

## 功能特性

### 设备管理
- ✅ 设备列表显示
- ✅ 设备状态切换 (在线/离线)
- ✅ 设备信息管理
- ✅ 实时状态更新

### 用户界面
- ✅ 现代化表格界面
- ✅ 设备状态图标 (🟢/🔴)
- ✅ 响应式设计
- ✅ 刷新功能

### 后端服务
- ✅ RESTful API
- ✅ MQTT消息处理
- ✅ Supabase数据库集成
- ✅ 错误处理和日志

## 设备状态说明

- 🟢 **在线**: 设备正常运行
- 🔴 **离线**: 设备已断开连接

## 配置说明

### Supabase配置
- Project URL: https://znarfgnwmbsawgndeuzh.supabase.co
- Publishable Key: sb_publishable_MMGYn93wCO4nsFuAWIzWNw_IaFHMO4W

### MQTT配置
- 服务器: mqtts://z01b0909.ala.asia-southeast1.emqxsl.com:8883
- 用户名: APP

## 项目文档

- [系统配置说明](./docs/system-config.md)
- [集成测试报告](./docs/integration-test-report.md)
- [CORS修复报告](./docs/cors-fix-report.md)

## 设备管理流程

1. **查看设备**: 前端显示所有设备列表
2. **状态切换**: 点击按钮切换设备在线/离线状态
3. **数据同步**: 状态变更实时同步到Supabase数据库
4. **界面更新**: 设备状态图标和文本实时更新

## 开发说明

### 前端开发
- 设备状态管理使用React Hooks
- API调用通过Axios进行
- UI组件基于Ant Design

### 后端开发
- Express框架提供REST API
- Supabase客户端进行数据库操作
- MQTT客户端处理设备通信

## 最新更新

### 2025-11-11
- ✅ 修复前端刷新按钮功能
- ✅ 修复设备状态图标显示
- ✅ 添加设备状态切换功能
- ✅ 实现完整的CRUD API
- ✅ 优化用户界面体验

## 贡献指南

1. Fork项目
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建Pull Request

## 许可证

本项目采用MIT许可证

## 联系我们

- 项目维护者: SmartHome Team
- 邮箱: support@smarthome.com