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
        this.app.post('/devices', (req, res) => this.createDevice(req, res));
        this.app.put('/devices/:deviceId', (req, res) => this.updateDevice(req, res));
        this.app.delete('/devices/:deviceId', (req, res) => this.deleteDevice(req, res));
        this.app.get('/sensor/latest', (req, res) => this.getSensorLatest(req, res));
        this.app.get('/sensor/summary', (req, res) => this.getSensorSummary(req, res));
        this.app.get('/sensor/history/raw', (req, res) => this.getSensorHistoryRaw(req, res));
        this.app.get('/sensor/history/agg', (req, res) => this.getSensorHistoryAgg(req, res));
        this.app.post('/sensor/ingest', express.json(), async (req, res) => {
            const body = req.body || {};
            const deviceId = body.device_id || body.deviceId;
            const sensorType = body.sensor_type || body.sensorType;
            const value = body.value;
            const unit = body.unit || '';
            const timestamp = body.timestamp || body.ts;
            if (!deviceId || !sensorType || value === undefined) return res.status(400).json({ error: 'missing fields' });
            try {
                await SensorDataManager.insertSensorData({ deviceId, sensorType, value, unit, timestamp });
                try {
                    const { SensorLatestManager } = require('./supabase');
                    await SensorLatestManager.upsertLatest({ deviceId, sensorType, value, unit, timestamp });
                } catch (e) { console.error('更新最新值快照失败:', e); }
                return res.json({ ok: true });
            } catch (e) {
                console.error('HTTP ingest 插入失败:', e);
                return res.status(500).json({ error: e.message });
            }
        });
        // SSE 事件流（告知 App 实时安全事件）
        this.sseClients = new Set();
        this.app.get('/events', (req, res) => {
            res.setHeader('Content-Type', 'text/event-stream');
            res.setHeader('Cache-Control', 'no-cache');
            res.setHeader('Connection', 'keep-alive');
            res.flushHeaders();
            this.sseClients.add(res);
            req.on('close', ()=>{ this.sseClients.delete(res); });
        });

        this.app.post('/devices/:deviceId/control', async (req, res) => {
            try {
                const deviceId = req.params.deviceId;
                const body = req.body || {};
                const cmd = (body.command || '').toLowerCase();
                const parameters = body.parameters || { value: body.value };
                try { await require('./supabase').DeviceControlHistoryManager.insertControlHistory({ deviceId, command: cmd, parameters, status: 'received' }); } catch(e) { console.warn('skip control history(received):', e?.message || e); }
                let payload = { timestamp: new Date().toISOString(), source: 'middleware' };
                if (cmd === 'brightness') {
                    const v = parameters.value ?? parameters.brightness ?? parameters.level ?? 50;
                    payload.brightness_set = v; payload.value = v;
                } else if (cmd === 'color_temp') {
                    const v = parameters.value ?? parameters.color_temp ?? 'natural';
                    payload.color_temp_set = v; payload.value = v;
                } else if (cmd === 'power') {
                    const v = String(parameters.value ?? 'ON').toUpperCase();
                    payload.power = v; payload.value = v;
                } else {
                    payload.command = cmd; payload.parameters = parameters;
                }
                await this.mqttClient.publishToDevice(deviceId, 'control', payload);
                try { await require('./supabase').DeviceControlHistoryManager.insertControlHistory({ deviceId, command: cmd, parameters, status: 'sent' }); } catch(e) { console.warn('skip control history(sent):', e?.message || e); }
                res.json({ ok: true });
            } catch (e) {
                this.stats.errors++; res.status(500).json({ error: e.message });
            }
        });
        
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
        console.log('   - Supabase URL:', process.env.SUPABASE_URL ? 'configured' : 'not configured');
        console.log('   - MQTT Broker:', process.env.MQTT_URL || process.env.MQTT_BROKER_URL);
        console.log('   - 服务器端�?', process.env.SERVER_PORT || 3000);
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
            const msg = data.message || {};
            const deviceId = msg.device_id || data.deviceId;
            const sensorType = msg.sensorType || msg.sensor_type;
            const value = msg.value;
            const unit = msg.unit || (sensorType==='hall'?'binary':'');
            const timestamp = msg.timestamp || msg.ts;
            if (sensorType && value !== undefined) {
                try {
                    await SensorDataManager.insertSensorData({ deviceId, sensorType, value, unit, timestamp });
                    try {
                        const { SensorLatestManager } = require('./supabase');
                        await SensorLatestManager.upsertLatest({ deviceId, sensorType, value, unit, timestamp });
                    } catch (e) { console.error('更新最新值快照失败:', e); }
                    if (sensorType === 'hall') {
                        const st = (msg.status||'').toLowerCase();
                        const opened = st==='magnet_missing' || Number(value)===1;
                        const evt = { device_id: deviceId, type: 'door', sensor_type: 'hall', status: opened?'open':'closed', value: Number(value)||0, timestamp: new Date().toISOString() };
                        const line = `event: security_event\n` + `data: ${JSON.stringify(evt)}\n\n`;
                        for (const c of this.sseClients) { try { c.write(line); } catch(e){} }
                    }
                } catch (error) {
                    console.error('插入传感器数据失败:', error);
                    this.stats.errors++;
                }
            }
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

        try {
            const cmd = (data.message.command || '').toLowerCase();
            const p = data.message.parameters || {};
            let payload = { timestamp: new Date().toISOString(), source: 'middleware' };
            if (cmd === 'brightness' || cmd === 'light_brightness') {
                const v = p.value ?? p.brightness ?? p.level ?? 50;
                payload.brightness_set = v;
                payload.value = v;
            } else if (cmd === 'color_temp' || cmd === 'light_color_temp') {
                const v = (p.value ?? p.color_temp ?? 'natural');
                payload.color_temp_set = v;
                payload.value = v;
            } else if (cmd === 'power') {
                const v = (p.value ?? 'ON');
                payload.power = String(v).toUpperCase();
                payload.value = payload.power;
            } else if (cmd) {
                payload.command = cmd;
                payload.parameters = p;
            }
            await this.mqttClient.publishToDevice(data.deviceId, 'control', payload);
            try {
                await require('./supabase').DeviceControlHistoryManager.insertControlHistory({
                    deviceId: data.deviceId,
                    command: data.message.command,
                    parameters: data.message.parameters || {},
                    status: 'sent'
                });
            } catch {}
        } catch (e) {
            console.error('发布控制到设备失败:', e);
            this.stats.errors++;
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
            console.log(`📈 最新传感器: http://localhost:${port}/sensor/latest?sensor_type=temperature`);
            console.log(`📊 概览: http://localhost:${port}/sensor/summary`);
            console.log(`🧾 历史原始: http://localhost:${port}/sensor/history/raw`);
            console.log(`📉 历史聚合: http://localhost:${port}/sensor/history/agg`);
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

    async getSensorLatest(req, res) {
        try {
            const t = (req.query.sensor_type||'').toLowerCase();
            const { data, error } = await require('./supabase').supabase
                .from('sensor_latest')
                .select('device_id,value,timestamp')
                .eq('sensor_type', t)
                .order('timestamp', { ascending: false })
                .limit(1);
            if (error) return res.status(500).json({ error: error.message });
            res.json({ data: data||[] });
        } catch (e) { res.status(500).json({ error: e.message }); }
    }

    async getSensorSummary(req, res) {
        try {
            const types = ['temperature','humidity','gas'];
            const out = {};
            for (const t of types) {
                const r = await require('./supabase').supabase
                    .from('sensor_latest')
                    .select('device_id,value,timestamp')
                    .eq('sensor_type', t)
                    .order('timestamp', { ascending: false })
                    .limit(10);
                out[t] = r.data||[];
            }
            res.json(out);
        } catch (e) { res.status(500).json({ error: e.message }); }
    }

    async getSensorHistoryRaw(req, res) {
        try {
            let q = require('./supabase').supabase.from('sensor_data').select('*');
            if (req.query.device_id) q = q.eq('device_id', req.query.device_id);
            if (req.query.sensor_type) q = q.eq('sensor_type', req.query.sensor_type);
            if (req.query.from) q = q.gte('timestamp', req.query.from);
            if (req.query.to) q = q.lte('timestamp', req.query.to);
            const asc = (req.query.order||'desc').toLowerCase()!=='desc';
            q = q.order('timestamp', { ascending: asc });
            if (req.query.limit) q = q.limit(Number(req.query.limit));
            const { data, error } = await q;
            if (error) return res.status(500).json({ error: error.message });
            res.json(data||[]);
        } catch (e) { res.status(500).json({ error: e.message }); }
    }

    async getSensorHistoryAgg(req, res) {
        try {
            const device_id = req.query.device_id||null;
            const sensor_type = req.query.sensor_type||null;
            const from = req.query.from||null;
            const to = req.query.to||null;
            const bucket = req.query.bucket||'5m';
            const rpc = await require('./supabase').supabase.rpc('sensor_history_agg', { p_device: device_id, p_type: sensor_type, p_from: from, p_to: to, p_bucket: bucket });
            if (!rpc.error && rpc.data) return res.json({ data: rpc.data });
            const seconds = bucket==='30m'?1800:bucket==='1h'?3600:bucket==='6h'?21600:300;
            let q = require('./supabase').supabase.from('sensor_data').select('value,timestamp');
            if (device_id) q = q.eq('device_id', device_id);
            if (sensor_type) q = q.eq('sensor_type', sensor_type);
            if (from) q = q.gte('timestamp', from);
            if (to) q = q.lte('timestamp', to);
            const r = await q.order('timestamp', { ascending: true });
            if (r.error) return res.status(500).json({ error: r.error.message });
            const buckets = new Map();
            for (const row of r.data||[]) {
                const t = Math.floor(new Date(row.timestamp).getTime()/1000);
                const b = Math.floor(t/seconds)*seconds*1000;
                const v = buckets.get(b)||{ sum:0, count:0, ts:b };
                v.sum += Number(row.value);
                v.count += 1;
                buckets.set(b, v);
            }
            const out = Array.from(buckets.values()).sort((a,b)=>a.ts-b.ts).map(x=>({ bucket: new Date(x.ts).toISOString(), avg: x.sum/x.count }));
            res.json({ data: out });
        } catch (e) { res.status(500).json({ error: e.message }); }
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

