# MQTT Supabase Middleware

这是智能家居系统中的中间件服务，负责连接MQTT broker与Supabase数据库，实现设备数据的实时同步。

## 功能特性

- **MQTT客户端**：连接MQTT broker，订阅设备主题
- **Supabase集成**：将MQTT消息数据存储到Supabase数据库
- **实时同步**：实现设备状态的实时更新
- **消息处理**：支持设备数据的解析和处理

## 技术栈

- Node.js
- MQTT.js
- @supabase/supabase-js
- dotenv

## 快速开始

1. 安装依赖：
```bash
npm install
```

2. 配置环境变量：
复制`.env`文件并配置相关参数：
- `MQTT_BROKER_URL`：MQTT broker地址
- `SUPABASE_URL`：Supabase项目URL
- `SUPABASE_ANON_KEY`：Supabase匿名密钥

3. 启动服务：
```bash
npm start
```

## 文件结构

- `index.js`：主服务入口
- `mqtt-client.js`：MQTT客户端实现
- `supabase.js`：Supabase客户端实现
- `package.json`：项目配置和依赖
- `.env`：环境变量配置

## 贡献指南

1. Fork本项目
2. 创建特性分支：`git checkout -b feature/amazing-feature`
3. 提交更改：`git commit -m 'Add some amazing feature'`
4. 推送分支：`git push origin feature/amazing-feature`
5. 提交Pull Request

## 许可证

本项目采用MIT许可证，详见LICENSE文件。