# 智能家居后端系统

一个基于Spring Boot的企业级智能家居后端系统，提供完整的设备管理、用户管理、数据采集、安全监控和MQTT通信功能。

## 🏗️ 系统架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   前端应用      │    │   移动端应用    │    │   IoT设备       │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                    ┌─────────────┴─────────────┐
                    │      Nginx (反向代理)      │
                    └─────────────┬─────────────┘
                                 │
                    ┌─────────────┴─────────────┐
                    │   Spring Boot 后端服务    │
                    └─────────────┬─────────────┘
                                 │
          ┌──────────────────────┼──────────────────────┐
          │                      │                      │
┌─────────┴───────┐    ┌─────────┴───────┐    ┌─────────┴───────┐
│   MySQL 数据库   │    │   Redis 缓存    │    │ MQTT 消息代理   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🚀 核心功能

### 用户管理
- 用户注册、登录、权限管理
- JWT令牌认证
- 角色权限控制（管理员/普通用户）
- 用户信息管理

### 社区管理
- 社区创建和管理
- 社区成员管理
- 社区设备统计

### 设备管理
- 设备注册和配置
- 设备状态监控
- 设备分类管理
- 设备权限控制

### 数据采集
- 传感器数据实时采集
- 数据存储和查询
- 数据统计分析
- 历史数据管理

### 安全监控
- 安全事件管理
- 告警处理
- 事件统计分析
- 安全日志记录

### MQTT通信
- 设备消息通信
- 实时数据推送
- 命令下发
- 状态同步

## 🛠️ 技术栈

### 后端框架
- **Spring Boot 2.7.x** - 主框架
- **Spring Security** - 安全认证
- **Spring Data JPA** - 数据访问
- **MyBatis Plus** - ORM框架
- **Spring Integration** - 系统集成

### 数据存储
- **MySQL 8.0** - 主数据库
- **Redis 7.x** - 缓存数据库
- **HikariCP** - 连接池

### 消息通信
- **Eclipse Mosquitto** - MQTT消息代理
- **Spring Integration MQTT** - MQTT集成

### 监控运维
- **Prometheus** - 指标监控
- **Grafana** - 数据可视化
- **Spring Boot Actuator** - 应用监控

### 部署工具
- **Docker** - 容器化
- **Docker Compose** - 容器编排
- **Nginx** - 反向代理

## 📦 快速开始

### 环境要求
- Java 11+
- Maven 3.6+
- Docker & Docker Compose
- Git

### 1. 克隆项目
```bash
git clone https://github.com/abcd110/smart_home.git
cd Smarthome
```

### 2. 构建项目
```bash
# Linux/Mac
chmod +x deploy.sh
./deploy.sh build

# Windows
deploy.bat build
```

### 3. 启动服务
```bash
# Linux/Mac
./deploy.sh

# Windows
deploy.bat
```

### 4. 验证部署
访问以下地址验证服务是否正常：
- 后端API: http://localhost:8080
- API文档: http://localhost:8080/swagger-ui/index.html
- 健康检查: http://localhost:8080/actuator/health
- Grafana: http://localhost:3000 (admin/SmartHome@2025)
- Prometheus: http://localhost:9090

## 📋 API文档

系统提供完整的RESTful API，包括：

### 认证相关
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/logout` - 用户登出
- `POST /api/auth/refresh` - 刷新令牌

### 用户管理
- `GET /api/users` - 获取用户列表
- `GET /api/users/{id}` - 获取用户详情
- `PUT /api/users/{id}` - 更新用户信息
- `DELETE /api/users/{id}` - 删除用户

### 设备管理
- `GET /api/devices` - 获取设备列表
- `POST /api/devices` - 创建设备
- `PUT /api/devices/{id}` - 更新设备
- `DELETE /api/devices/{id}` - 删除设备

### 数据采集
- `POST /api/sensor-data/report` - 上报传感器数据
- `GET /api/sensor-data` - 查询传感器数据
- `GET /api/sensor-data/statistics` - 获取数据统计

### 安全监控
- `GET /api/security-events` - 获取安全事件
- `POST /api/security-events` - 创建安全事件
- `PUT /api/security-events/{id}/handle` - 处理安全事件

详细API文档请访问：http://localhost:8080/swagger-ui/index.html

## 🔧 配置说明

### 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/smarthome
    username: smarthome
    password: SmartHome@2025
```

### Redis配置
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: SmartHome@2025
```

### MQTT配置
```yaml
mqtt:
  broker-url: tcp://localhost:1883
  username: smarthome
  password: SmartHome@2025
```

## 🐳 Docker部署

### 使用Docker Compose
```bash
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

### 服务端口映射
- 后端服务: 8080
- MySQL: 3306
- Redis: 6379
- MQTT: 1883, 9001
- Nginx: 80, 443
- Prometheus: 9090
- Grafana: 3000

## 📊 监控指标

系统提供丰富的监控指标：

### 应用指标
- HTTP请求数量和响应时间
- JVM内存使用情况
- 数据库连接池状态
- 缓存命中率

### 业务指标
- 在线设备数量
- 数据上报频率
- 安全事件统计
- 用户活跃度

### 系统指标
- CPU使用率
- 内存使用率
- 磁盘IO
- 网络流量

## 🔒 安全特性

### 认证授权
- JWT令牌认证
- 角色权限控制
- API访问控制
- 会话管理

### 数据安全
- 密码加密存储
- 敏感数据脱敏
- SQL注入防护
- XSS攻击防护

### 通信安全
- HTTPS传输加密
- MQTT认证授权
- API签名验证
- 访问日志记录

## 🧪 测试

### 单元测试
```bash
mvn test
```

### 集成测试
```bash
mvn verify
```

### API测试
使用Postman或其他API测试工具，导入API文档进行测试。

## 📝 开发指南

### 代码规范
- 遵循阿里巴巴Java开发手册
- 使用统一的代码格式化配置
- 编写完整的JavaDoc注释
- 遵循RESTful API设计规范

### 分支管理
- `main` - 主分支，用于生产环境
- `develop` - 开发分支，用于集成测试
- `feature/*` - 功能分支，用于新功能开发
- `hotfix/*` - 热修复分支，用于紧急修复

### 提交规范
```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整
refactor: 代码重构
test: 测试相关
chore: 构建过程或辅助工具的变动
```

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📞 联系我们

- 项目维护者: SmartHome Team
- 邮箱: support@smarthome.com
- 问题反馈: [GitHub Issues](https://github.com/abcd110/smart_home/issues)

## 🙏 致谢

感谢所有为这个项目做出贡献的开发者！

---

**注意**: 这是一个演示项目，请根据实际需求调整配置和安全设置。
