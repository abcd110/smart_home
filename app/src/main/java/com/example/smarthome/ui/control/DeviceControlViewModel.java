package com.example.smarthome.ui.control;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.smarthome.model.Device;
import com.example.smarthome.supabase.SupabaseClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 设备控制页面的ViewModel，处理设备控制的业务逻辑
 */
public class DeviceControlViewModel extends AndroidViewModel {

    private static final String TAG = "DeviceControlViewModel";
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final SupabaseClient supabaseClient;
    private final MutableLiveData<Device> device = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> statusMessage = new MutableLiveData<>();
    private final MutableLiveData<Integer> brightnessLive = new MutableLiveData<>(70);
    private final MutableLiveData<String> colorTempLive = new MutableLiveData<>("natural");
    private final MutableLiveData<String> powerLive = new MutableLiveData<>("ON");
    private com.example.smarthome.utils.LightStateRepository lightRepo;
    private final Gson gson = new Gson();

    public DeviceControlViewModel(@NonNull Application application) {
        super(application);
        this.supabaseClient = SupabaseClient.getInstance(application);
        this.lightRepo = new com.example.smarthome.utils.LightStateRepository(application);
        com.example.smarthome.utils.MqttBridge.getInstance().addLightStatusListener((id,b,c,p)->{
            Device d = device.getValue();
            if (d!=null && id!=null && id.equals(d.getDeviceId())){
                if (b>=0){ brightnessLive.postValue(b); lightRepo.setBrightness(id,b);} 
                if (c!=null){ colorTempLive.postValue(c); lightRepo.setColorTemp(id,c);} 
                if (p!=null){ powerLive.postValue(p); lightRepo.setPower(id,p);} 
            }
        });
    }

    /**
     * 获取设备LiveData
     * @return 设备LiveData
     */
    public LiveData<Device> getDevice() {
        return device;
    }

    /**
     * 获取错误消息LiveData
     * @return 错误消息LiveData
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * 获取状态消息LiveData
     * @return 状态消息LiveData
     */
    public LiveData<String> getStatusMessage() {
        return statusMessage;
    }

    public LiveData<Integer> getBrightnessLive(){ return brightnessLive; }
    public LiveData<String> getColorTempLive(){ return colorTempLive; }
    public LiveData<String> getPowerLive(){ return powerLive; }

    /**
     * 根据ID加载设备数据
     * @param deviceId 设备ID
     */
    public void loadDeviceById(String deviceId) {
        disposables.add(
                supabaseClient.getDeviceById(deviceId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    try {
                                        com.google.gson.JsonElement root = com.google.gson.JsonParser.parseString(response);
                                        if (root.isJsonArray()) {
                                            com.google.gson.JsonArray arr = root.getAsJsonArray();
                                            if (arr.size() > 0) {
                                                com.google.gson.JsonObject obj = arr.get(0).getAsJsonObject();
                                                Device d = gson.fromJson(obj, Device.class);
                                                String status = obj.has("status") && !obj.get("status").isJsonNull() ? obj.get("status").getAsString() : null;
                                                if (status != null) d.setOnline("online".equalsIgnoreCase(status));
                                                if (obj.has("is_active") && !obj.get("is_active").isJsonNull()) d.setOn(obj.get("is_active").getAsBoolean());
                                                device.setValue(d);
                                                // 应用记忆
                                                brightnessLive.setValue(lightRepo.getBrightness(d.getDeviceId()));
                                                colorTempLive.setValue(lightRepo.getColorTemp(d.getDeviceId()));
                                                powerLive.setValue(lightRepo.getPower(d.getDeviceId()));
                                            } else {
                                                errorMessage.setValue("未找到设备");
                                            }
                                        } else {
                                            Device d = gson.fromJson(root, Device.class);
                                            device.setValue(d);
                                            brightnessLive.setValue(lightRepo.getBrightness(d.getDeviceId()));
                                            colorTempLive.setValue(lightRepo.getColorTemp(d.getDeviceId()));
                                            powerLive.setValue(lightRepo.getPower(d.getDeviceId()));
                                        }
                                        statusMessage.setValue("设备数据加载成功");
                                } catch (Exception e) {
                                    Log.e(TAG, "解析设备数据失败: " + e.getMessage());
                                    errorMessage.setValue("设备数据解析失败");
                                }
                                },
                                throwable -> {
                                    Log.e(TAG, "加载设备失败: " + throwable.getMessage());
                                    errorMessage.setValue("加载设备失败: " + throwable.getMessage());
                                }
                        )
        );
    }

    /**
     * 切换设备状态
     * @param device 设备对象
     * @param isChecked 是否开启
     */
    public void toggleDeviceStatus(Device device, boolean isChecked) {
        // 更新本地状态
        device.setOn(isChecked);
        device.setLastActiveTime(new Date());
        device.setUpdatedAt(new Date());
        this.device.setValue(device);

        // 准备更新数据
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("is_on", isChecked);
        updateData.put("last_active_time", device.getLastActiveTime());
        updateData.put("updated_at", device.getUpdatedAt());

        // 发送MQTT命令
        sendDeviceCommand(device, isChecked ? "ON" : "OFF");

        // 更新Supabase数据库
        updateDeviceInDatabase(device.getId(), updateData);
    }

    /**
     * 发送设备控制命令
     * @param device 设备对象
     * @param command 命令内容
     */
    private void sendDeviceCommand(Device device, String command) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("command", "power");
        payload.put("value", command);
        disposables.add(
                supabaseClient.sendMqttCommand(device.getDeviceId(), payload)
                        .subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.io())
                        .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
                        .subscribe(
                                resp -> statusMessage.setValue("控制命令已发送"),
                                err -> errorMessage.setValue("命令发送失败，请重试")
                        )
        );
    }

    /**
     * 在数据库中更新设备信息
     * @param deviceId 设备ID
     * @param updateData 更新数据
     */
    private void updateDeviceInDatabase(String deviceId, Map<String, Object> updateData) {
        disposables.add(
                supabaseClient.updateDevice(deviceId, updateData)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    Log.d(TAG, "设备数据库更新成功");
                                },
                                throwable -> {
                                    Log.e(TAG, "设备数据库更新失败: " + throwable.getMessage());
                                    // 数据库更新失败不影响UI状态，仅记录日志
                                }
                        )
        );
    }

    /**
     * 刷新设备状态
     * @param deviceId 设备ID
     */
    public void refreshDeviceStatus(String deviceId) {
        loadDeviceById(deviceId);
    }

    /**
     * 执行设备特定操作
     * @param device 设备对象
     * @param actionType 操作类型
     */
    public void performDeviceAction(Device device, int actionType) {
        // 根据设备类型和操作类型执行不同的操作
        String command = ""; 
        String actionDescription = "";

        switch (actionType) {
            case 1: // 第一个按钮操作
                if (device.isSensorType()) {
                    viewSensorHistory(device);
                    return;
                } else if ("light".equals(device.getDeviceType()) || "灯具".equals(device.getDeviceType())) {
                    command = "BRIGHTNESS_SET";
                    actionDescription = "亮度调节";
                } else if ("outlet".equals(device.getDeviceType()) || "插座".equals(device.getDeviceType())) {
                    command = "POWER_CONSUMPTION";
                    actionDescription = "查看功耗";
                } else if ("curtain".equals(device.getDeviceType()) || "窗帘".equals(device.getDeviceType())) {
                    command = "OPEN";
                    actionDescription = "打开窗帘";
                } else if ("buzzer".equals(device.getDeviceType()) || "蜂鸣器".equals(device.getDeviceType())) {
                    command = "BUZZ_ON";
                    actionDescription = "响铃";
                } else if ("humidifier".equals(device.getDeviceType()) || "加湿器".equals(device.getDeviceType())) {
                    command = "HUMIDIFY_ON";
                    actionDescription = "开启加湿";
                }
                break;
            case 2: // 第二个按钮操作
                if ("light".equals(device.getDeviceType()) || "灯具".equals(device.getDeviceType())) {
                    command = "COLOR_SET";
                    actionDescription = "颜色设置";
                } else if ("outlet".equals(device.getDeviceType()) || "插座".equals(device.getDeviceType())) {
                    command = "TIMER_SET";
                    actionDescription = "定时设置";
                } else if ("curtain".equals(device.getDeviceType()) || "窗帘".equals(device.getDeviceType())) {
                    command = "CLOSE";
                    actionDescription = "关闭窗帘";
                } else if ("buzzer".equals(device.getDeviceType()) || "蜂鸣器".equals(device.getDeviceType())) {
                    command = "BUZZ_OFF";
                    actionDescription = "停止响铃";
                } else if ("humidifier".equals(device.getDeviceType()) || "加湿器".equals(device.getDeviceType())) {
                    command = "HUMIDIFY_OFF";
                    actionDescription = "关闭加湿";
                }
                break;
            case 3: // 第三个按钮操作
                if ("light".equals(device.getDeviceType()) || "灯具".equals(device.getDeviceType())) {
                    command = "TIMER_SET";
                    actionDescription = "定时设置";
                } else if ("curtain".equals(device.getDeviceType()) || "窗帘".equals(device.getDeviceType())) {
                    command = "POSITION_SET";
                    actionDescription = "位置设置";
                } else if ("buzzer".equals(device.getDeviceType()) || "蜂鸣器".equals(device.getDeviceType())) {
                    command = "BUZZ_SCHEDULE";
                    actionDescription = "定时响铃";
                } else if ("humidifier".equals(device.getDeviceType()) || "加湿器".equals(device.getDeviceType())) {
                    command = "HUMIDIFY_SCHEDULE";
                    actionDescription = "定时加湿";
                }
                break;
        }

        if (!command.isEmpty()) {
            sendDeviceCommand(device, command);
            statusMessage.setValue("" + actionDescription + "已触发");
        }
    }

    public void setLightBrightness(Device device, int value) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("command", "BRIGHTNESS_SET");
        payload.put("value", Math.max(0, Math.min(100, value)));
        sendPayload(device, payload, "亮度已设置");
        brightnessLive.setValue(Math.max(0, Math.min(100, value)));
        lightRepo.setBrightness(device.getDeviceId(), Math.max(0, Math.min(100, value)));
    }

    public void setLightColor(Device device, String colorHex) {
        String hex = colorHex == null ? "#FFFFFF" : colorHex.trim();
        Map<String, Object> payload = new HashMap<>();
        payload.put("command", "COLOR_SET");
        payload.put("value", hex);
        sendPayload(device, payload, "颜色已设置");
    }

    public void setLightColorPreset(Device device, String preset) {
        String p = preset == null ? "natural" : preset.toLowerCase();
        String hex;
        switch (p) {
            case "warm":
            case "暖光":
                hex = "#FFC107"; // 暖光
                break;
            case "cool":
            case "冷光":
                hex = "#64B5F6"; // 冷光
                break;
            case "natural":
            case "自然光":
            default:
                hex = "#FFFFFF"; // 自然光
                break;
        }
        setLightColor(device, hex);
        colorTempLive.setValue(p);
        lightRepo.setColorTemp(device.getDeviceId(), p);
    }

    public void setTimer(Device device, String atISO, String action) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("command", "TIMER_SET");
        payload.put("at", atISO);
        payload.put("action", action);
        sendPayload(device, payload, "定时已设置");
    }

    public void buzzerScheduleAt(Device device, int hour, int minute, int durationSec) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("command", "BUZZ_SCHEDULE");
        payload.put("hour", Math.max(0, Math.min(23, hour)));
        payload.put("minute", Math.max(0, Math.min(59, minute)));
        payload.put("duration", Math.max(1, durationSec));
        sendPayload(device, payload, "蜂鸣器定时已设置");
    }

    public void buzzerOn(Device device) { sendSimple(device, "BUZZ_ON", "蜂鸣器已开启"); }
    public void buzzerOff(Device device) { sendSimple(device, "BUZZ_OFF", "蜂鸣器已停止"); }
    public void buzzerSchedule(Device device, String cron) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("command", "BUZZ_SCHEDULE");
        payload.put("cron", cron);
        sendPayload(device, payload, "蜂鸣器定时已设置");
    }

    public void humidifierOn(Device device) { sendSimple(device, "HUMIDIFY_ON", "加湿已开启"); }
    public void humidifierOff(Device device) { sendSimple(device, "HUMIDIFY_OFF", "加湿已关闭"); }
    public void humidifierSchedule(Device device, int seconds) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("command", "HUMIDIFY_SCHEDULE");
        payload.put("duration", Math.max(1, seconds));
        sendPayload(device, payload, "加湿定时已设置");
    }

    public void curtainOpen(Device device) { sendSimple(device, "OPEN", "窗帘已打开"); }
    public void curtainClose(Device device) { sendSimple(device, "CLOSE", "窗帘已关闭"); }
    public void curtainPosition(Device device, int percent) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("command", "POSITION_SET");
        payload.put("value", Math.max(0, Math.min(100, percent)));
        sendPayload(device, payload, "窗帘位置已设置");
    }

    public void pingDevice(Device device) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("command", "ping");
        payload.put("value", "app");
        sendPayload(device, payload, "设备通信测试已发送");
    }

    private void sendSimple(Device device, String command, String successMsg) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("command", command);
        payload.put("value", command);
        disposables.add(
                supabaseClient.sendMqttCommand(device.getDeviceId(), payload)
                        .subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.io())
                        .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
                        .subscribe(
                                resp -> statusMessage.setValue(successMsg),
                                err -> errorMessage.setValue("命令发送失败")
                        )
        );
    }

    private void sendPayload(Device device, Map<String, Object> payload, String successMsg) {
        disposables.add(
                supabaseClient.sendMqttCommand(device.getDeviceId(), payload)
                        .subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.io())
                        .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
                        .subscribe(
                                resp -> statusMessage.setValue(successMsg),
                                err -> errorMessage.setValue("命令发送失败")
                        )
        );
    }

    /**
     * 查看传感器历史数据
     * @param device 设备对象
     */
    public void viewSensorHistory(Device device) {
        // 这里可以导航到历史数据页面或显示历史数据图表
        statusMessage.setValue("加载传感器历史数据...");
        
        // 获取传感器历史数据
        disposables.add(
                supabaseClient.getSensorHistoryRaw(device.getDeviceId(), "temperature", null, null, "desc", 100)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    Log.d(TAG, "获取传感器历史数据成功，共 " + response.length() + " 条记录");
                                    // 这里可以处理历史数据，如显示图表等
                                    statusMessage.setValue("历史数据已加载");
                                },
                                throwable -> {
                                    Log.e(TAG, "获取传感器历史数据失败: " + throwable.getMessage());
                                    errorMessage.setValue("获取历史数据失败");
                                }
                        )
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // 清理所有订阅，防止内存泄漏
        disposables.clear();
    }
}
