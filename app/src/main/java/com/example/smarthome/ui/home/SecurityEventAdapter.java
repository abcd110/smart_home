package com.example.smarthome.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smarthome.R;
import java.util.List;

public class SecurityEventAdapter extends RecyclerView.Adapter<SecurityEventAdapter.ViewHolder> {

    private List<SecurityEvent> events;

    public SecurityEventAdapter(List<SecurityEvent> events) {
        this.events = events;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_security_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SecurityEvent event = events.get(position);
        holder.eventTitleText.setText(event.getTitle());
        holder.eventTimeText.setText(event.getTime());
        holder.eventDescriptionText.setText(event.getDescription());
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void updateEvents(List<SecurityEvent> newEvents) {
        this.events = newEvents;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView eventTitleText;
        TextView eventTimeText;
        TextView eventDescriptionText;

        ViewHolder(View itemView) {
            super(itemView);
            eventTitleText = itemView.findViewById(R.id.text_event_title);
            eventTimeText = itemView.findViewById(R.id.text_event_time);
            eventDescriptionText = itemView.findViewById(R.id.text_event_description);
        }
    }
}