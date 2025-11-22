package com.example.smarthome.ui.home;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.smarthome.model.DeviceItem;
import com.example.smarthome.model.SensorSummary;
import com.example.smarthome.supabase.SupabaseClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.example.smarthome.supabase.SupabaseClient;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 首页ViewModel
 * 管理设备数据和业务逻辑
 */
public class HomeViewModel extends AndroidViewModel {

    private static final String TAG = "HomeViewModel";
    private final MutableLiveData<List<DeviceItem>> deviceListLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<SensorSummary> sensorSummaryLiveData = new MutableLiveData<>();
    private final MutableLiveData<java.util.List<com.example.smarthome.model.SecurityEvent>> securityEventsLiveData = new MutableLiveData<>(new java.util.ArrayList<>());
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private final CompositeDisposable disposables = new CompositeDisposable();

    public HomeViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * 获取设备列表LiveData
     */
    public MutableLiveData<List<DeviceItem>> getDeviceList() {
        return deviceListLiveData;
    }

    /**
     * 获取加载状态LiveData
     */
    public MutableLiveData<Boolean> getLoading() {
        return loadingLiveData;
    }

    /**
     * 获取错误信息LiveData
     */
    public MutableLiveData<String> getError() {
        return errorLiveData;
    }

    public MutableLiveData<SensorSummary> getSensorSummary() {
        return sensorSummaryLiveData;
    }
    public MutableLiveData<java.util.List<com.example.smarthome.model.SecurityEvent>> getSecurityEvents() {
        return securityEventsLiveData;
    }

    /**
     * 加载设备列表
     */
    public void loadDevices() {
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);
        SupabaseClient client = SupabaseClient.getInstance(getApplication());
        io.reactivex.rxjava3.schedulers.Schedulers.io().scheduleDirect(() -> {
            try {
                String json = client.getDevices().blockingGet();
                List<DeviceItem> devices = new ArrayList<>();
                Gson gson = new Gson();
                JsonElement root = com.google.gson.JsonParser.parseString(json);
                if (root.isJsonArray()) {
                    JsonArray arr = root.getAsJsonArray();
                    for (JsonElement el : arr) {
                        JsonObject obj = el.getAsJsonObject();
                        DeviceItem item = new DeviceItem();
                        String deviceId = getStringSafe(obj, "device_id");
                        if (deviceId == null) deviceId = getStringSafe(obj, "id");
                        item.setDeviceId(deviceId);
                        item.setName(getStringSafe(obj, "name"));
                        String type = getStringSafe(obj, "device_type");
                        if (type == null) type = getStringSafe(obj, "type");
                        item.setType(type);
                        // 在线：优先使用 status==online；否则回退 is_online
                        boolean online = false;
                        String status = getStringSafe(obj, "status");
                        if (status != null) online = "online".equalsIgnoreCase(status);
                        else online = getBooleanSafe(obj, "is_online");
                        item.setOnline(online);

                        // 运行：优先 is_active；否则 is_on；否则 active
                        boolean active = getBooleanSafe(obj, "is_active");
                        if (!active) active = getBooleanSafe(obj, "is_on");
                        if (!active) active = getBooleanSafe(obj, "active");
                        item.setActive(active);
                        item.setStatus(item.isActive() ? "开启" : "关闭");
                        item.setLastUpdateTime(System.currentTimeMillis());
                        item.setValue(obj.has("latest_sensor_data") && !obj.get("latest_sensor_data").isJsonNull() ? obj.get("latest_sensor_data").getAsString() : null);
                        devices.add(item);
                    }
                }
                deviceListLiveData.postValue(devices);
                loadingLiveData.postValue(false);
            } catch (Exception e) {
                Log.e(TAG, "加载设备失败: " + e.getMessage());
                errorLiveData.postValue("加载设备失败: " + e.getMessage());
                loadingLiveData.postValue(false);
            }
        });
    }

    /**
     * 加载传感器概览（温度/湿度/煤气）
     */
    public void loadSensorSummary() {
        SupabaseClient client = SupabaseClient.getInstance(getApplication());
        executorService.execute(() -> {
            try {
                Double temp = null, hum = null, gas = null;

                // 优先使用中间件的概览接口（一次性返回三项），失败时回退到逐项查询
                boolean summaryOk = false;
                try {
                    String summaryJson = client.getSensorSummary().blockingGet();
                    com.google.gson.JsonObject root = com.google.gson.JsonParser.parseString(summaryJson).getAsJsonObject();
                    if (root.has("temperature")) temp = parseLatestValue(root.get("temperature").toString());
                    if (root.has("humidity")) hum = parseLatestValue(root.get("humidity").toString());
                    if (root.has("gas")) gas = parseLatestValue(root.get("gas").toString());
                    summaryOk = true;
                } catch (Exception se) {
                    Log.e(TAG, "中间件概览获取失败: " + se.getMessage());
                }

                if (!summaryOk) {
                    StringBuilder sb = new StringBuilder();
                    try {
                        String j = client.getLatestSensorValue("temperature").blockingGet();
                        temp = parseLatestValue(j);
                        Log.d(TAG, "温度最新值=" + temp);
                    } catch (Exception te) {
                        Log.e(TAG, "温度获取失败: " + te.getMessage());
                        sb.append("温度失败: ").append(te.getMessage()).append("; ");
                    }
                    try {
                        String j = client.getLatestSensorValue("humidity").blockingGet();
                        hum = parseLatestValue(j);
                        Log.d(TAG, "湿度最新值=" + hum);
                    } catch (Exception he) {
                        Log.e(TAG, "湿度获取失败: " + he.getMessage());
                        sb.append("湿度失败: ").append(he.getMessage()).append("; ");
                    }
                    try {
                        String j = client.getLatestSensorValue("gas").blockingGet();
                        gas = parseLatestValue(j);
                        Log.d(TAG, "煤气最新值=" + gas);
                    } catch (Exception ge) {
                        Log.e(TAG, "煤气获取失败: " + ge.getMessage());
                        sb.append("煤气失败: ").append(ge.getMessage()).append("; ");
                    }
                    if (sb.length() > 0) errorLiveData.postValue("环境概览加载异常: " + sb.toString());
                }

                sensorSummaryLiveData.postValue(new SensorSummary(temp, hum, gas));
            } catch (Exception e) {
                Log.e(TAG, "环境概览加载失败: " + e.getMessage());
                errorLiveData.postValue("加载环境概览失败: " + e.getMessage());
            }
        });
    }

    public void startSse() {
        io.reactivex.rxjava3.schedulers.Schedulers.io().scheduleDirect(() -> {
            try {
                String url = com.example.smarthome.supabase.SupabaseClient.getMiddlewareBaseUrl() + "/events";
                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                        .readTimeout(0, java.util.concurrent.TimeUnit.MILLISECONDS)
                        .build();
                okhttp3.Request req = new okhttp3.Request.Builder().url(url).get().build();
                okhttp3.Response resp = client.newCall(req).execute();
                if (resp.isSuccessful() && resp.body() != null) {
                    okio.BufferedSource source = resp.body().source();
                    String event = null;
                    while (!source.exhausted()) {
                        String line = source.readUtf8LineStrict();
                        if (line.startsWith("event:")) { event = line.substring(6).trim(); }
                        else if (line.startsWith("data:")) {
                            String data = line.substring(5).trim();
                            handleSseEvent(event, data);
                            event = null;
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "SSE连接失败: " + e.getMessage());
            }
        });
    }

    private void handleSseEvent(String event, String data) {
        try {
            com.google.gson.JsonElement root = com.google.gson.JsonParser.parseString(data);
            if ("sensor_summary".equals(event) && root.isJsonObject()) {
                Double t = extractFirstValue(root.getAsJsonObject(), "temperature");
                Double h = extractFirstValue(root.getAsJsonObject(), "humidity");
                Double g = extractFirstValue(root.getAsJsonObject(), "gas");
                sensorSummaryLiveData.postValue(new SensorSummary(t, h, g));
            } else if ("security_event".equals(event) && root.isJsonObject()) {
                com.google.gson.JsonObject obj = root.getAsJsonObject();
                String type = obj.has("type") ? obj.get("type").getAsString() : "security";
                String status = obj.has("status") ? obj.get("status").getAsString() : null;
                String sensorType = obj.has("sensor_type") ? obj.get("sensor_type").getAsString() : null;
                String dev = obj.has("device_id") ? obj.get("device_id").getAsString() : null;
                String at = obj.has("timestamp") ? obj.get("timestamp").getAsString() : new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault()).format(new java.util.Date());
                String msg;
                if ("hall".equals(sensorType)) {
                    msg = "门磁: " + ("open".equalsIgnoreCase(status)?"打开":"关闭");
                } else {
                    msg = obj.has("message") ? obj.get("message").getAsString() : "安全事件";
                }
                java.util.List<com.example.smarthome.model.SecurityEvent> list = securityEventsLiveData.getValue();
                if (list == null) list = new java.util.ArrayList<>();
                list.add(new com.example.smarthome.model.SecurityEvent(type, msg, at, dev));
                securityEventsLiveData.postValue(list);
            } else if ("alarm_event".equals(event) && root.isJsonObject()) {
                String msg = root.getAsJsonObject().has("message") ? root.getAsJsonObject().get("message").getAsString() : "报警";
                String at = root.getAsJsonObject().has("at") ? root.getAsJsonObject().get("at").getAsString() : new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault()).format(new java.util.Date());
                String dev = root.getAsJsonObject().has("device_id") ? root.getAsJsonObject().get("device_id").getAsString() : null;
                java.util.List<com.example.smarthome.model.SecurityEvent> list = securityEventsLiveData.getValue();
                if (list == null) list = new java.util.ArrayList<>();
                list.add(new com.example.smarthome.model.SecurityEvent("alarm", msg, at, dev));
                securityEventsLiveData.postValue(list);
            }
        } catch (Exception e) { Log.e(TAG, "SSE事件处理失败: " + e.getMessage()); }
    }

    public void markSecurityEventHandled(int index) {
        java.util.List<com.example.smarthome.model.SecurityEvent> list = securityEventsLiveData.getValue();
        if (list == null || index < 0 || index >= list.size()) return;
        com.example.smarthome.model.SecurityEvent e = list.get(index);
        e.setHandled(true);
        securityEventsLiveData.postValue(list);
    }

    private Double extractFirstValue(com.google.gson.JsonObject obj, String key) {
        try {
            if (obj.has(key)) {
                com.google.gson.JsonElement arr = obj.get(key);
                if (arr.isJsonArray() && arr.getAsJsonArray().size() > 0) {
                    com.google.gson.JsonObject rec = arr.getAsJsonArray().get(0).getAsJsonObject();
                    if (rec.has("value")) return rec.get("value").getAsDouble();
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private Double parseLatestValue(String json) {
        try {
            JsonElement root = com.google.gson.JsonParser.parseString(json);
            if (root.isJsonArray()) {
                JsonArray arr = root.getAsJsonArray();
                if (arr.size() > 0) {
                    JsonObject obj = arr.get(0).getAsJsonObject();
                    if (obj.has("value") && !obj.get("value").isJsonNull()) {
                        return obj.get("value").getAsDouble();
                    }
                    Log.e(TAG, "最新记录缺少value字段: " + obj.toString());
                } else {
                    Log.e(TAG, "未找到最新传感器记录");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "解析最新传感器值失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 切换设备状态
     */
    public void toggleDeviceState(DeviceItem device, boolean isEnabled) {
        // 在实际应用中，这里应该调用API来切换设备状态
        executorService.execute(() -> {
            try {
                // 模拟网络请求
                Thread.sleep(500);
                
                // 更新本地设备状态
                List<DeviceItem> currentDevices = deviceListLiveData.getValue();
                if (currentDevices != null) {
                    for (DeviceItem item : currentDevices) {
                        if (item.getDeviceId().equals(device.getDeviceId())) {
                            item.setActive(isEnabled);
                            break;
                        }
                    }
                    
                    // 在主线程更新UI
                    deviceListLiveData.postValue(new ArrayList<>(currentDevices));
                }
            } catch (Exception e) {
                Log.e(TAG, "切换设备状态失败: " + e.getMessage());
                String errorMessage = "控制设备失败，请稍后重试";
                
                errorLiveData.postValue(errorMessage);
            }
        });
    }

    /**
     * 模拟设备数据
     */
    private List<DeviceItem> generateMockDevices() {
        List<DeviceItem> devices = new ArrayList<>();
        
        // 模拟智能灯光
        devices.add(new DeviceItem(
                "device_001",
                "客厅灯光",
                "light",
                true,
                true,
                "开启",
                System.currentTimeMillis(),
                100
        ));
        
        // 模拟空调
        devices.add(new DeviceItem(
                "device_002",
                "主卧空调",
                "air_conditioner",
                true,
                false,
                "关闭",
                System.currentTimeMillis(),
                26
        ));
        
        // 模拟温湿度传感器
        devices.add(new DeviceItem(
                "device_003",
                "客厅温湿度",
                "sensor",
                true,
                true,
                "正常",
                System.currentTimeMillis(),
                25.5
        ));
        
        // 模拟智能门锁
        devices.add(new DeviceItem(
                "device_004",
                "前门智能锁",
                "lock",
                true,
                true,
                "已锁定",
                System.currentTimeMillis(),
                0
        ));
        
        // 模拟窗帘
        devices.add(new DeviceItem(
                "device_005",
                "主卧窗帘",
                "curtain",
                true,
                false,
                "关闭",
                System.currentTimeMillis(),
                0
        ));
        
        // 模拟加湿器
        devices.add(new DeviceItem(
                "device_006",
                "客厅加湿器",
                "humidifier",
                true,
                true,
                "开启",
                System.currentTimeMillis(),
                45
        ));
        
        return devices;
    }

    private String getStringSafe(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : null;
    }

    private boolean getBooleanSafe(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() && obj.get(key).getAsBoolean();
    }

    /**
     * 添加新设备
     */
    public void addDevice(DeviceItem device) {
        executorService.execute(() -> {
            try {
                // 模拟API调用
                Thread.sleep(800);
                
                List<DeviceItem> currentDevices = deviceListLiveData.getValue();
                if (currentDevices == null) {
                    currentDevices = new ArrayList<>();
                }
                
                // 检查设备ID是否已存在
                boolean deviceExists = currentDevices.stream()
                        .anyMatch(d -> d.getDeviceId().equals(device.getDeviceId()));
                
                if (!deviceExists) {
                    currentDevices.add(device);
                    
                    deviceListLiveData.postValue(new ArrayList<>(currentDevices));
                } else {
                    throw new Exception("设备ID已存在");
                }
            } catch (Exception e) {
                Log.e(TAG, "添加设备失败: " + e.getMessage());
                String errorMessage = "添加设备失败: " + e.getMessage();
                
                errorLiveData.postValue(errorMessage);
            }
        });
    }

    /**
     * 删除设备
     */
    public void deleteDevice(String deviceId) {
        executorService.execute(() -> {
            try {
                // 模拟API调用
                Thread.sleep(600);
                
                List<DeviceItem> currentDevices = deviceListLiveData.getValue();
                if (currentDevices != null) {
                    currentDevices.removeIf(d -> d.getDeviceId().equals(deviceId));
                    
                    deviceListLiveData.postValue(new ArrayList<>(currentDevices));
                }
            } catch (Exception e) {
                Log.e(TAG, "删除设备失败: " + e.getMessage());
                String errorMessage = "删除设备失败，请稍后重试";
                
                errorLiveData.postValue(errorMessage);
            }
        });
    }

    /**
     * 更新设备信息
     */
    public void updateDevice(DeviceItem updatedDevice) {
        executorService.execute(() -> {
            try {
                // 模拟API调用
                Thread.sleep(700);
                
                List<DeviceItem> currentDevices = deviceListLiveData.getValue();
                if (currentDevices != null) {
                    for (int i = 0; i < currentDevices.size(); i++) {
                        if (currentDevices.get(i).getDeviceId().equals(updatedDevice.getDeviceId())) {
                            currentDevices.set(i, updatedDevice);
                            break;
                        }
                    }
                    
                    deviceListLiveData.postValue(new ArrayList<>(currentDevices));
                }
            } catch (Exception e) {
                Log.e(TAG, "更新设备失败: " + e.getMessage());
                String errorMessage = "更新设备失败，请稍后重试";
                
                errorLiveData.postValue(errorMessage);
            }
        });
    }

    /**
     * 定期刷新设备状态
     */
    public void startPeriodicRefresh(long intervalMs) {
        executorService.scheduleAtFixedRate(this::loadDevices, intervalMs, intervalMs, TimeUnit.MILLISECONDS);
    }

    /**
     * 停止定期刷新
     */
    public void stopPeriodicRefresh() {
        executorService.shutdown();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopPeriodicRefresh();
        disposables.clear();
    }
    public void closeAlarm(String deviceId) {
        if (deviceId == null || deviceId.isEmpty()) return;
        disposables.add(
                SupabaseClient.getInstance(getApplication())
                        .closeAlarm(deviceId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(r->{}, e->{})
        );
    }
}
