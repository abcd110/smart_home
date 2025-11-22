package com.example.smarthome.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class LightStateRepository {
    private static final String PREF = "light_state_repo";
    private final SharedPreferences sp;

    public LightStateRepository(Context ctx) { this.sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE); }

    private String kB(String id){ return "light_"+id+"_brightness"; }
    private String kC(String id){ return "light_"+id+"_colorTemp"; }
    private String kP(String id){ return "light_"+id+"_power"; }

    public int getBrightness(String id){ return sp.getInt(kB(id), 70); }
    public void setBrightness(String id, int v){ sp.edit().putInt(kB(id), Math.max(0, Math.min(100, v))).apply(); }

    public String getColorTemp(String id){ return sp.getString(kC(id), "natural"); }
    public void setColorTemp(String id, String v){ sp.edit().putString(kC(id), v==null?"natural":v).apply(); }

    public String getPower(String id){ return sp.getString(kP(id), "ON"); }
    public void setPower(String id, String v){ sp.edit().putString(kP(id), v==null?"ON":v.toUpperCase()).apply(); }
}