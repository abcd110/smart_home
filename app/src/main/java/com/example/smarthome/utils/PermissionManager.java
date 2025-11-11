package com.example.smarthome.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * 权限管理器
 * 管理应用所需的各种权限
 */
public class PermissionManager {
    
    public static final int REQUEST_STORAGE_PERMISSION = 1001;
    public static final int REQUEST_CAMERA_PERMISSION = 1002;
    public static final int REQUEST_READ_EXTERNAL_STORAGE = 1003;
    public static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1004;
    
    private static PermissionManager instance;
    private final Context context;
    
    private PermissionManager(Context context) {
        this.context = context;
    }
    
    /**
     * 获取PermissionManager单例实例
     */
    public static synchronized PermissionManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("PermissionManager not initialized. Call init(Context) first.");
        }
        return instance;
    }
    
    /**
     * 初始化PermissionManager单例
     */
    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new PermissionManager(context.getApplicationContext());
        }
    }
    
    /**
     * 检查是否有存储权限
     */
    public boolean hasStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ 使用 READ_MEDIA_IMAGES 等细分权限
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED;
        } else {
            // Android 12 及以下版本
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED;
        }
    }
    
    /**
     * 检查是否有相机权限
     */
    public boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * 检查是否有读取外部存储权限
     */
    public boolean hasReadExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return true; // Android 13+ 不需要此权限
        } else {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED;
        }
    }
    
    /**
     * 检查是否有写入外部存储权限
     */
    public boolean hasWriteExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true; // Android 10+ 不需要此权限（分区存储）
        } else {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED;
        }
    }
    
    /**
     * 请求存储权限
     */
    public void requestStoragePermission(Activity activity) {
        if (!hasStoragePermission()) {
            String[] permissions;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions = new String[]{Manifest.permission.READ_MEDIA_IMAGES};
            } else {
                permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
            }
            
            ActivityCompat.requestPermissions(
                activity,
                permissions,
                REQUEST_STORAGE_PERMISSION
            );
        }
    }
    
    /**
     * 请求相机权限
     */
    public void requestCameraPermission(Activity activity) {
        if (!hasCameraPermission()) {
            ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.CAMERA},
                REQUEST_CAMERA_PERMISSION
            );
        }
    }
    
    /**
     * 请求读取外部存储权限
     */
    public void requestReadExternalStoragePermission(Activity activity) {
        if (!hasReadExternalStoragePermission() && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_READ_EXTERNAL_STORAGE
            );
        }
    }
    
    /**
     * 请求写入外部存储权限
     */
    public void requestWriteExternalStoragePermission(Activity activity) {
        if (!hasWriteExternalStoragePermission() && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_WRITE_EXTERNAL_STORAGE
            );
        }
    }
    
    /**
     * 请求所有必要权限
     */
    public void requestAllNecessaryPermissions(Activity activity) {
        java.util.List<String> permissionsToRequest = new java.util.ArrayList<>();
        
        if (!hasStoragePermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES);
            } else {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
        
        if (!hasCameraPermission()) {
            permissionsToRequest.add(Manifest.permission.CAMERA);
        }
        
        if (!hasReadExternalStoragePermission() && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        
        if (!hasWriteExternalStoragePermission() && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        
        if (!permissionsToRequest.isEmpty()) {
            String[] permissionsArray = permissionsToRequest.toArray(new String[0]);
            ActivityCompat.requestPermissions(
                activity,
                permissionsArray,
                1000 // 综合请求码
            );
        }
    }
    
    /**
     * 权限结果处理
     */
    public boolean handlePermissionResult(
        int requestCode,
        String[] permissions,
        int[] grantResults
    ) {
        if (grantResults.length == 0) {
            return false;
        }
        
        switch (requestCode) {
            case REQUEST_STORAGE_PERMISSION:
                return grantResults[0] == PackageManager.PERMISSION_GRANTED;
            case REQUEST_CAMERA_PERMISSION:
                return grantResults[0] == PackageManager.PERMISSION_GRANTED;
            case REQUEST_READ_EXTERNAL_STORAGE:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    return true; // Android 13+ 不需要此权限
                } else {
                    return grantResults[0] == PackageManager.PERMISSION_GRANTED;
                }
            case REQUEST_WRITE_EXTERNAL_STORAGE:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    return true; // Android 10+ 不需要此权限
                } else {
                    return grantResults[0] == PackageManager.PERMISSION_GRANTED;
                }
            case 1000: // 综合权限请求
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        return false;
                    }
                }
                return true;
            default:
                return false;
        }
    }
    
    /**
     * 权限被拒绝的提示信息
     */
    public String getPermissionDeniedMessage(String permission) {
        switch (permission) {
            case Manifest.permission.CAMERA:
                return "需要相机权限才能拍照和扫描设备";
            case Manifest.permission.READ_EXTERNAL_STORAGE:
            case Manifest.permission.READ_MEDIA_IMAGES:
                return "需要存储权限才能选择和上传文件";
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return "需要写入权限才能保存文件";
            default:
                return "需要相应权限才能使用此功能";
        }
    }
}