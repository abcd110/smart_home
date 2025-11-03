# 智能家居系统

一个完整的智能家居解决方案，包含Android移动应用和Spring Boot后端系统，提供设备管理、数据采集、用户管理和安全监控等功能。

## 项目结构

```
Smarthome/
├── app/                    # Android移动应用
├── backend/                # Spring Boot后端系统
├── nginx/                  # Nginx配置
├── mosquitto/              # MQTT配置
└── prometheus/             # 监控配置
```

## 技术栈

### Android端
- Java/Kotlin
- Android SDK
- Retrofit (网络请求)
- MQTT客户端

### 后端
- Spring Boot 2.7.x
- Spring Security
- Spring Data JPA
- MySQL 8.0
- Redis 7.x
- Eclipse Mosquitto (MQTT)
- Prometheus & Grafana (监控)

## 快速开始

### 环境要求
- Java 11+
- Android Studio
- Maven 3.6+
- Docker & Docker Compose
- Git

### 克隆项目
```bash
git clone https://github.com/abcd110/smart_home.git
cd Smarthome
```

### 运行后端
```bash
cd backend
mvn clean install
mvn spring-boot:run -pl smarthome-main
```

### 运行Android
1. 使用Android Studio打开app目录
2. 同步项目依赖
3. 运行应用

## 功能特性

- 用户认证与授权
- 设备管理与控制
- 实时数据采集
- 安全事件监控
- MQTT消息通信
- 数据可视化

## 联系我们

- 项目维护者: SmartHome Team
- 邮箱: support@smarthome.com
