# 智能家居系统

一个基于Android、React、Node.js和Supabase的现代化智能家居管理系统。

## 项目架构

```
智能家居系统/
├── Smarthome/           # 主项目目录
│   ├── app/            # Android应用
│   ├── web-simulator/  # React前端模拟器
│   ├── docs/           # 项目文档
│   ├── nginx/          # Nginx配置
│   └── ...
├── mqtt-supabase-middleware/  # MQTT中间件服务
└── 文档/               # 项目文档目录
```

## 项目组件

### 1. Smarthome (主项目)
- **Android应用** (`app/`): 原生Android客户端
- **前端模拟器** (`web-simulator/`): React-based Web界面
- **后端服务**: Spring Boot API服务
- **配置管理**: Nginx反向代理配置

### 2. MQTT Supabase Middleware
- **功能**: 连接MQTT broker与Supabase数据库
- **技术栈**: Node.js, MQTT.js, @supabase/supabase-js
- **作用**: 实现设备数据的实时同步

### 3. 技术栈

#### 前端
- Android (Java/Kotlin)
- React.js
- HTML/CSS/JavaScript

#### 后端
- Node.js (MQTT Middleware)
- Supabase (数据库)
- MQTT Broker

#### 部署
- Nginx (反向代理)
- Docker (容器化)

## 快速开始

### 环境要求
- Android Studio
- Node.js 16+
- Java 11+
- Docker (可选)

### Android应用
1. 打开Android Studio
2. 导入`app/`目录
3. 配置Supabase连接参数
4. 运行应用

### 前端模拟器
```bash
cd web-simulator/
npm install
npm start
```

### MQTT Middleware
```bash
cd mqtt-supabase-middleware/
npm install
npm start
```

## 项目配置

### Supabase配置
- **Project URL**: https://znarfgnwmbsawgndeuzh.supabase.co
- **Publishable Key**: sb_publishable_MMGYn93wCO4nsFuAWIzWNw_IaFHMO4W

### 目录说明
- `app/`: Android应用源代码
- `web-simulator/`: React前端模拟器
- `mqtt-supabase-middleware/`: MQTT中间件服务
- `docs/`: 技术文档和配置说明
- `nginx/`: Nginx配置文件
- `文档/`: 项目文档目录

## API接口文档

### 设备管理
- `GET /api/devices` - 获取设备列表
- `POST /api/devices` - 创建设备
- `PUT /api/devices/:deviceId` - 更新设备信息
- `DELETE /api/devices/:deviceId` - 删除设备
- `PUT /api/devices/:deviceId/status` - 切换设备状态

### 用户认证
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/logout` - 用户登出

## 功能特性

- ✅ 设备状态监控
- ✅ 实时数据同步
- ✅ 用户认证管理
- ✅ MQTT消息处理
- ✅ 设备控制界面
- ✅ Android原生应用
- ✅ Web模拟器界面
- ✅ Nginx反向代理

## 最新更新

### v1.2.0 (2025-11-12)
- 修复后端API路由问题
- 完善设备状态切换功能
- 添加web-simulator前端模拟器
- 集成mqtt-supabase-middleware中间件
- 更新README文档

### v1.1.0
- 实现Supabase数据库集成
- 完善Android应用功能

### v1.0.0
- 初始版本发布
- 基础设备管理功能

## 贡献指南

1. Fork本项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启Pull Request

## 许可证

本项目采用MIT许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 联系方式

如有问题或建议，请创建Issue或联系维护者。