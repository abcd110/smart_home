package com.example.smarthome.supabase;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Single;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Supabase客户端，用于与Supabase数据库和中间件通信
 */
public class SupabaseClient {
    private static final String TAG = "SupabaseClient";
    private static SupabaseClient instance;
    private static final String SUPABASE_URL = "https://znarfgnwmbsawgndeuzh.supabase.co";
    private static final String SUPABASE_ANON_KEY = "sb_publishable_MMGYn93wCO4nsFuAWIzWNw_IaFHMO4W";
    
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final Context context;

    private SupabaseClient(Context context) {
        this.context = context;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request.Builder builder = original.newBuilder()
                            .addHeader("apikey", SUPABASE_ANON_KEY)
                            .addHeader("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                            .addHeader("Content-Type", "application/json");
                    
                    return chain.proceed(builder.build());
                })
                .build();
    }

    public static synchronized SupabaseClient getInstance(Context context) {
        if (instance == null) {
            instance = new SupabaseClient(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * 根据ID获取设备
     * @param deviceId 设备ID
     * @return 设备JSON字符串
     */
    public Single<String> getDeviceById(String deviceId) {
        return Single.create(emitter -> {
            try {
                String url = SUPABASE_URL + "/rest/v1/devices?id=eq." + deviceId + "&select=*";
                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();
                
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        Log.d(TAG, "获取设备成功: " + responseBody);
                        emitter.onSuccess(responseBody);
                    } else {
                        String error = "HTTP " + response.code() + ": " + response.message();
                        Log.e(TAG, "获取设备失败: " + error);
                        emitter.onError(new IOException(error));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "获取设备异常: " + e.getMessage(), e);
                emitter.onError(e);
            }
        });
    }

    /**
     * 发送MQTT命令
     * @param deviceId 设备ID
     * @param command 命令
     * @return 响应结果
     */
    public Single<String> sendMqttCommand(String deviceId, String command) {
        return Single.create(emitter -> {
            try {
                String url = SUPABASE_URL + "/rest/v1/mqtt_commands";
                
                Map<String, Object> commandData = new HashMap<>();
                commandData.put("device_id", deviceId);
                commandData.put("command", command);
                commandData.put("timestamp", System.currentTimeMillis());
                commandData.put("status", "pending");
                
                String jsonBody = gson.toJson(commandData);
                RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
                
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        Log.d(TAG, "MQTT命令发送成功: " + responseBody);
                        emitter.onSuccess(responseBody);
                    } else {
                        String error = "HTTP " + response.code() + ": " + response.message();
                        Log.e(TAG, "MQTT命令发送失败: " + error);
                        emitter.onError(new IOException(error));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "MQTT命令发送异常: " + e.getMessage(), e);
                emitter.onError(e);
            }
        });
    }

    /**
     * 更新设备信息
     * @param deviceId 设备ID
     * @param updateData 更新数据
     * @return 响应结果
     */
    public Single<String> updateDevice(String deviceId, Map<String, Object> updateData) {
        return Single.create(emitter -> {
            try {
                String url = SUPABASE_URL + "/rest/v1/devices?id=eq." + deviceId;
                
                String jsonBody = gson.toJson(updateData);
                RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
                
                Request request = new Request.Builder()
                        .url(url)
                        .patch(body)
                        .build();
                
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        Log.d(TAG, "设备更新成功: " + responseBody);
                        emitter.onSuccess(responseBody);
                    } else {
                        String error = "HTTP " + response.code() + ": " + response.message();
                        Log.e(TAG, "设备更新失败: " + error);
                        emitter.onError(new IOException(error));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "设备更新异常: " + e.getMessage(), e);
                emitter.onError(e);
            }
        });
    }

    /**
     * 获取设备列表
     * @return 设备列表JSON
     */
    public Single<String> getDevices() {
        return Single.create(emitter -> {
            try {
                String url = SUPABASE_URL + "/rest/v1/devices?select=*";
                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();
                
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        Log.d(TAG, "获取设备列表成功: " + responseBody);
                        emitter.onSuccess(responseBody);
                    } else {
                        String error = "HTTP " + response.code() + ": " + response.message();
                        Log.e(TAG, "获取设备列表失败: " + error);
                        emitter.onError(new IOException(error));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "获取设备列表异常: " + e.getMessage(), e);
                emitter.onError(e);
            }
        });
    }

    /**
     * 删除设备
     * @param deviceId 设备ID
     * @return 响应结果
     */
    public Single<String> deleteDevice(String deviceId) {
        return Single.create(emitter -> {
            try {
                String url = SUPABASE_URL + "/rest/v1/devices?id=eq." + deviceId;
                Request request = new Request.Builder()
                        .url(url)
                        .delete()
                        .build();
                
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        Log.d(TAG, "删除设备成功: " + responseBody);
                        emitter.onSuccess(responseBody);
                    } else {
                        String error = "HTTP " + response.code() + ": " + response.message();
                        Log.e(TAG, "删除设备失败: " + error);
                        emitter.onError(new IOException(error));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "删除设备异常: " + e.getMessage(), e);
                emitter.onError(e);
            }
        });
    }

    /**
     * 获取传感器历史数据
     * @param deviceId 设备ID
     * @param limit 返回数据条数限制
     * @return 历史数据JSON
     */
    public Single<String> getSensorHistory(String deviceId, int limit) {
        return Single.create(emitter -> {
            try {
                String url = SUPABASE_URL + "/rest/v1/sensor_data?device_id=eq." + deviceId + "&order=timestamp.desc&limit=" + limit;
                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();
                
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        Log.d(TAG, "获取传感器历史数据成功: " + responseBody);
                        emitter.onSuccess(responseBody);
                    } else {
                        String error = "HTTP " + response.code() + ": " + response.message();
                        Log.e(TAG, "获取传感器历史数据失败: " + error);
                        emitter.onError(new IOException(error));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "获取传感器历史数据异常: " + e.getMessage(), e);
                emitter.onError(e);
            }
        });
    }

    /**
     * 清理资源
     */
    public void cleanup() {
        if (httpClient != null) {
            httpClient.dispatcher().cancelAll();
        }
    }
}