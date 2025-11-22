package com.example.smarthome.ui.home;

import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

import com.example.smarthome.R;

public class BindingAdapters {
    @BindingAdapter({"deviceTypeIcon", "deviceName"})
    public static void setDeviceTypeIcon(ImageView view, String type, String name) {
        String t = type == null ? "" : type.toLowerCase();
        String n = name == null ? "" : name.toLowerCase();
        int res = R.drawable.ic_device_outlined;
        if (t.equals("sensor")) {
            if (n.contains("温湿度")) res = R.drawable.wenshidu;
            else if (n.contains("可燃气") || n.contains("煤气") || n.contains("gas")) res = R.drawable.meiqi;
            else if (n.contains("人体红外") || n.contains("pir")) res = R.drawable.rentihongwaiganying;
            else if (n.contains("门磁") || n.contains("door") || n.contains("lock")) res = R.drawable.security_lock;
            else res = R.drawable.wenshidu;
        } else if (t.equals("actuator")) {
            if (n.contains("蜂鸣器") || n.contains("buzzer") || n.contains("bell")) res = R.drawable.jiadian_fengmingqi;
            else if (n.contains("灯") || n.contains("light")) res = R.drawable.zhinengdengkong;
            else if (n.contains("加湿器") || n.contains("humidifier")) res = R.drawable.jiashiqi;
            else res = R.drawable.ic_device_outlined;
        } else {
            if (t.equals("light")) res = R.drawable.zhinengdengkong;
            else if (t.equals("buzzer")) res = R.drawable.jiadian_fengmingqi;
            else if (t.equals("humidifier")) res = R.drawable.jiashiqi;
            else if (t.equals("pir")) res = R.drawable.rentihongwaiganying;
            else if (t.equals("gas")) res = R.drawable.meiqi;
            else if (t.equals("temp_humidity")) res = R.drawable.wenshidu;
            else res = R.drawable.ic_device_outlined;
        }
        view.setImageResource(res);
    }
}
