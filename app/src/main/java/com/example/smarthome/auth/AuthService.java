package com.example.smarthome.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 认证服务类
 * 基于Supabase REST API实现用户认证功能
 */
public class AuthService {
    private static final String TAG = "AuthService";
    private static final String BASE_URL = "https://znarfgnwmbsawgndeuzh.supabase.co";
    private static final String ANON_KEY = "sb_publishable_MMGYn93wCO4nsFuAWIzWNw_IaFHMO4W";
    
    private static final String SIGN_UP_ENDPOINT = "/auth/v1/signup";
    private static final String SIGN_IN_ENDPOINT = "/auth/v1/token?grant_type=password";
    private static final String SIGN_OUT_ENDPOINT = "/auth/v1/logout";
    private static final String USER_ENDPOINT = "/auth/v1/user";
    
    private static final String PREFS_NAME = "smarthome_auth";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    
    private OkHttpClient client;
    private Context context;
    private SharedPreferences preferences;
    
    public interface AuthCallback {
        void onSuccess(AuthResponse response);
        void onError(String error);
    }
    
    public static class AuthResponse {
        public boolean success;
        public String message;
        public String userEmail;
        public String accessToken;
        public String refreshToken;
        
        public AuthResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public AuthResponse(boolean success, String message, String userEmail, String accessToken, String refreshToken) {
            this.success = success;
            this.message = message;
            this.userEmail = userEmail;
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }
    
    public AuthService(Context context) {
        this.context = context.getApplicationContext();
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * 用户注册
     */
    public void signUp(String email, String password, final AuthCallback callback) {
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("email", email);
            requestBody.put("password", password);
        } catch (JSONException e) {
            callback.onError("请求参数错误");
            return;
        }
        
        RequestBody body = RequestBody.create(requestBody.toString(), MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(BASE_URL + SIGN_UP_ENDPOINT)
                .addHeader("apikey", ANON_KEY)
                .addHeader("Authorization", "Bearer " + ANON_KEY)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();
        
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "注册请求失败: " + e.getMessage());
                callback.onError("网络连接失败: " + e.getMessage());
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                handleAuthResponse(response, callback, "注册");
            }
        });
    }
    
    /**
     * 用户登录
     */
    public void signIn(String email, String password, final AuthCallback callback) {
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("email", email);
            requestBody.put("password", password);
        } catch (JSONException e) {
            callback.onError("请求参数错误");
            return;
        }
        
        RequestBody body = RequestBody.create(requestBody.toString(), MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(BASE_URL + SIGN_IN_ENDPOINT)
                .addHeader("apikey", ANON_KEY)
                .addHeader("Authorization", "Bearer " + ANON_KEY)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();
        
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "登录请求失败: " + e.getMessage());
                callback.onError("网络连接失败: " + e.getMessage());
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                handleAuthResponse(response, callback, "登录");
            }
        });
    }
    
    /**
     * 用户登出
     */
    public void signOut(final AuthCallback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + SIGN_OUT_ENDPOINT)
                .addHeader("apikey", ANON_KEY)
                .addHeader("Authorization", "Bearer " + getAccessToken())
                .post(RequestBody.create("", MediaType.get("application/json")))
                .build();
        
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "登出请求失败: " + e.getMessage());
                // 登出失败也清除本地状态
                clearAuthData();
                callback.onError("网络连接失败: " + e.getMessage());
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                clearAuthData();
                if (response.isSuccessful()) {
                    callback.onSuccess(new AuthResponse(true, "登出成功"));
                } else {
                    callback.onSuccess(new AuthResponse(true, "登出成功")); // 即使服务端返回错误，也清理本地状态
                }
                response.close();
            }
        });
    }
    
    /**
     * 获取当前用户信息
     */
    public void getCurrentUser(final AuthCallback callback) {
        String accessToken = getAccessToken();
        if (accessToken == null) {
            callback.onError("用户未登录");
            return;
        }
        
        Request request = new Request.Builder()
                .url(BASE_URL + USER_ENDPOINT)
                .addHeader("apikey", ANON_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();
        
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "获取用户信息失败: " + e.getMessage());
                callback.onError("网络连接失败: " + e.getMessage());
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseBody = response.body().string();
                    if (response.isSuccessful()) {
                        JSONObject userData = new JSONObject(responseBody);
                        String userEmail = userData.getString("email");
                        callback.onSuccess(new AuthResponse(true, "获取用户信息成功", userEmail, accessToken, getRefreshToken()));
                    } else {
                        callback.onError("获取用户信息失败: " + responseBody);
                    }
                } catch (Exception e) {
                    callback.onError("解析用户信息失败");
                } finally {
                    response.close();
                }
            }
        });
    }
    
    /**
     * 处理认证响应
     */
    private void handleAuthResponse(Response response, AuthCallback callback, String operation) {
        try {
            String responseBody = response.body().string();
            Log.d(TAG, operation + "响应: " + responseBody);
            JSONObject jsonResponse = new JSONObject(responseBody);
            
            if (response.isSuccessful()) {
                // 检查是否包含access_token（登录成功或立即可用的注册）
                if (jsonResponse.has("access_token")) {
                    String accessToken = jsonResponse.getString("access_token");
                    String refreshToken = jsonResponse.has("refresh_token") ? jsonResponse.getString("refresh_token") : null;
                    
                    String userEmail = null;
                    if (jsonResponse.has("user")) {
                        JSONObject user = jsonResponse.getJSONObject("user");
                        userEmail = user.getString("email");
                    } else if (jsonResponse.has("email")) {
                        userEmail = jsonResponse.getString("email");
                    }
                    
                    if (userEmail != null) {
                        // 保存认证信息
                        saveAuthData(userEmail, accessToken, refreshToken);
                        callback.onSuccess(new AuthResponse(true, operation + "成功", userEmail, accessToken, refreshToken));
                    } else {
                        callback.onSuccess(new AuthResponse(true, operation + "成功", null, accessToken, refreshToken));
                    }
                }
                // 检查是否只包含user（注册成功但需要邮箱验证）
                else if (jsonResponse.has("user")) {
                    JSONObject user = jsonResponse.getJSONObject("user");
                    String userEmail = user.getString("email");
                    callback.onSuccess(new AuthResponse(true, "注册成功，请查收邮箱验证链接", userEmail, null, null));
                }
                // 检查是否只有email（另一种注册格式）
                else if (jsonResponse.has("email")) {
                    String userEmail = jsonResponse.getString("email");
                    callback.onSuccess(new AuthResponse(true, "注册成功，请查收邮箱验证链接", userEmail, null, null));
                }
                // 检查是否表明需要确认
                else if (jsonResponse.has("confirmation_sent_at") || jsonResponse.has("email_otp")) {
                    // 需要邮箱确认
                    String message = jsonResponse.optString("message", "注册成功，请查收邮箱验证链接");
                    callback.onSuccess(new AuthResponse(true, message, null, null, null));
                }
                // 通用成功处理
                else if (jsonResponse.has("success") && jsonResponse.getBoolean("success")) {
                    callback.onSuccess(new AuthResponse(true, operation + "成功"));
                }
                else {
                    Log.w(TAG, operation + "响应格式未知: " + responseBody);
                    callback.onError(operation + "响应格式异常");
                }
            } else {
                String errorMessage = jsonResponse.optString("error_description", jsonResponse.optString("msg", jsonResponse.optString("error", "未知错误")));
                Log.e(TAG, operation + "失败: " + errorMessage);
                callback.onError(operation + "失败: " + errorMessage);
            }
        } catch (Exception e) {
            Log.e(TAG, operation + "响应解析失败", e);
            callback.onError(operation + "响应解析失败: " + e.getMessage());
        } finally {
            response.close();
        }
    }
    
    /**
     * 保存认证数据到本地
     */
    private void saveAuthData(String email, String accessToken, String refreshToken) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        if (refreshToken != null) {
            editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        }
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }
    
    /**
     * 清除认证数据
     */
    private void clearAuthData() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(KEY_USER_EMAIL);
        editor.remove(KEY_ACCESS_TOKEN);
        editor.remove(KEY_REFRESH_TOKEN);
        editor.remove(KEY_IS_LOGGED_IN);
        editor.apply();
    }
    
    /**
     * 检查是否已登录
     */
    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    /**
     * 获取当前用户邮箱
     */
    public String getCurrentUserEmail() {
        return preferences.getString(KEY_USER_EMAIL, null);
    }
    
    /**
     * 获取访问令牌
     */
    public String getAccessToken() {
        return preferences.getString(KEY_ACCESS_TOKEN, null);
    }
    
    /**
     * 获取刷新令牌
     */
    public String getRefreshToken() {
        return preferences.getString(KEY_REFRESH_TOKEN, null);
    }
}