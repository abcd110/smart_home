package com.example.smarthome.supabase;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.example.smarthome.auth.AuthService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.io.UnsupportedEncodingException;

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
    private static final String MIDDLEWARE_URL = "http://8.134.63.151";
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
                    String url = original.url().toString();
                    Request.Builder builder = original.newBuilder()
                            .addHeader("Content-Type", "application/json");
                    if (url.startsWith(SUPABASE_URL)) {
                        AuthService auth = new AuthService(context);
                        String token = auth.getAccessToken();
                        String authHeader = token != null ? ("Bearer " + token) : ("Bearer " + SUPABASE_ANON_KEY);
                        builder.addHeader("apikey", SUPABASE_ANON_KEY)
                               .addHeader("Authorization", authHeader);
                    }
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

    public static String getMiddlewareBaseUrl() { return MIDDLEWARE_URL; }

    public Single<String> closeAlarm(String deviceId) {
        return Single.create(emitter -> {
            try {
                String url = MIDDLEWARE_URL + "/alarm/" + deviceId + "/close";
                RequestBody body = RequestBody.create("{}", MediaType.parse("application/json"));
                Request req = new Request.Builder().url(url).post(body).build();
                try (Response resp = httpClient.newCall(req).execute()) {
                    if (resp.isSuccessful()) {
                        String r = resp.body()!=null?resp.body().string():"";
                        emitter.onSuccess(r);
                    } else {
                        emitter.onError(new IOException("HTTP " + resp.code()));
                    }
                }
            } catch (Exception e) { emitter.onError(e); }
        });
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
                String mwUrl = MIDDLEWARE_URL + "/devices/" + deviceId + "/control";
                Map<String, Object> commandData = new HashMap<>();
                commandData.put("command", command);
                String jsonBody = gson.toJson(commandData);
                RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
                Request mwReq = new Request.Builder().url(mwUrl).post(body).build();
                try (Response mwResp = httpClient.newCall(mwReq).execute()) {
                    if (mwResp.isSuccessful()) {
                        String resp = mwResp.body() != null ? mwResp.body().string() : "";
                        Log.d(TAG, "MQTT命令通过中间件发送成功: " + resp);
                        emitter.onSuccess(resp);
                        return;
                    }
                }

                // 移除 Supabase 备份链路，避免 401 干扰控制流程
                emitter.onError(new IOException("中间件控制失败"));
            } catch (Exception e) {
                Log.e(TAG, "MQTT命令发送异常: " + e.getMessage(), e);
                emitter.onError(e);
            }
        });
    }

    public Single<String> sendMqttCommand(String deviceId, Map<String, Object> payload) {
        return Single.create(emitter -> {
            try {
                String mwUrl = MIDDLEWARE_URL + "/devices/" + deviceId + "/control";
                String jsonBody = gson.toJson(payload);
                RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
                Request mwReq = new Request.Builder().url(mwUrl).post(body).build();
                try (Response mwResp = httpClient.newCall(mwReq).execute()) {
                    if (mwResp.isSuccessful()) {
                        String resp = mwResp.body() != null ? mwResp.body().string() : "";
                        emitter.onSuccess(resp);
                        return;
                    } else {
                        String text = mwResp.body() != null ? mwResp.body().string() : "";
                        emitter.onError(new IOException("HTTP " + mwResp.code() + ": " + text));
                        return;
                    }
                }
            } catch (Exception e) {
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
                String mwUrl = MIDDLEWARE_URL + "/devices";
                Request mwReq = new Request.Builder().url(mwUrl).get().build();
                try (Response mwResp = httpClient.newCall(mwReq).execute()) {
                    if (mwResp.isSuccessful()) {
                        String body = mwResp.body() != null ? mwResp.body().string() : "";
                        JsonElement root = com.google.gson.JsonParser.parseString(body);
                        if (root.isJsonObject() && root.getAsJsonObject().has("devices")) {
                            JsonElement arr = root.getAsJsonObject().get("devices");
                            emitter.onSuccess(arr.toString());
                            return;
                        }
                    }
                }
                String url = SUPABASE_URL + "/rest/v1/devices?select=*";
                Request request = new Request.Builder().url(url).get().build();
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        emitter.onSuccess(responseBody);
                    } else {
                        String error = "HTTP " + response.code() + ": " + response.message();
                        emitter.onError(new IOException(error));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "获取设备列表异常: " + e.getMessage(), e);
                emitter.onError(e);
            }
        });
    }

    public Single<String> sendDeviceConfig(String deviceId, Map<String, Object> payload) {
        return Single.create(emitter -> {
            try {
                String mwUrl = MIDDLEWARE_URL + "/devices/" + deviceId + "/config";
                String jsonBody = gson.toJson(payload);
                RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
                Request req = new Request.Builder().url(mwUrl).post(body).build();
                try (Response resp = httpClient.newCall(req).execute()) {
                    if (resp.isSuccessful()) {
                        String r = resp.body() != null ? resp.body().string() : "";
                        emitter.onSuccess(r);
                    } else {
                        String text = resp.body() != null ? resp.body().string() : "";
                        emitter.onError(new IOException("HTTP " + resp.code() + ": " + text));
                    }
                }
            } catch (Exception e) {
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
        return getSensorHistoryRaw(deviceId, "temperature", null, null, "desc", limit);
    }

    public Single<String> getSensorHistoryRaw(String deviceId, String sensorType, String from, String to, String order, int limit) {
        return Single.create(emitter -> {
            try {
                StringBuilder sb = new StringBuilder();
                sb.append(MIDDLEWARE_URL).append("/sensor/history/raw?");
                if (deviceId != null && !deviceId.isEmpty()) sb.append("device_id=").append(encode(deviceId)).append('&');
                if (sensorType != null && !sensorType.isEmpty()) sb.append("sensor_type=").append(encode(sensorType)).append('&');
                if (from != null && !from.isEmpty()) sb.append("from=").append(encode(from)).append('&');
                if (to != null && !to.isEmpty()) sb.append("to=").append(encode(to)).append('&');
                if (order != null && !order.isEmpty()) sb.append("order=").append(encode(order)).append('&');
                sb.append("limit=").append(limit);
                String url = sb.toString();
                Request request = new Request.Builder().url(url).get().build();
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        emitter.onSuccess(responseBody);
                    } else {
                        String error = "HTTP " + response.code() + ": " + response.message();
                        emitter.onError(new IOException(error));
                    }
                }
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }

    public Single<String> getSensorHistoryAgg(String deviceId, String sensorType, String from, String to, String bucket) {
        return Single.create(emitter -> {
            try {
                StringBuilder sb = new StringBuilder();
                sb.append(MIDDLEWARE_URL).append("/sensor/history/agg?");
                if (deviceId != null && !deviceId.isEmpty()) sb.append("device_id=").append(encode(deviceId)).append('&');
                if (sensorType != null && !sensorType.isEmpty()) sb.append("sensor_type=").append(encode(sensorType)).append('&');
                if (from != null && !from.isEmpty()) sb.append("from=").append(encode(from)).append('&');
                if (to != null && !to.isEmpty()) sb.append("to=").append(encode(to)).append('&');
                if (bucket != null && !bucket.isEmpty()) sb.append("bucket=").append(encode(bucket));
                String url = sb.toString();
                Request request = new Request.Builder().url(url).get().build();
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        emitter.onSuccess(responseBody);
                    } else {
                        String error = "HTTP " + response.code() + ": " + response.message();
                        emitter.onError(new IOException(error));
                    }
                }
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }

    private static String encode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            return s;
        }
    }

    /**
     * 获取某类传感器的最新值
     * @param sensorType 传感器类型（temperature/humidity/gas）
     * @return 单条最新记录JSON
     */
    public Single<String> getLatestSensorValue(String sensorType) {
        return Single.create(emitter -> {
            try {
                String mwUrl = MIDDLEWARE_URL + "/sensor/latest?sensor_type=" + sensorType;
                Request mwReq = new Request.Builder().url(mwUrl).get().build();
                try (Response mwResp = httpClient.newCall(mwReq).execute()) {
                    if (mwResp.isSuccessful()) {
                        String body = mwResp.body() != null ? mwResp.body().string() : "";
                        JsonElement root = com.google.gson.JsonParser.parseString(body);
                        if (root.isJsonObject() && root.getAsJsonObject().has("data")) {
                            JsonElement arr = root.getAsJsonObject().get("data");
                            emitter.onSuccess(arr.toString());
                            return;
                        }
                    }
                }

                String url = buildLatestSensorUrl(sensorType);
                Log.d(TAG, "获取最新传感器值(" + sensorType + ") URL=" + url);
                Request request = new Request.Builder().url(url).get().build();
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        Log.d(TAG, "传感器最新值请求成功 code=" + response.code() + " length=" + responseBody.length());
                        emitter.onSuccess(responseBody);
                    } else {
                        String error = "HTTP " + response.code() + ": " + response.message();
                        Log.e(TAG, "传感器最新值请求失败: " + error + " URL=" + url);
                        emitter.onError(new IOException(error));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "传感器最新值请求异常: " + e.getMessage(), e);
                emitter.onError(e);
            }
        });
    }

    /**
     * 从中间件获取传感器概览（temperature/humidity/gas）
     */
    public Single<String> getSensorSummary() {
        return Single.create(emitter -> {
            try {
                String mwUrl = MIDDLEWARE_URL + "/sensor/summary";
                Request mwReq = new Request.Builder().url(mwUrl).get().build();
                try (Response mwResp = httpClient.newCall(mwReq).execute()) {
                    if (mwResp.isSuccessful()) {
                        String body = mwResp.body() != null ? mwResp.body().string() : "";
                        emitter.onSuccess(body);
                        return;
                    } else {
                        String error = "HTTP " + mwResp.code() + ": " + mwResp.message();
                        emitter.onError(new IOException(error));
                        return;
                    }
                }
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }

    static String buildLatestSensorUrl(String sensorType) {
        String[] aliases = getAliases(sensorType);
        StringBuilder inClause = new StringBuilder();
        for (int i = 0; i < aliases.length; i++) {
            String enc;
            try {
                enc = URLEncoder.encode(aliases[i], "UTF-8");
            } catch (UnsupportedEncodingException e) {
                enc = aliases[i];
            }
            inClause.append(enc);
            if (i < aliases.length - 1) inClause.append(",");
        }
        return SUPABASE_URL + "/rest/v1/sensor_data?sensor_type=in.(" + inClause + ")&order=timestamp.desc&limit=1";
    }

    static String[] getAliases(String sensorType) {
        String key = sensorType == null ? "" : sensorType.toLowerCase();
        switch (key) {
            case "temperature":
                return new String[]{"temperature", "温度"};
            case "humidity":
                return new String[]{"humidity", "湿度"};
            case "gas":
                return new String[]{"gas", "可燃气体", "煤气浓度"};
            default:
                return new String[]{sensorType};
        }
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
