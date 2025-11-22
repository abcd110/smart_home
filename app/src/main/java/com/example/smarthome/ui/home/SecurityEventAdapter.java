package com.example.smarthome.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smarthome.R;
import com.example.smarthome.model.SecurityEvent;
import java.util.ArrayList;
import java.util.List;

public class SecurityEventAdapter extends RecyclerView.Adapter<SecurityEventAdapter.VH> {
    private final List<SecurityEvent> items = new ArrayList<>();
    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_security_event, parent, false);
        return new VH(v);
    }
    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        SecurityEvent e = items.get(pos);
        String title = e.isHandled() ? ("[已处理] " + e.getMessage()) : e.getMessage();
        h.title.setText(title);
        h.subtitle.setText(e.getAt());
    }
    @Override public int getItemCount() { return items.size(); }
    public void setItems(List<SecurityEvent> list) { items.clear(); if (list!=null) items.addAll(list); notifyDataSetChanged(); }
    static class VH extends RecyclerView.ViewHolder {
        TextView title, subtitle;
        VH(View v){ super(v); title=v.findViewById(R.id.text_event_title); subtitle=v.findViewById(R.id.text_event_subtitle);} }
}