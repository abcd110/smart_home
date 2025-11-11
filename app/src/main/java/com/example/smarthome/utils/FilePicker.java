package com.example.smarthome.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 文件选择器工具类
 * 提供文件选择、拍照等功能
 */
public class FilePicker {
    
    private static final String IMAGE_DIRECTORY = "SmartHome";
    private static final String TEMP_PHOTO_FILE = "temp_photo_";
    private static final String LOG_FILE = "device_log_";
    
    private static FilePicker instance;
    private final Context context;
    
    private FilePicker(Context context) {
        this.context = context;
    }
    
    /**
     * 获取FilePicker单例实例
     */
    public static synchronized FilePicker getInstance() {
        if (instance == null) {
            throw new IllegalStateException("FilePicker not initialized. Call init(Context) first.");
        }
        return instance;
    }
    
    /**
     * 初始化FilePicker单例
     */
    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new FilePicker(context.getApplicationContext());
        }
    }
    
    /**
     * 创建临时照片文件
     */
    public File createTempPhotoFile() {
        try {
            File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (storageDir == null) {
                return null;
            }
            
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }
            
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = TEMP_PHOTO_FILE + timeStamp + ".jpg";
            File file = new File(storageDir, fileName);
            
            if (file.createNewFile()) {
                return file;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 创建日志文件
     */
    public File createLogFile(String deviceId) {
        try {
            File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            if (storageDir == null) {
                return null;
            }
            
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }
            
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = LOG_FILE + (deviceId != null ? deviceId : "general") + "_" + timeStamp + ".txt";
            File file = new File(storageDir, fileName);
            
            if (file.createNewFile()) {
                return file;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 保存字符串到文件
     */
    public boolean saveStringToFile(String content, File file) {
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            try {
                outputStream.write(content.getBytes());
                outputStream.flush();
                return true;
            } finally {
                outputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 获取文件URI
     */
    public Uri getFileUri(File file) {
        return FileProvider.getUriForFile(
            context,
            context.getPackageName() + ".fileprovider",
            file
        );
    }
    
    /**
     * 获取Context
     */
    public Context getContext() {
        return context;
    }
    
    /**
     * 检查是否有相机权限
     */
    public boolean hasCameraPermission() {
        // 实际实现需要检查相机权限
        return true; // 简化实现
    }
    
    /**
     * 检查是否有存储权限
     */
    public boolean hasStoragePermission() {
        // 实际实现需要检查存储权限
        return true; // 简化实现
    }
    
    /**
     * 创建选择图片的Intent
     */
    public Intent createImagePickerIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/jpeg", "image/png", "image/webp"});
        return intent;
    }
    
    /**
     * 创建拍照的Intent
     */
    public Intent createCameraIntent(File photoFile) {
        try {
            Uri photoUri = getFileUri(photoFile);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            return intent;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 创建选择文件的Intent
     */
    public Intent createFilePickerIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
            "application/pdf",
            "text/plain",
            "application/json",
            "text/csv"
        });
        return intent;
    }
    
    /**
     * 清理临时文件
     */
    public boolean cleanTempFiles() {
        try {
            File tempDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File logDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            
            int cleaned = 0;
            
            // 清理临时照片
            if (tempDir != null) {
                File[] tempFiles = tempDir.listFiles(file -> file.getName().startsWith(TEMP_PHOTO_FILE));
                if (tempFiles != null) {
                    for (File file : tempFiles) {
                        if (file.delete()) cleaned++;
                    }
                }
            }
            
            // 清理临时日志文件
            if (logDir != null) {
                File[] logFiles = logDir.listFiles(file -> file.getName().startsWith(LOG_FILE));
                if (logFiles != null) {
                    for (File file : logFiles) {
                        if (file.delete()) cleaned++;
                    }
                }
            }
            
            return cleaned > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}