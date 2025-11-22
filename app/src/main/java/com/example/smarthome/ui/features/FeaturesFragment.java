package com.example.smarthome.ui.features;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.text.InputType;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import com.example.smarthome.R;
import com.example.smarthome.supabase.SupabaseClient;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.example.smarthome.model.Device;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import android.content.res.ColorStateList;
import android.graphics.Color;

public class FeaturesFragment extends Fragment {
    private SupabaseClient client;
    private Gson gson = new Gson();
    private TextView status;
    private String lightDeviceId;
    private com.example.smarthome.utils.LightStateRepository lightRepo;
    private final com.example.smarthome.utils.MqttBridge.LightStatusListener listener = (id,b,c,p)->{
        if (lightDeviceId==null || !id.equals(lightDeviceId)) return;
        View v = getView(); if (v==null) return;
        TextView current = v.findViewById(R.id.text_brightness_current);
        TextView brightnessValue = v.findViewById(R.id.brightness_value);
        android.widget.SeekBar brightness = v.findViewById(R.id.brightness_slider);
        com.google.android.material.switchmaterial.SwitchMaterial switchDevice = v.findViewById(R.id.switch_device);
        MaterialButton btnNatural = v.findViewById(R.id.button_temp_natural);
        MaterialButton btnCool = v.findViewById(R.id.button_temp_cool);
        MaterialButton btnWarm = v.findViewById(R.id.button_temp_warm);
        if (b>=0){ if (current!=null) current.setText("当前亮度: " + b + "%"); if (brightnessValue!=null) brightnessValue.setText(b+"%"); if (brightness!=null) brightness.setProgress(b); lightRepo.setBrightness(id,b); }
        if (p!=null && switchDevice!=null){ boolean on = "ON".equalsIgnoreCase(p); if (switchDevice.isChecked()!=on) switchDevice.setChecked(on); lightRepo.setPower(id,p); }
        if (c!=null){ lightRepo.setColorTemp(id,c); applyColorTempButtons(v, c); }
    };
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_features, container, false);
        client = SupabaseClient.getInstance(requireContext());
        lightRepo = new com.example.smarthome.utils.LightStateRepository(requireContext());
        status = v.findViewById(R.id.text_status);
        v.findViewById(R.id.button_scene_home).setOnClickListener(view -> runScene("home"));
        v.findViewById(R.id.button_scene_away).setOnClickListener(view -> runScene("away"));
        v.findViewById(R.id.button_scene_sleep).setOnClickListener(view -> runScene("sleep"));
        // 定时任务已移除
        v.findViewById(R.id.button_view_env_trend).setOnClickListener(view -> {
            androidx.fragment.app.Fragment fragment = new TrendFragment();
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        });
        // 环境概览卡已移除
        updateCurrentHumidity(v);
        MaterialButton btnNatural = v.findViewById(R.id.button_temp_natural);
        MaterialButton btnCool = v.findViewById(R.id.button_temp_cool);
        MaterialButton btnWarm = v.findViewById(R.id.button_temp_warm);
        com.google.android.material.switchmaterial.SwitchMaterial switchDevice = v.findViewById(R.id.switch_device);
        android.widget.SeekBar brightness = v.findViewById(R.id.brightness_slider);
        TextView brightnessValue = v.findViewById(R.id.brightness_value);
        selectLightDeviceId();
        v.postDelayed(() -> applyMemoryToUi(v), 300);
        ColorStateList tintDefault = ColorStateList.valueOf(Color.parseColor("#4658F8"));
        ColorStateList tintActive = ColorStateList.valueOf(Color.parseColor("#f8f844"));
        if (btnNatural != null && btnCool != null && btnWarm != null) {
            btnNatural.setBackgroundTintList(tintDefault);
            btnCool.setBackgroundTintList(tintDefault);
            btnWarm.setBackgroundTintList(tintDefault);
            btnNatural.setTextColor(Color.WHITE);
            btnCool.setTextColor(Color.WHITE);
            btnWarm.setTextColor(Color.WHITE);
            btnNatural.setOnClickListener(view -> {
                btnNatural.setBackgroundTintList(tintActive);
                btnNatural.setTextColor(Color.parseColor("#4658F8"));
                btnCool.setBackgroundTintList(tintDefault);
                btnCool.setTextColor(Color.WHITE);
                btnWarm.setBackgroundTintList(tintDefault);
                btnWarm.setTextColor(Color.WHITE);
                sendColorTemp("natural");
            });
            btnCool.setOnClickListener(view -> {
                btnNatural.setBackgroundTintList(tintDefault);
                btnNatural.setTextColor(Color.WHITE);
                btnCool.setBackgroundTintList(tintActive);
                btnCool.setTextColor(Color.parseColor("#4658F8"));
                btnWarm.setBackgroundTintList(tintDefault);
                btnWarm.setTextColor(Color.WHITE);
                sendColorTemp("cool");
            });
            btnWarm.setOnClickListener(view -> {
                btnNatural.setBackgroundTintList(tintDefault);
                btnNatural.setTextColor(Color.WHITE);
                btnCool.setBackgroundTintList(tintDefault);
                btnCool.setTextColor(Color.WHITE);
                btnWarm.setBackgroundTintList(tintActive);
                btnWarm.setTextColor(Color.parseColor("#4658F8"));
                sendColorTemp("warm");
            });
        }
        if (brightness != null) {
            brightness.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                    if (brightnessValue != null) brightnessValue.setText(progress + "%");
                }
                @Override public void onStartTrackingTouch(android.widget.SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(android.widget.SeekBar seekBar) {
                    sendBrightness(seekBar.getProgress());
                }
            });
        }
        if (switchDevice != null) {
            switchDevice.setOnCheckedChangeListener((buttonView, isChecked) -> sendPower(isChecked ? "ON" : "OFF"));
        }
        return v;
    }

    private void selectLightDeviceId() {
        client.getDevices()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(body -> {
                    try {
                        JsonElement root = com.google.gson.JsonParser.parseString(body);
                        java.util.List<Device> devices;
                        if (root.isJsonArray()) devices = gson.fromJson(root, new TypeToken<java.util.List<Device>>(){}.getType());
                        else devices = new java.util.ArrayList<>();
                        String found = null;
                        for (Device d : devices) {
                            String t = d.getDeviceType() == null ? "" : d.getDeviceType().toLowerCase();
                            String name = d.getName() == null ? "" : d.getName();
                            if ("light".equals(t) || name.contains("客厅灯")) { found = d.getDeviceId(); break; }
                        }
                        lightDeviceId = found;
                    } catch (Exception ignored) {}
                }, err -> {});
    }

    private void sendBrightness(int value) {
        if (lightDeviceId == null) return;
        int vInt = Math.max(0, Math.min(100, value));
        Map<String,Object> payload = new HashMap<>();
        payload.put("command", "brightness");
        payload.put("value", vInt);
        client.sendMqttCommand(lightDeviceId, payload)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    lightRepo.setBrightness(lightDeviceId, vInt);
                    View v = getView(); if (v!=null){ TextView current = v.findViewById(R.id.text_brightness_current); TextView bv = v.findViewById(R.id.brightness_value); if (current!=null) current.setText("当前亮度: "+vInt+"%"); if (bv!=null) bv.setText(vInt+"%"); }
                }, err -> Toast.makeText(requireContext(), "亮度设置失败", Toast.LENGTH_SHORT).show());
    }

    private void sendColorTemp(String temp) {
        if (lightDeviceId == null) return;
        String p = temp==null?"natural":temp.toLowerCase();
        Map<String,Object> payload = new HashMap<>();
        payload.put("command", "color_temp");
        payload.put("value", p);
        client.sendMqttCommand(lightDeviceId, payload)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    lightRepo.setColorTemp(lightDeviceId, p);
                    View v = getView(); if (v!=null) applyColorTempButtons(v, p);
                }, err -> Toast.makeText(requireContext(), "色温设置失败", Toast.LENGTH_SHORT).show());
    }

    private void sendPower(String power) {
        if (lightDeviceId == null) return;
        String p = power==null?"ON":power.toUpperCase();
        Map<String,Object> payload = new HashMap<>();
        payload.put("command", "power");
        payload.put("value", p);
        client.sendMqttCommand(lightDeviceId, payload)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> { lightRepo.setPower(lightDeviceId, p); }, err -> Toast.makeText(requireContext(), "电源设置失败", Toast.LENGTH_SHORT).show());
    }

    private void runScene(String scene) {
        status.setText("执行场景: " + scene);
        client.getDevices()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(body -> {
                    try {
                        JsonElement root = com.google.gson.JsonParser.parseString(body);
                        List<Device> devices;
                        if (root.isJsonArray()) devices = gson.fromJson(root, new TypeToken<List<Device>>(){}.getType());
                        else {
                            devices = new ArrayList<>();
                        }
                        applyScene(scene, devices);
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "设备加载失败", Toast.LENGTH_SHORT).show();
                    }
                }, err -> Toast.makeText(requireContext(), "设备加载失败", Toast.LENGTH_SHORT).show());
    }

    private void applyScene(String scene, List<Device> devices) {
        int ok = 0, fail = 0;
        for (Device d : devices) {
            String t = d.getDeviceType() == null ? "" : d.getDeviceType().toLowerCase();
            String name = d.getName() == null ? "" : d.getName();
            Map<String,Object> payload = null;
            switch (scene) {
                case "home":
                    if (t.equals("light")) { payload = new HashMap<>(); payload.put("command","COLOR_SET"); payload.put("value","#FFC107"); client.sendMqttCommand(d.getDeviceId(), payload).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(r->{},e->{}); }
                    if (t.equals("buzzer")) { payload = new HashMap<>(); payload.put("command","BUZZ_OFF"); client.sendMqttCommand(d.getDeviceId(), payload).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(r->{},e->{}); }
                    if (name.contains("人体红外")) { Map<String,Object> cfg = new HashMap<>(); cfg.put("action","disable"); client.sendDeviceConfig(d.getDeviceId(), cfg).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(r->{},e->{}); }
                    if (name.contains("门磁")) { Map<String,Object> cfg = new HashMap<>(); cfg.put("action","disable"); client.sendDeviceConfig(d.getDeviceId(), cfg).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(r->{},e->{}); }
                    break;
                case "away":
                    if (t.equals("light")) { payload = new HashMap<>(); payload.put("command","TIMER_SET"); payload.put("at", isoInMinutes(1)); payload.put("action","OFF"); client.sendMqttCommand(d.getDeviceId(), payload).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(r->{},e->{}); }
                    if (t.equals("humidifier")) { payload = new HashMap<>(); payload.put("command","HUMIDIFY_OFF"); client.sendMqttCommand(d.getDeviceId(), payload).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(r->{},e->{}); }
                    if (t.equals("buzzer")) { payload = new HashMap<>(); payload.put("command","BUZZ_OFF"); client.sendMqttCommand(d.getDeviceId(), payload).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(r->{},e->{}); }
                    if (name.contains("人体红外")) { Map<String,Object> cfg = new HashMap<>(); cfg.put("action","enable"); client.sendDeviceConfig(d.getDeviceId(), cfg).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(r->{},e->{}); }
                    if (name.contains("门磁")) { Map<String,Object> cfg = new HashMap<>(); cfg.put("action","enable"); client.sendDeviceConfig(d.getDeviceId(), cfg).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(r->{},e->{}); }
                    break;
                case "sleep":
                    if (t.equals("light")) { payload = new HashMap<>(); payload.put("command","BRIGHTNESS_SET"); payload.put("value",20); client.sendMqttCommand(d.getDeviceId(), payload).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(r->{},e->{}); }
                    if (t.equals("buzzer")) { payload = new HashMap<>(); payload.put("command","BUZZ_OFF"); client.sendMqttCommand(d.getDeviceId(), payload).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(r->{},e->{}); }
                    if (name.contains("人体红外")) { Map<String,Object> cfg = new HashMap<>(); cfg.put("action","enable"); client.sendDeviceConfig(d.getDeviceId(), cfg).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(r->{},e->{}); }
                    if (name.contains("门磁")) { Map<String,Object> cfg = new HashMap<>(); cfg.put("action","enable"); client.sendDeviceConfig(d.getDeviceId(), cfg).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(r->{},e->{}); }
                    break;
                case "wake":
                    if (t.equals("light")) { payload = new HashMap<>(); payload.put("command","COLOR_SET"); payload.put("value","#FFFFFF"); client.sendMqttCommand(d.getDeviceId(), payload).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(r->{},e->{}); }
                    break;
            }
            // 状态提示更新由各调用设置
        }
    }

    private String isoInMinutes(int minutes) {
        long ms = System.currentTimeMillis() + minutes * 60L * 1000L;
        return new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault()).format(new java.util.Date(ms));
    }
    private int nowHour() { java.util.Calendar c = java.util.Calendar.getInstance(); return c.get(java.util.Calendar.HOUR_OF_DAY); }
    private int nowMinute() { java.util.Calendar c = java.util.Calendar.getInstance(); return c.get(java.util.Calendar.MINUTE); }

    // 定时任务功能删除

    // 环境概览加载逻辑已移除

    private void updateCurrentHumidity(View v) {
        TextView current = v.findViewById(R.id.text_humidity_current);
        if (current == null) return;
        client.getSensorSummary()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(body -> {
                    try {
                        JsonElement root = com.google.gson.JsonParser.parseString(body);
                        String hum = extractFirstValue(root, "humidity");
                        if (hum != null) {
                            current.setText("当前湿度: " + hum + "%");
                        } else {
                            current.setText("当前湿度: --");
                        }
                    } catch (Exception e) {
                        current.setText("当前湿度: --");
                    }
                }, err -> current.setText("当前湿度: --"));
    }

    private String extractFirstValue(JsonElement root, String key) {
        if (root != null && root.isJsonObject() && root.getAsJsonObject().has(key)) {
            JsonElement arr = root.getAsJsonObject().get(key);
            if (arr.isJsonArray() && arr.getAsJsonArray().size() > 0) {
                JsonElement obj = arr.getAsJsonArray().get(0);
                if (obj.isJsonObject() && obj.getAsJsonObject().has("value")) {
                    return obj.getAsJsonObject().get("value").getAsString();
                }
            }
        }
        return null;
    }

    

    private String trimAgg(String json) {
        try {
            JsonElement root = com.google.gson.JsonParser.parseString(json);
            if (root.isJsonObject() && root.getAsJsonObject().has("data")) {
                return root.getAsJsonObject().get("data").toString();
            }
        } catch (Exception ignored) {}
        return json;
    }
    private void applyMemoryToUi(View v){
        if (lightDeviceId==null) return;
        int b = lightRepo.getBrightness(lightDeviceId);
        String c = lightRepo.getColorTemp(lightDeviceId);
        String p = lightRepo.getPower(lightDeviceId);
        TextView current = v.findViewById(R.id.text_brightness_current);
        TextView bv = v.findViewById(R.id.brightness_value);
        android.widget.SeekBar bs = v.findViewById(R.id.brightness_slider);
        com.google.android.material.switchmaterial.SwitchMaterial sw = v.findViewById(R.id.switch_device);
        if (current!=null) current.setText("当前亮度: "+b+"%");
        if (bv!=null) bv.setText(b+"%");
        if (bs!=null) bs.setProgress(b);
        if (sw!=null) sw.setChecked("ON".equalsIgnoreCase(p));
        applyColorTempButtons(v, c);
    }

    private void applyColorTempButtons(View v, String temp){
        String p = temp==null?"natural":temp.toLowerCase();
        MaterialButton btnNatural = v.findViewById(R.id.button_temp_natural);
        MaterialButton btnCool = v.findViewById(R.id.button_temp_cool);
        MaterialButton btnWarm = v.findViewById(R.id.button_temp_warm);
        ColorStateList tintDefault = ColorStateList.valueOf(Color.parseColor("#4658F8"));
        ColorStateList tintActive = ColorStateList.valueOf(Color.parseColor("#f8f844"));
        if (btnNatural==null||btnCool==null||btnWarm==null) return;
        btnNatural.setBackgroundTintList("natural".equals(p)?tintActive:tintDefault);
        btnNatural.setTextColor("natural".equals(p)?Color.parseColor("#4658F8"):Color.WHITE);
        btnCool.setBackgroundTintList("cool".equals(p)?tintActive:tintDefault);
        btnCool.setTextColor("cool".equals(p)?Color.parseColor("#4658F8"):Color.WHITE);
        btnWarm.setBackgroundTintList("warm".equals(p)?tintActive:tintDefault);
        btnWarm.setTextColor("warm".equals(p)?Color.parseColor("#4658F8"):Color.WHITE);
    }

    @Override public void onDestroyView(){
        super.onDestroyView();
    }
}
