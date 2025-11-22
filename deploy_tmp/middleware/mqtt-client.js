const mqtt = require('mqtt');
require('dotenv').config();

// MQTT 配置
const MQTT_CONFIG = {
    broker: process.env.MQTT_BROKER_URL || 'mqtts://z01b0909.ala.asia-southeast1.emqxsl.com:8883',
    username: process.env.MQTT_USERNAME || 'APP',
    password: process.env.MQTT_PASSWORD || 'APP2025',
    clientId: process.env.MQTT_CLIENT_ID_PREFIX + Date.now(),
    clean: false,
    reconnectPeriod: 1000,
    connectTimeout: 30 * 1000,
    keepalive: 60,
    rejectUnauthorized: true
};

// MQTT 主题规范 - 根据文档定义
const MQTT_TOPICS = {
    // 订阅主题 (服务器端接收设备消息)
    DEVICE_TO_SERVER: 'smarthome/+/in/#',
    
    // 发布主题 (服务器端向设备发送命令)
    SERVER_TO_DEVICE_PREFIX: 'smarthome/', // 后面跟 device_id
    
    // 状态主题
    DEVICE_STATUS: 'smarthome/+/in/status',
    
    // 控制主题
    DEVICE_CONTROL_PREFIX: 'smarthome/', // 格式: smarthome/{device_id}/out/control
    
    // 配置主题
    DEVICE_CONFIG_PREFIX: 'smarthome/' // 格式: smarthome/{device_id}/out/config
};

// MQTT 客户端类
class MQTTClient {
    constructor() {
        this.client = null;
        this.isConnected = false;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.messageHandlers = new Map();
        this.statusHandlers = new Map();
        this.errorHandlers = new Map();
        
        // 记录配置信息
        this.logConfig();
    }

    // 记录配置信息
    logConfig() {
        console.log('📡 MQTT配置信息:');
        console.log('   - 服务器:', MQTT_CONFIG.broker);
        console.log('   - 用户名:', MQTT_CONFIG.username);
        console.log('   - 客户端ID:', MQTT_CONFIG.clientId);
        console.log('   - 清理会话:', MQTT_CONFIG.clean);
        console.log('   - 重连间隔:', MQTT_CONFIG.reconnectPeriod + 'ms');
        console.log('   - 连接超时:', MQTT_CONFIG.connectTimeout + 'ms');
    }

    // 连接 MQTT 代理
    async connect() {
        return new Promise((resolve, reject) => {
            try {
                console.log('🔌 连接到MQTT代理...');
                
                // 创建 MQTT 客户端
                this.client = mqtt.connect(MQTT_CONFIG.broker, {
                    clientId: MQTT_CONFIG.clientId,
                    username: MQTT_CONFIG.username,
                    password: MQTT_CONFIG.password,
                    clean: MQTT_CONFIG.clean,
                    reconnectPeriod: MQTT_CONFIG.reconnectPeriod,
                    connectTimeout: MQTT_CONFIG.connectTimeout,
                    keepalive: MQTT_CONFIG.keepalive,
                    rejectUnauthorized: MQTT_CONFIG.rejectUnauthorized
                });

                // 连接成功
                this.client.on('connect', () => {
                    console.log('✅ MQTT连接成功');
                    console.log('📡 客户端ID:', this.client.options.clientId);
                    this.isConnected = true;
                    this.reconnectAttempts = 0;
                    
                    // 订阅主题
                    this.subscribeToTopics();
                    
                    resolve();
                });

                // 连接错误
                this.client.on('error', (error) => {
                    console.error('❌ MQTT连接错误:', error.message);
                    this.isConnected = false;
                    reject(error);
                });

                // 重连
                this.client.on('reconnect', () => {
                    this.reconnectAttempts++;
                    console.log(`🔄 MQTT重连尝试 ${this.reconnectAttempts}/${this.maxReconnectAttempts}...`);
                    
                    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
                        console.error('❌ MQTT重连失败次数过多，停止重连');
                        this.client.end();
                    }
                });

                // 断线
                this.client.on('close', () => {
                    console.log('❌ MQTT连接关闭');
                    this.isConnected = false;
                });

                // 消息接收
                this.client.on('message', (topic, message) => {
                    this.handleMessage(topic, message);
                });

            } catch (error) {
                console.error('❌ MQTT客户端创建失败:', error);
                reject(error);
            }
        });
    }

    // 订阅主题
    async subscribeToTopics() {
        if (!this.isConnected) {
            throw new Error('MQTT客户端未连接');
        }

        const topics = [
            MQTT_TOPICS.DEVICE_TO_SERVER,    // 所有设备 -> 服务器的消息
            MQTT_TOPICS.DEVICE_STATUS        // 设备状态消息
        ];

        console.log('📨 订阅MQTT主题...');
        
        for (const topic of topics) {
            try {
                await this.client.subscribe(topic, {
                    qos: 1, // 至少一次投递
                });
                console.log(`   ✅ 订阅: ${topic}`);
            } catch (error) {
                console.error(`   ❌ 订阅失败 ${topic}:`, error);
            }
        }
    }

    // 处理接收到的消息
    handleMessage(topic, message) {
        try {
            let messageStr = message.toString('utf8');
            console.log(`📨 收到MQTT原始消息: topic=${topic} payload=${messageStr}`);
            messageStr = messageStr.replace(/^\ufeff/, '').trim();
            if ((messageStr.startsWith("'") && messageStr.endsWith("'")) || (messageStr.startsWith('"') && messageStr.endsWith('"'))) {
                messageStr = messageStr.slice(1, -1);
            }
            if (!(messageStr.startsWith('{') && messageStr.endsWith('}'))) {
                const start = messageStr.indexOf('{');
                const end = messageStr.lastIndexOf('}');
                if (start !== -1 && end !== -1 && end > start) {
                    messageStr = messageStr.slice(start, end + 1);
                }
            }
            const parsedMessage = JSON.parse(messageStr);
            
            console.log(`📨 收到MQTT消息:`);
            console.log(`   主题: ${topic}`);
            console.log(`   内容: ${messageStr}`);
            
            // 解析主题获取设备ID
            const topicParts = topic.split('/');
            const deviceId = topicParts[1]; // smarthome/{device_id}/...
            
            // 根据主题类型分发消息
            if (topic.includes('/status')) {
                this.handleStatusMessage(deviceId, parsedMessage);
            } else if (topic.includes('/in/')) {
                this.handleInMessage(deviceId, parsedMessage);
            }
            
            // 调用注册的处理器
            this.callHandlers('message', { topic, message: parsedMessage, deviceId });
            
        } catch (error) {
            console.error('❌ MQTT消息处理错误:', error);
            try {
                const fallback = String(message || '').trim();
                console.error('原始消息内容:', fallback);
            } catch (_) {}
        }
    }

    // 处理状态消息
    handleStatusMessage(deviceId, message) {
        console.log(`📊 设备状态更新 (${deviceId}):`, message);
        
        // 调用注册的处理器
        this.callHandlers('status', { deviceId, status: message });
    }

    // 处理设备输入消息
    handleInMessage(deviceId, message) {
        console.log(`📥 设备输入消息 (${deviceId}):`, message);
        
        // 调用注册的处理器
        this.callHandlers('incoming', { deviceId, message });
    }

    // 发布消息到设备
    async publishToDevice(deviceId, action, data, options = {}) {
        if (!this.isConnected) {
            throw new Error('MQTT客户端未连接');
        }

        let topic;
        
        // 根据操作类型确定主题
        switch (action) {
            case 'control':
                topic = `${MQTT_TOPICS.SERVER_TO_DEVICE_PREFIX}${deviceId}/out/control`;
                break;
            case 'config':
                topic = `${MQTT_TOPICS.SERVER_TO_DEVICE_PREFIX}${deviceId}/out/config`;
                break;
            case 'status':
                topic = `${MQTT_TOPICS.SERVER_TO_DEVICE_PREFIX}${deviceId}/out/status`;
                break;
            default:
                topic = `${MQTT_TOPICS.SERVER_TO_DEVICE_PREFIX}${deviceId}/out/${action}`;
        }

        const message = {
            timestamp: new Date().toISOString(),
            source: 'middleware',
            ...data
        };

        try {
            await this.client.publish(topic, JSON.stringify(message), {
                qos: 1,     // 至少一次投递
                retain: options.retain || false // 是否为保留消息
            });
            
            console.log(`📤 发送到设备消息:`);
            console.log(`   主题: ${topic}`);
            console.log(`   内容: ${JSON.stringify(message, null, 2)}`);
            
            return true;
        } catch (error) {
            console.error(`❌ 发布消息失败 ${topic}:`, error);
            throw error;
        }
    }

    // 注册消息处理器
    on(event, handler) {
        switch (event) {
            case 'message':
            case 'status':
            case 'incoming':
            case 'connect':
            case 'error':
                this.messageHandlers.set(handler, event);
                break;
            default:
                console.warn(`⚠️ 未知的事件类型: ${event}`);
        }
    }

    // 调用处理器
    callHandlers(event, data) {
        this.messageHandlers.forEach((handlerEvent, handler) => {
            if (handlerEvent === event) {
                try {
                    handler(data);
                } catch (error) {
                    console.error('❌ 消息处理器错误:', error);
                }
            }
        });
    }

    // 获取连接状态
    isClientConnected() {
        return this.isConnected && this.client && this.client.connected;
    }

    // 获取客户端信息
    getClientInfo() {
        return {
            connected: this.isClientConnected(),
            clientId: this.client?.options?.clientId,
            reconnectAttempts: this.reconnectAttempts,
            broker: MQTT_CONFIG.broker
        };
    }

    // 断开连接
    async disconnect() {
        if (this.client) {
            console.log('🔌 断开MQTT连接...');
            this.client.end();
            this.isConnected = false;
            this.messageHandlers.clear();
        }
    }
}

module.exports = {
    MQTTClient,
    MQTT_TOPICS
};
