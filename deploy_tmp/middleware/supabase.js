const { createClient } = require('@supabase/supabase-js');
require('dotenv').config();

// Supabase 配置
const supabaseUrl = process.env.SUPABASE_URL;
const supabaseKey = process.env.SUPABASE_ANON_KEY;
const supabaseServiceKey = process.env.SUPABASE_SERVICE_ROLE_KEY;

// 创建 Supabase 客户端
const supabase = createClient(supabaseUrl, supabaseServiceKey || supabaseKey, {
  auth: {
    autoRefreshToken: true,
    persistSession: false
  }
});

// 打印配置信息
console.log('🔍 Supabase配置信息:');
console.log('   - URL:', supabaseUrl);
console.log('   - Key类型:', supabaseServiceKey ? 'Service Role' : 'Anonymous');
console.log('   - Key前缀:', (supabaseServiceKey || supabaseKey)?.substring(0, 20) + '...');

// 测试 Supabase 连接
async function testSupabaseConnection() {
    try {
        console.log('🔍 测试Supabase连接...');
        
        // 测试Supabase客户端初始化
        if (!supabase) {
            console.error('❌ Supabase客户端未初始化');
            return false;
        }
        
        console.log('✅ Supabase客户端初始化成功');
        
        // 测试数据库表访问
        console.log('🔍 测试数据库表访问...');
        
        try {
            const { data, error } = await supabase
                .from('sensor_data')
                .select('*')
                .limit(1);
            
            if (error) {
                console.error('❌ 数据库表访问失败:', error);
                console.log('💡 可能的原因:');
                console.log('   1. 数据库表未创建');
                console.log('   2. API密钥权限不足');
                console.log('   3. 网络连接问题');
                return true;
            }
            
            console.log('✅ 数据库表访问成功');
            console.log('📊 当前设备数量:', data?.length || 0);
            return true;
            
        } catch (tableError) {
            console.error('❌ 数据库表访问异常:', tableError);
            return true;
        }
        
    } catch (error) {
        console.error('❌ 连接测试失败:', error.message);
        return false;
    }
}

class DeviceManager {
    static get tableName() {
        return 'devices';
    }

    // 创建设备
    static async createDevice(device) {
        try {
            console.log('创建设备:', device);
            
            const newDevice = {
                device_id: device.device_id,
                name: device.name,
                type: device.type,
                status: device.status || 'offline',
                is_active: device.is_active !== false
            };

            const { data, error } = await supabase
                .from(this.tableName)
                .insert([newDevice])
                .select()
                .single();
        
            return { data, error };
        } catch (error) {
            console.error('DeviceManager.createDevice 错误:', error);
            throw error;
        }
    }

    // 获取所有设备
    static async getAllDevices() {
        const { data, error } = await supabase
            .from(DeviceManager.tableName)
            .select('*');
        
        return { data, error };
    }

    // 根据设备ID获取设备
    static async getDeviceById(deviceId) {
        const { data, error } = await supabase
            .from(DeviceManager.tableName)
            .select('*')
            .eq('device_id', deviceId)
            .single();
        
        return { data, error };
    }

    // 根据主键ID获取设备
    static async getDeviceByPk(id) {
        const { data, error } = await supabase
            .from(DeviceManager.tableName)
            .select('*')
            .eq('id', id)
            .single();
        
        return { data, error };
    }

    // 更新设备信息
    static async updateDevice(deviceId, updates) {
        const { data, error } = await supabase
            .from(DeviceManager.tableName)
            .update({
                name: updates.name,
                type: updates.type,
                status: updates.status,
                is_active: updates.isActive,
                last_seen_at: new Date().toISOString()
            })
            .eq('device_id', deviceId);
        
        return { data, error };
    }

    // 删除设备
    static async deleteDevice(deviceId) {
        const { data, error } = await supabase
            .from(DeviceManager.tableName)
            .delete()
            .eq('device_id', deviceId);
        
        return { data, error };
    }

    // 更新设备状态
    static async updateDeviceStatus(deviceId, status) {
        const { data, error } = await supabase
            .from(DeviceManager.tableName)
            .update({
                status: status,
                last_seen_at: new Date().toISOString()
            })
            .eq('device_id', deviceId);
        
        return { data, error };
    }
}

// 传感器数据管理类
class SensorDataManager {
    // 插入传感器数据
    static async insertSensorData(sensorData) {
        try {
            const { randomUUID } = require('crypto');
            
            const newSensorData = {
                id: randomUUID(),
                device_id: sensorData.deviceId,
                sensor_type: sensorData.sensorType,
                value: sensorData.value,
                unit: sensorData.unit || '',
                timestamp: sensorData.timestamp || new Date().toISOString(),
                created_at: new Date().toISOString()
            };

            const { data, error } = await supabase
                .from('sensor_data')
                .insert([newSensorData])
                .select()
                .single();
            
            if (error) {
                console.error('插入传感器数据失败:', error);
                throw error;
            }
            
            console.log(`📊 插入传感器数据: ${sensorData.sensorType}=${sensorData.value}${sensorData.unit || ''} (设备: ${sensorData.deviceId})`);
            return data;
        } catch (error) {
            console.error('SensorDataManager.insertSensorData 错误:', error);
            throw error;
        }
    }

    // 获取设备传感器数据
    static async getSensorData(deviceId, limit = 100) {
        try {
            const { data, error } = await supabase
                .from('sensor_data')
                .select('*')
                .eq('device_id', deviceId)
                .order('timestamp', { ascending: false })
                .limit(limit);
            
            if (error) {
                console.error('获取传感器数据失败:', error);
                throw error;
            }
            
            console.log(`📈 获取到 ${data?.length || 0} 条传感器数据 (设备: ${deviceId})`);
            return data || [];
        } catch (error) {
            console.error('SensorDataManager.getSensorData 错误:', error);
            throw error;
        }
    }
}

class SensorDataQuery {
    static async getLatestByTypes(sensorTypes = []) {
        try {
            const key = supabaseServiceKey || supabaseKey;
            const encoded = sensorTypes.map(s => encodeURIComponent(s)).join(',');
            const url = `${supabaseUrl}/rest/v1/sensor_data?sensor_type=in.(${encoded})&order=timestamp.desc&limit=1`;
            const res = await fetch(url, {
                headers: {
                    apikey: key,
                    Authorization: `Bearer ${key}`,
                    'Content-Type': 'application/json'
                }
            });
            if (!res.ok) {
                const text = await res.text();
                throw new Error(text || `HTTP ${res.status}`);
            }
            const data = await res.json();
            return data || [];
        } catch (e) {
            throw e;
        }
    }

    static async getRaw({ deviceId, sensorTypes = [], from, to, order = 'asc', limit = 1000 }) {
        const key = supabaseServiceKey || supabaseKey;
        const params = [];
        if (deviceId) params.push(`device_id=eq.${encodeURIComponent(deviceId)}`);
        if (sensorTypes && sensorTypes.length) {
            const encoded = sensorTypes.map(s => encodeURIComponent(s)).join(',');
            params.push(`sensor_type=in.(${encoded})`);
        }
        if (from) params.push(`timestamp=gte.${encodeURIComponent(from)}`);
        if (to) params.push(`timestamp=lte.${encodeURIComponent(to)}`);
        params.push(`order=timestamp.${order === 'desc' ? 'desc' : 'asc'}`);
        params.push(`limit=${Math.max(1, Math.min(10000, Number(limit) || 1000))}`);
        const url = `${supabaseUrl}/rest/v1/sensor_data?${params.join('&')}`;
        const res = await fetch(url, { headers: { apikey: key, Authorization: `Bearer ${key}`, 'Content-Type': 'application/json' } });
        if (!res.ok) {
            const text = await res.text();
            throw new Error(text || `HTTP ${res.status}`);
        }
        const data = await res.json();
        return Array.isArray(data) ? data : [];
    }

    static async getAggregated({ deviceId, sensorTypes = [], from, to, bucket = '1h' }) {
        const raw = await this.getRaw({ deviceId, sensorTypes, from, to, order: 'asc', limit: 10000 });
        const ms = SensorDataQuery.parseBucketToMs(bucket);
        const groups = new Map();
        for (const row of raw) {
            const t = new Date(row.timestamp).getTime();
            if (!Number.isFinite(t)) continue;
            const b = Math.floor(t / ms) * ms;
            const key = String(b);
            const g = groups.get(key) || { bucket: new Date(b).toISOString(), values: [] };
            g.values.push(Number(row.value));
            groups.set(key, g);
        }
        const out = [];
        for (const g of groups.values()) {
            const vals = g.values.filter(v => Number.isFinite(v));
            if (!vals.length) continue;
            const sum = vals.reduce((a, b) => a + b, 0);
            const min = Math.min(...vals);
            const max = Math.max(...vals);
            out.push({ bucket: g.bucket, avg: sum / vals.length, min, max, count: vals.length });
        }
        out.sort((a, b) => new Date(a.bucket) - new Date(b.bucket));
        return out;
    }

    static parseBucketToMs(bucket) {
        const s = String(bucket || '').trim().toLowerCase();
        if (/^\d+ms$/.test(s)) return parseInt(s);
        if (/^\d+s$/.test(s)) return parseInt(s) * 1000;
        if (/^\d+m$/.test(s)) return parseInt(s) * 60 * 1000;
        if (/^\d+h$/.test(s)) return parseInt(s) * 60 * 60 * 1000;
        if (/^\d+d$/.test(s)) return parseInt(s) * 24 * 60 * 60 * 1000;
        return 60 * 60 * 1000;
    }
}

// 最新值快照管理（用于概览与图表初始值）
class SensorLatestManager {
    static get tableName() {
        return 'sensor_latest';
    }

    static async upsertLatest({ deviceId, sensorType, value, unit, timestamp }) {
        const payload = {
            device_id: deviceId,
            sensor_type: sensorType,
            value,
            unit: unit || '',
            timestamp: timestamp || new Date().toISOString(),
            updated_at: new Date().toISOString()
        };
        // 依赖唯一键 (device_id, sensor_type)
        const { data, error } = await supabase
            .from(this.tableName)
            .upsert(payload, { onConflict: 'device_id,sensor_type' })
            .select();
        if (error) throw error;
        return data;
    }

    static async getLatestByTypes(sensorTypes = []) {
        // 读取快照表，针对别名集取时间最新的一条
        const query = supabase
            .from(this.tableName)
            .select('*')
            .in('sensor_type', sensorTypes)
            .order('timestamp', { ascending: false })
            .limit(1);
        const { data, error } = await query;
        if (error) throw error;
        return data || [];
    }
}

// 设备控制历史管理类
class DeviceControlHistoryManager {
    // 插入控制历史
    static async insertControlHistory(controlData) {
        try {
            const { v4: uuidv4 } = require('crypto');
            
            const newHistory = {
                id: uuidv4(),
                device_id: controlData.deviceId,
                command: controlData.command,
                parameters: controlData.parameters || {},
                status: controlData.status || 'pending',
                response: controlData.response || null,
                timestamp: controlData.timestamp || new Date().toISOString(),
                created_at: new Date().toISOString()
            };

            const { data, error } = await supabase
                .from('device_control_history')
                .insert([newHistory])
                .select()
                .single();
            
            if (error) {
                console.error('插入控制历史失败:', error);
                throw error;
            }
            
            console.log(`📝 记录控制历史: ${controlData.command} (设备: ${controlData.deviceId})`);
            return data;
        } catch (error) {
            console.error('DeviceControlHistoryManager.insertControlHistory 错误:', error);
            throw error;
        }
    }

    // 更新控制历史状态
    static async updateControlHistory(historyId, status, response = null) {
        try {
            const updateData = {
                status,
                response,
                updated_at: new Date().toISOString()
            };

            const { data, error } = await supabase
                .from('device_control_history')
                .update(updateData)
                .eq('id', historyId)
                .select()
                .single();
            
            if (error) {
                console.error('更新控制历史失败:', error);
                throw error;
            }
            
            console.log(`✅ 更新控制历史状态: ${status} (ID: ${historyId})`);
            return data;
        } catch (error) {
            console.error('DeviceControlHistoryManager.updateControlHistory 错误:', error);
            throw error;
        }
    }
}

module.exports = {
    supabase,
    DeviceManager,
    SensorDataManager,
    SensorDataQuery,
    SensorLatestManager,
    DeviceControlHistoryManager,
    testSupabaseConnection
};
