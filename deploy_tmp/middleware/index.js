const express = require('express');
const { createServer } = require('http');
const cors = require('cors');
const { MQTTClient, MQTT_TOPICS } = require('./mqtt-client');
const { DeviceManager, SensorDataManager, testSupabaseConnection } = require('./supabase');
require('dotenv').config();

// 设置控制台编码支持中�?
process.env.NODE_ENV = process.env.NODE_ENV || 'development';
process.stdout.setEncoding('utf8');
process.stderr.setEncoding('utf8');

// 中间件主�?
class Middleware {
    constructor() {
        this.app = express();
        this.server = createServer(this.app);
        this.mqttClient = new MQTTClient();
        this.sseClients = new Set();
        this.stats = {
            startTime: new Date(),
            messagesReceived: 0,
            messagesSent: 0,
            errors: 0,
            devicesSeen: new Set(),
            lastMessage: null
        };
        
        this.setupExpress();
        this.setupSignalHandlers();
    }

    // 设置 Express 应用
    setupExpress() {
        // CORS 配置 - 允许来自 Vite 开发服务器的跨域请�?
        this.app.use(cors({
            origin: ['http://localhost:5173', 'http://127.0.0.1:5173', 'http://8.134.63.151'],
            credentials: true,
            methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
            allowedHeaders: ['Content-Type', 'Authorization', 'X-Requested-With']
        }));
        
        // 配置JSON解析器支持UTF-8编码
        this.app.use(express.json({
            verify: (req, res, buf, encoding) => {
                if (buf && buf.length) {
                    const rawData = buf.toString(encoding || 'utf8');
                    console.log('🔍 接收到的原始数据:', rawData);
                    req.rawBody = rawData;
                }
            }
        }));
        
        // 配置URL编码解析器支持UTF-8
        this.app.use(express.urlencoded({ 
            extended: true,
            verify: (req, res, buf, encoding) => {
                if (buf && buf.length) {
                    req.rawBody = buf.toString(encoding || 'utf8');
                }
            }
        }));
        
        // 路由
        this.app.get('/health', (req, res) => this.getHealthCheck(req, res));
        this.app.get('/status', (req, res) => this.getStatus(req, res));
        this.app.get('/stats', (req, res) => this.getStats(req, res));
        this.app.get('/devices', (req, res) => this.getDevices(req, res));
        this.app.get('/sensor/latest', (req, res) => this.getLatestSensor(req, res));
        this.app.get('/sensor/summary', (req, res) => this.getSensorSummary(req, res));
        this.app.get('/sensor/history/raw', (req, res) => this.getSensorHistoryRaw(req, res));
        this.app.get('/sensor/history/agg', (req, res) => this.getSensorHistoryAgg(req, res));
        this.app.get('/events', (req, res) => this.handleSSE(req, res));
        this.app.post('/devices', (req, res) => this.createDevice(req, res));
        this.app.put('/devices/:deviceId', (req, res) => this.updateDevice(req, res));
        this.app.delete('/devices/:deviceId', (req, res) => this.deleteDevice(req, res));
        this.app.post('/devices/:deviceId/control', (req, res) => this.controlDevice(req, res));
        this.app.post('/devices/:deviceId/config', (req, res) => this.configDevice(req, res));
        this.app.post('/alarm/:deviceId/close', (req, res) => this.closeAlarm(req, res));
        
        // 404 处理
        this.app.use('*', (req, res) => {
            res.status(404).json({
                error: 'Endpoint not found',
                path: req.originalUrl
            });
        });
    }

    // 设置信号处理�?
    setupSignalHandlers() {
        process.on('SIGTERM', () => this.shutdown());
        process.on('SIGINT', () => this.shutdown());
        process.on('uncaughtException', (error) => {
            console.error('�?未捕获的异常:', error);
            this.shutdown();
        });
    }

    // 初始化中间件
    async initialize() {
        try {
            console.log('🏠 智能家居中间件启动中...');
            
            // 记录配置信息
            this.logConfiguration();
            
            // 测试 Supabase 连接
            const supabaseConnected = await testSupabaseConnection();
            if (!supabaseConnected) {
                throw new Error('Supabase连接测试失败');
            }
            
            // 初始�?MQTT 客户�?
            await this.mqttClient.connect();
            
            // 注册 MQTT 事件处理�?
        this.registerMQTTHandlers();
            
            // 启动 HTTP 服务�?
            this.startHTTPServer();
            
            console.log('�?中间件初始化完成');
            
        } catch (error) {
            console.error('�?中间件初始化失败:', error);
            throw error;
        }
    }

    // 记录配置信息
    logConfiguration() {
        console.log('📋 配置信息:');
        console.log('   - Supabase URL:', process.env.SUPABASE_URL ? '已配置' : '未配置');
        console.log('   - MQTT Broker:', process.env.MQTT_BROKER_URL || 'mqtts://z01b0909.ala.asia-southeast1.emqxsl.com:8883');
        console.log('   - 服务器端口:', process.env.SERVER_PORT || 3000);
        console.log('   - 日志级别:', process.env.LOG_LEVEL || 'info');
    }

    // 注册 MQTT 事件处理�?
    registerMQTTHandlers() {
        // 消息接收处理�?
        this.mqttClient.on('message', async (data) => {
            this.stats.messagesReceived++;
            this.stats.lastMessage = data;
            
            if (data.deviceId) {
                this.stats.devicesSeen.add(data.deviceId);
            }
            
            // 处理设备消息
            await this.processDeviceMessage(data);
        });

        // 状态消息处理器
        this.mqttClient.on('status', async (data) => {
            console.log(`📊 设备状态更�? ${data.deviceId} - ${JSON.stringify(data.status)}`);
            
            // 更新设备状态到数据�?
            try {
                await DeviceManager.updateDeviceStatus(data.deviceId, data.status.status || 'online');
            } catch (error) {
                console.error('更新设备状态失�?', error);
                this.stats.errors++;
            }
        });

        // 输入消息处理�?
        this.mqttClient.on('incoming', async (data) => {
            console.log(`📥 设备输入消息: ${data.deviceId} - ${JSON.stringify(data.message)}`);
            
            // 处理传感器数�?
            if (data.message.sensorType && data.message.value !== undefined) {
                try {
                    await SensorDataManager.insertSensorData({
                        deviceId: data.deviceId,
                        sensorType: data.message.sensorType,
                        value: data.message.value,
                        unit: data.message.unit,
                        timestamp: data.message.timestamp
                    });
                    try {
                        const { SensorLatestManager } = require('./supabase');
                        await SensorLatestManager.upsertLatest({
                            deviceId: data.deviceId,
                            sensorType: data.message.sensorType,
                            value: data.message.value,
                            unit: data.message.unit,
                            timestamp: data.message.timestamp
                        });
                        this.broadcastEvent('sensor_update', { device_id: data.deviceId, sensor_type: data.message.sensorType, value: data.message.value, timestamp: data.message.timestamp });
                    } catch (e) {
                        console.error('更新最新值快照失败:', e);
                    }
                } catch (error) {
                    console.error('插入传感器数据失�?', error);
                    this.stats.errors++;
                }
            }
            const topicStr = String(data.topic || '');
            if (topicStr.includes('/in/alarm') || data.message.alarm === true || String(data.message.type||'').toLowerCase()==='alarm') {
                const msg = data.message.message || '报警';
                const at = data.message.timestamp || new Date().toISOString();
                this.broadcastEvent('alarm_event', { device_id: data.deviceId, message: msg, at });
            }
            // 安全事件检测：人体红外 + 门磁
            try {
                this.securityState = this.securityState || { pir: false, door: false, enabledPir: true, enabledDoor: true };
                const mt = String(data.message.sensorType || '').toLowerCase();
                const val = Number(data.message.value);
                if (mt.includes('pir') || mt.includes('人体红外')) {
                    this.securityState.pir = val > 0;
                }
                if (mt.includes('door') || mt.includes('门磁')) {
                    this.securityState.door = val > 0;
                }
                if (this.securityState.enabledPir && this.securityState.enabledDoor && this.securityState.pir && this.securityState.door) {
                    const now = Date.now();
                    if (!this.securityState.lastTriggerAt || (now - this.securityState.lastTriggerAt > 5000)) {
                        this.securityState.lastTriggerAt = now;
                        this.broadcastEvent('security_event', { type: 'intrusion', message: '检测到有人且门已打开', at: new Date().toISOString() });
                        try {
                            const { DeviceManager } = require('./supabase');
                            const listRes = await DeviceManager.getAllDevices();
                            const devices = listRes && listRes.data ? listRes.data : [];
                            for (const d of devices) {
                                const type = String(d.type || '').toLowerCase();
                                if (type === 'buzzer') {
                                    await this.mqttClient.publishToDevice(d.device_id, 'control', { command: 'BUZZ_ON', parameters: {} });
                                } else if (type === 'light') {
                                    await this.mqttClient.publishToDevice(d.device_id, 'control', { command: 'COLOR_SET', parameters: { value: '#FF0000' } });
                                }
                            }
                        } catch (e) { console.error('报警联动执行失败:', e); }
                        this.securityState.pir = false; this.securityState.door = false;
                    }
                }
            } catch (e) { console.error('安全事件检测失败', e); }
        });
    }

    // 处理设备消息
    async processDeviceMessage(data) {
        try {
            // 这里可以添加更复杂的消息处理逻辑
            // 比如消息路由、业务逻辑�?
            
            if (data.message.command) {
                // 处理控制命令
                await this.handleDeviceCommand(data);
            }
            
        } catch (error) {
            console.error('�?处理设备消息失败:', error);
            this.stats.errors++;
        }
    }

    // 处理设备命令
    async handleDeviceCommand(data) {
        console.log(`🎮 处理设备命令: ${data.deviceId} - ${data.message.command}`);
        
        // 记录控制历史
        try {
            await require('./supabase').DeviceControlHistoryManager.insertControlHistory({
                deviceId: data.deviceId,
                command: data.message.command,
                parameters: data.message.parameters || {},
                status: 'received'
            });
        } catch (error) {
            console.error('记录控制历史失败:', error);
        }
    }

    // 启动 HTTP 服务�?
    startHTTPServer() {
        const port = process.env.SERVER_PORT || 3000;
        
        this.server.listen(port, () => {
            console.log(`🌐 HTTP服务器启动成功`);
            console.log(`📡 健康检�? http://localhost:${port}/health`);
            console.log(`📊 状态查�? http://localhost:${port}/status`);
            console.log(`📈 统计数据: http://localhost:${port}/stats`);
            console.log(`📱 设备列表: http://localhost:${port}/devices`);
        });
    }

    // 健康检查接�?
    getHealthCheck(req, res) {
        const health = {
            status: 'healthy',
            timestamp: new Date().toISOString(),
            uptime: process.uptime(),
            checks: {
                supabase: 'connected', // 可以添加更详细的检�?
                mqtt: this.mqttClient.isClientConnected() ? 'connected' : 'disconnected',
                database: 'unknown'
            }
        };

        res.json(health);
    }

    // 状态查询接�?
    getStatus(req, res) {
        const status = {
            server: {
                uptime: process.uptime(),
                memory: process.memoryUsage(),
                timestamp: new Date().toISOString()
            },
            mqtt: {
                connected: this.mqttClient.isClientConnected(),
                clientId: this.mqttClient.getClientInfo().clientId,
                broker: this.mqttClient.getClientInfo().broker
            },
            stats: {
                messagesReceived: this.stats.messagesReceived,
                messagesSent: this.stats.messagesSent,
                errors: this.stats.errors,
                devicesSeen: this.stats.devicesSeen.size,
                lastMessage: this.stats.lastMessage?.timestamp || null
            }
        };

        res.json(status);
    }

    // 统计数据接口
    getStats(req, res) {
        const stats = {
            runtime: {
                startTime: this.stats.startTime,
                uptime: Date.now() - this.stats.startTime.getTime()
            },
            messages: {
                received: this.stats.messagesReceived,
                sent: this.stats.messagesSent,
                errorRate: this.stats.messagesReceived > 0 ? 
                    (this.stats.errors / this.stats.messagesReceived * 100).toFixed(2) + '%' : '0%'
            },
            devices: {
                totalSeen: this.stats.devicesSeen.size,
                deviceIds: Array.from(this.stats.devicesSeen)
            },
            lastActivity: this.stats.lastMessage?.timestamp || null
        };

        res.json(stats);
    }

    // 设备列表接口
    async getDevices(req, res) {
        try {
            const result = await DeviceManager.getAllDevices();
            if (result.error) {
                throw result.error;
            }
            res.json({
                devices: result.data || [],
                count: (result.data || []).length,
                timestamp: new Date().toISOString()
            });
        } catch (error) {
            console.error('获取设备列表失败:', error);
            res.status(500).json({
                error: 'Failed to fetch devices',
                details: error.message
            });
        }
    }

    // 控制设备接口：发布到 MQTT 并记录控制历史
    async controlDevice(req, res) {
        try {
            const { deviceId } = req.params;
            const { command, parameters } = req.body || {};
            if (!deviceId || !command) {
                return res.status(400).json({
                    error: 'Missing required fields',
                    required: ['deviceId', 'command']
                });
            }

            const requestId = (Date.now().toString(36) + Math.random().toString(36).slice(2));
            const payload = { command, parameters, requestId };
            await this.mqttClient.publishToDevice(deviceId, 'control', payload);

            // 记录控制历史（非阻塞）
            try {
                await require('./supabase').DeviceControlHistoryManager.insertControlHistory({
                    deviceId,
                    command,
                    parameters: parameters || {},
                    status: 'sent',
                    response: null
                });
            } catch (e) {
                console.error('记录控制历史失败:', e);
            }

            return res.json({
                status: 'sent',
                deviceId,
                topic: `smarthome/${deviceId}/out/control`,
                requestId,
                timestamp: new Date().toISOString()
            });
        } catch (error) {
            console.error('控制设备失败:', error);
            return res.status(500).json({ error: 'Failed to control device', details: error.message });
        }
    }

    // 下发设备配置接口：发布到 MQTT
    async configDevice(req, res) {
        try {
            const { deviceId } = req.params;
            const { config } = req.body || {};
            if (!deviceId || !config) {
                return res.status(400).json({
                    error: 'Missing required fields',
                    required: ['deviceId', 'config']
                });
            }

            await this.mqttClient.publishToDevice(deviceId, 'config', { config });
            return res.json({
                status: 'sent',
                deviceId,
                topic: `smarthome/${deviceId}/out/config`,
                timestamp: new Date().toISOString()
            });
        } catch (error) {
            console.error('下发设备配置失败:', error);
            return res.status(500).json({ error: 'Failed to config device', details: error.message });
        }
    }

    async closeAlarm(req, res) {
        try {
            const { deviceId } = req.params;
            await this.mqttClient.publishToDevice(deviceId, 'alarm', { command: 'ALARM_OFF' });
            this.broadcastEvent('alarm_ack', { device_id: deviceId, at: new Date().toISOString() });
            return res.json({ status: 'sent', deviceId, timestamp: new Date().toISOString() });
        } catch (e) {
            return res.status(500).json({ error: 'Failed to close alarm', details: e.message });
        }
    }

    async getLatestSensor(req, res) {
        try {
            const { sensor_type } = req.query;
            if (!sensor_type) {
                return res.status(400).json({ error: 'Missing sensor_type' });
            }
            const aliases = this.resolveSensorAliases(sensor_type);
            const { SensorDataQuery } = require('./supabase');
            const data = await SensorDataQuery.getLatestByTypes(aliases);
            return res.json({ data, count: data.length, timestamp: new Date().toISOString() });
        } catch (error) {
            console.error('获取最新传感器数据失败:', error);
            return res.status(500).json({ error: 'Failed to fetch latest sensor data', details: error.message });
        }
    }

    async getSensorSummary(req, res) {
        try {
            const groups = {
                temperature: this.resolveSensorAliases('temperature'),
                humidity: this.resolveSensorAliases('humidity'),
                gas: this.resolveSensorAliases('gas')
            };
            const { SensorLatestManager, SensorDataQuery } = require('./supabase');
            let temp, hum, gas;
            try {
                temp = await SensorLatestManager.getLatestByTypes(groups.temperature);
                hum = await SensorLatestManager.getLatestByTypes(groups.humidity);
                gas = await SensorLatestManager.getLatestByTypes(groups.gas);
            } catch (e) {
                temp = await SensorDataQuery.getLatestByTypes(groups.temperature);
                hum = await SensorDataQuery.getLatestByTypes(groups.humidity);
                gas = await SensorDataQuery.getLatestByTypes(groups.gas);
            }
            return res.json({
                temperature: temp,
                humidity: hum,
                gas: gas,
                timestamp: new Date().toISOString()
            });
            try { this.broadcastEvent('sensor_summary', { temperature: temp, humidity: hum, gas: gas, timestamp: new Date().toISOString() }); } catch (_) {}
        } catch (error) {
            console.error('获取传感器概览失败:', error);
            return res.status(500).json({ error: 'Failed to fetch sensor summary', details: error.message });
        }
    }

    handleSSE(req, res) {
        res.setHeader('Content-Type', 'text/event-stream');
        res.setHeader('Cache-Control', 'no-cache');
        res.setHeader('Connection', 'keep-alive');
        res.flushHeaders();
        const client = res;
        this.sseClients.add(client);
        client.write(`event: ping\n`);
        client.write(`data: ${JSON.stringify({ ts: new Date().toISOString() })}\n\n`);
        req.on('close', () => { this.sseClients.delete(client); });
    }

    broadcastEvent(event, data) {
        if (!this.sseClients || this.sseClients.size === 0) return;
        const payload = `event: ${event}\n` + `data: ${JSON.stringify(data)}\n\n`;
        for (const client of this.sseClients) {
            try { client.write(payload); } catch (e) {}
        }
    }

    async getSensorHistoryRaw(req, res) {
        try {
            const { device_id, sensor_type, from, to, order = 'asc', limit } = req.query;
            if (!sensor_type) {
                return res.status(400).json({ error: 'Missing sensor_type' });
            }
            const aliases = this.resolveSensorAliases(sensor_type);
            const { SensorDataQuery } = require('./supabase');
            const data = await SensorDataQuery.getRaw({ deviceId: device_id, sensorTypes: aliases, from, to, order, limit });
            return res.json({ data, count: data.length, timestamp: new Date().toISOString() });
        } catch (error) {
            console.error('获取历史原始数据失败:', error);
            return res.status(500).json({ error: 'Failed to fetch sensor history (raw)', details: error.message });
        }
    }

    async getSensorHistoryAgg(req, res) {
        try {
            const { device_id, sensor_type, from, to, bucket = '1h' } = req.query;
            if (!sensor_type) {
                return res.status(400).json({ error: 'Missing sensor_type' });
            }
            const aliases = this.resolveSensorAliases(sensor_type);
            const { SensorDataQuery } = require('./supabase');
            const data = await SensorDataQuery.getAggregated({ deviceId: device_id, sensorTypes: aliases, from, to, bucket });
            return res.json({ data, count: data.length, bucket, timestamp: new Date().toISOString() });
        } catch (error) {
            console.error('获取历史聚合数据失败:', error);
            return res.status(500).json({ error: 'Failed to fetch sensor history (agg)', details: error.message });
        }
    }

    resolveSensorAliases(type) {
        const key = String(type || '').toLowerCase();
        if (key === 'temperature') return ['temperature', '温度'];
        if (key === 'humidity') return ['humidity', '湿度'];
        if (key === 'gas') return ['gas', '可燃气体', '煤气浓度'];
        return [type];
    }

    // 创建设备接口
    async createDevice(req, res) {
        try {
            const { device_id, name, type, status = 'offline', location, description } = req.body;
            
            if (!device_id || !name || !type) {
                return res.status(400).json({
                    error: 'Missing required fields',
                    required: ['device_id', 'name', 'type']
                });
            }
            
            const deviceData = {
                device_id,
                name,
                type,
                status,
                location,
                description,
                is_active: true
            };
            
            const result = await DeviceManager.createDevice(deviceData);
            if (result.error) {
                throw result.error;
            }
            
            res.status(201).json({
                message: 'Device created successfully',
                device: result.data,
                timestamp: new Date().toISOString()
            });
        } catch (error) {
            console.error('创建设备失败:', error);
            res.status(500).json({
                error: 'Failed to create device',
                details: error.message
            });
        }
    }

    // 更新设备接口
    async updateDevice(req, res) {
        try {
            const { deviceId } = req.params;
            const updates = req.body;
            
            if (!deviceId) {
                return res.status(400).json({
                    error: 'Missing device ID',
                    required: ['deviceId']
                });
            }
            
            console.log(`🔄 更新设备 ${deviceId}:`, updates);
            
            const result = await DeviceManager.updateDevice(deviceId, updates);
            if (result.error) {
                throw result.error;
            }
            
            res.json({
                message: 'Device updated successfully',
                device: result.data,
                timestamp: new Date().toISOString()
            });
        } catch (error) {
            console.error('更新设备失败:', error);
            res.status(500).json({
                error: 'Failed to update device',
                details: error.message
            });
        }
    }

    // 删除设备接口
    async deleteDevice(req, res) {
        try {
            const { deviceId } = req.params;
            
            if (!deviceId) {
                return res.status(400).json({
                    error: 'Missing device ID',
                    required: ['deviceId']
                });
            }
            
            console.log(`🗑�?删除设备 ${deviceId}`);
            
            const result = await DeviceManager.deleteDevice(deviceId);
            if (result.error) {
                throw result.error;
            }
            
            res.json({
                message: 'Device deleted successfully',
                timestamp: new Date().toISOString()
            });
        } catch (error) {
            console.error('删除设备失败:', error);
            res.status(500).json({
                error: 'Failed to delete device',
                details: error.message
            });
        }
    }

    // 优雅关闭
    async shutdown() {
        console.log('🛑 开始优雅关�?..');
        
        try {
            // 关闭 MQTT 连接
            if (this.mqttClient) {
                await this.mqttClient.disconnect();
            }
            
            // 关闭 HTTP 服务�?
            if (this.server) {
                this.server.close();
            }
            
            console.log('�?优雅关闭完成');
            process.exit(0);
        } catch (error) {
            console.error('�?关闭过程中发生错�?', error);
            process.exit(1);
        }
    }
}

// 如果直接运行此文�?
if (require.main === module) {
    const middleware = new Middleware();
    
    middleware.initialize().catch(error => {
        console.error('�?启动失败:', error);
        process.exit(1);
    });
}

module.exports = { Middleware };
