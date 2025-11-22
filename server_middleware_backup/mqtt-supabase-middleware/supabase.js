const { createClient } = require('@supabase/supabase-js');
require('dotenv').config({ path: __dirname + '/.env' });

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
console.log('   - Key前缀:', (supabaseServiceKey||supabaseKey)?.substring(0, 20) + '...');

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
                .from('devices')
                .select('*')
                .limit(1);
            
            if (error) {
                console.error('❌ 数据库表访问失败:', error);
                console.log('💡 可能的原因:');
                console.log('   1. 数据库表未创建');
                console.log('   2. API密钥权限不足');
                console.log('   3. 网络连接问题');
                return false;
            }
            
            console.log('✅ 数据库表访问成功');
            console.log('📊 当前设备数量:', data?.length || 0);
            return true;
            
        } catch (tableError) {
            console.error('❌ 数据库表访问异常:', tableError);
            return false;
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
            function toISOUTC(ms) { return new Date(ms).toISOString(); }
            const baseMillis = (()=>{
                if (typeof sensorData.timestamp === 'string') {
                    const p = Date.parse(sensorData.timestamp);
                    if (!Number.isNaN(p)) return p;
                }
                if (typeof sensorData.timestamp === 'number') {
                    const n = sensorData.timestamp;
                    return n > 1e12 ? n : n * 1000;
                }
                const tsNum = typeof sensorData.ts === 'number' ? sensorData.ts : (typeof sensorData.ts === 'string' ? Number(sensorData.ts) : NaN);
                if (Number.isFinite(tsNum)) {
                    const millis = tsNum > 1e12 ? tsNum : tsNum * 1000;
                    if (millis >= Date.UTC(2000,0,1)) return millis; // sanity check
                }
                return Date.now();
            })();
            const newSensorData = {
                device_id: sensorData.deviceId,
                sensor_type: sensorData.sensorType,
                value: sensorData.value,
                unit: sensorData.unit || '',
                timestamp: toISOUTC(baseMillis),
                created_at: toISOUTC(Date.now())
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
            if (sensorData.sensorType === 'gas') {
                const level = sensorData.value >= 3000 ? 'high' : sensorData.value >= 2000 ? 'medium' : 'low';
                console.log(`   气体等级: ${level}`);
            }
            const up = {
                device_id: sensorData.deviceId,
                sensor_type: sensorData.sensorType,
                value: sensorData.value,
                unit: sensorData.unit || '',
                is_alert: false,
                timestamp: toISOUTC(baseMillis),
                updated_at: toISOUTC(Date.now())
            };
            const r2 = await supabase.from('sensor_latest').upsert(up, { onConflict: 'device_id,sensor_type' }).select().single();
            if (r2.error) {
                console.error('更新最新传感器数据失败:', r2.error);
            }
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

// 设备控制历史管理类
class DeviceControlHistoryManager {
    // 插入控制历史
    static async insertControlHistory(controlData) {
        try {
            const { randomUUID } = require('crypto');
            
            const newHistory = {
                id: (typeof randomUUID === 'function') ? randomUUID() : (Date.now().toString(36) + Math.random().toString(36).slice(2)),
                device_id: controlData.deviceId,
                command: controlData.command,
                parameters: controlData.parameters || {},
                status: controlData.status || 'pending',
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
    DeviceControlHistoryManager,
    testSupabaseConnection,
    SensorLatestManager: {
        async upsertLatest(rec) {
            const ms = (()=>{
                if (typeof rec.timestamp === 'string') { const p = Date.parse(rec.timestamp); if (!Number.isNaN(p)) return p; }
                if (typeof rec.timestamp === 'number') { const n = rec.timestamp; return n>1e12?n:n*1000; }
                const tsn = typeof rec.ts === 'number'?rec.ts:(typeof rec.ts==='string'?Number(rec.ts):NaN);
                return Number.isFinite(tsn)?(tsn>1e12?tsn:tsn*1000):Date.now();
            })();
            const up = {
                device_id: rec.deviceId,
                sensor_type: rec.sensorType,
                value: rec.value,
                unit: rec.unit || '',
                is_alert: false,
                timestamp: new Date(ms).toISOString(),
                updated_at: new Date().toISOString()
            };
            return supabase.from('sensor_latest').upsert(up, { onConflict: 'device_id,sensor_type' });
        }
    }
};