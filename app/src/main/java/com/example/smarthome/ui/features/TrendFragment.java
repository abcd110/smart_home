package com.example.smarthome.ui.features;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.content.res.ColorStateList;
import com.google.android.material.button.MaterialButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.smarthome.R;
import com.example.smarthome.supabase.SupabaseClient;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.gson.JsonElement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class TrendFragment extends Fragment {
    private SupabaseClient client;
    private MaterialButton btnToday, btn3d, btn7d;
    private TextView tvTrendTitle;
    private LineChart chartTemp, chartHum, chartGas;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_trend, container, false);
        client = SupabaseClient.getInstance(requireContext());
        btnToday = (MaterialButton) v.findViewById(R.id.btn_filter_today);
        btn3d = (MaterialButton) v.findViewById(R.id.btn_filter_3d);
        btn7d = (MaterialButton) v.findViewById(R.id.btn_filter_7d);
        tvTrendTitle = v.findViewById(R.id.tv_trend_title);
        chartTemp = v.findViewById(R.id.chart_temp);
        chartHum = v.findViewById(R.id.chart_hum);
        chartGas = v.findViewById(R.id.chart_gas);
        setupFilters();
        View back = v.findViewById(R.id.btn_back);
        if (back != null) back.setOnClickListener(view -> {
            if (getActivity()!=null) getActivity().getSupportFragmentManager().popBackStack();
        });
        return v;
    }

    private void setupFilters() {
        btnToday.setOnClickListener(v -> {
            updateActiveButton(btnToday);
            updateTitle("今日数据变化");
            applyRange(1, "5m");
        });
        btn3d.setOnClickListener(v -> {
            updateActiveButton(btn3d);
            updateTitle("近3天数据变化");
            applyRange(72, "30m");
        });
        btn7d.setOnClickListener(v -> {
            updateActiveButton(btn7d);
            updateTitle("近7天数据变化");
            applyRange(168, "6h");
        });
        // 默认选中"今日"按钮
        updateActiveButton(btnToday);
        updateTitle("今日数据变化");
        applyRange(1, "5m");
    }
    
    private void updateActiveButton(MaterialButton activeButton) {
        // 重置所有按钮样式为默认状态
        int defaultTextColor = getResources().getColor(R.color.md_theme_light_onSurfaceVariant, requireActivity().getTheme());
        int activeTextColor = getResources().getColor(R.color.md_theme_light_primary, requireActivity().getTheme());
        int buttonBackgroundColor = getResources().getColor(R.color.card_surface, requireActivity().getTheme());
        
        // 重置所有按钮的文本色，保持背景色和阴影不变
        for (MaterialButton btn : new MaterialButton[]{btnToday, btn3d, btn7d}) {
            btn.setTextColor(defaultTextColor);
            btn.setBackgroundTintList(ColorStateList.valueOf(buttonBackgroundColor));
        }
        
        // 设置选中按钮的文本颜色
        activeButton.setTextColor(activeTextColor);
        activeButton.setBackgroundTintList(ColorStateList.valueOf(buttonBackgroundColor));
    }
    
    private void updateTitle(String title) {
        if (tvTrendTitle != null) {
            tvTrendTitle.setText(title);
        }
    }

    private void loadAgg(String sensorType, int hours, String bucket, LineChart chart) {
        long now = System.currentTimeMillis();
        long fromMs = now - hours*3600L*1000L;
        String to = iso(now);
        String from = iso(fromMs);
        client.getSensorHistoryAgg(null, sensorType, from, to, bucket)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(json -> {
                    try {
                        renderChart(chart, json);
                    } catch (Exception e) {
                        chart.clear();
                    }
                }, err -> chart.clear());
    }

    private void applyRange(int hours, String bucket) {
        loadAgg("temperature", hours, bucket, chartTemp);
        loadAgg("humidity", hours, bucket, chartHum);
        loadAgg("gas", hours, bucket, chartGas);
    }

    private void renderChart(LineChart chart, String json) {
        JsonElement root = com.google.gson.JsonParser.parseString(json);
        List<Entry> entries = new ArrayList<>();
        List<Long> xTimes = new ArrayList<>();
        if (root.isJsonObject() && root.getAsJsonObject().has("data")) {
            for (JsonElement el : root.getAsJsonObject().get("data").getAsJsonArray()) {
                if (el.isJsonObject()) {
                    JsonElement b = el.getAsJsonObject().get("bucket");
                    JsonElement avg = el.getAsJsonObject().get("avg");
                    if (b!=null && avg!=null) {
                        long ts = parseIso(b.getAsString());
                        float y = (float) avg.getAsDouble();
                        entries.add(new Entry(entries.size(), y));
                        xTimes.add(ts);
                    }
                }
            }
        }
        LineDataSet set = new LineDataSet(entries, "趋势");
        set.setDrawCircles(false);
        // 根据不同图表设置不同线颜色
        if (chart == chartTemp) {
            set.setColor(0xFF165DFF); // 温度图表使用#165DFF
        } else if (chart == chartHum) {
            set.setColor(0xFF4BC0C0); // 湿度图表使用#4BC0C0
        } else if (chart == chartGas) {
            set.setColor(0xFFFF7D00); // 燃气浓度图表使用#FF7D00
        } else {
            set.setColor(0xFF4CAF50); // 默认颜色
        }
        set.setLineWidth(2f);
        LineData data = new LineData(set);
        chart.setData(data);
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override public String getFormattedValue(float value) {
                int idx = Math.round(value);
                if (idx>=0 && idx<xTimes.size()) {
                    SimpleDateFormat fmt = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
                    fmt.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Shanghai"));
                    return fmt.format(new Date(xTimes.get(idx)));
                }
                return "";
            }
        });
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.invalidate();
    }

    private String iso(long ms) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        fmt.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        return fmt.format(new Date(ms));
    }
    private long parseIso(String s) {
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            fmt.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            return fmt.parse(s).getTime();
        } catch (Exception e1) {
            try {
                SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                fmt.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                return fmt.parse(s).getTime();
            } catch (Exception e2) {
                return System.currentTimeMillis();
            }
        }
    }
}