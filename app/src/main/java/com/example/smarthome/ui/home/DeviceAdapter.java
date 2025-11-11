package com.example.smarthome.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarthome.R;
import com.example.smarthome.databinding.ItemDeviceBinding;
import com.example.smarthome.model.DeviceItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 设备列表适配器，用于在RecyclerView中显示设备信息
 */
public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    private List<DeviceItem> deviceList = new ArrayList<>();
    private OnDeviceClickListener deviceClickListener;
    private OnDeviceToggleListener deviceToggleListener;

    public interface OnDeviceClickListener {
        void onDeviceClick(DeviceItem device);
    }

    public interface OnDeviceToggleListener {
        void onDeviceToggle(DeviceItem device, boolean isChecked);
    }

    public DeviceAdapter(OnDeviceClickListener deviceClickListener, OnDeviceToggleListener deviceToggleListener) {
        this.deviceClickListener = deviceClickListener;
        this.deviceToggleListener = deviceToggleListener;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemDeviceBinding binding = DataBindingUtil.inflate(
                layoutInflater, R.layout.item_device, parent, false);
        return new DeviceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        DeviceItem device = deviceList.get(position);
        holder.bind(device);
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    /**
     * 设置设备列表数据
     * @param devices 设备列表
     */
    public void setDevices(List<DeviceItem> devices) {
        this.deviceList = new ArrayList<>(devices);
        notifyDataSetChanged();
    }

    /**
     * 添加单个设备
     * @param device 设备对象
     */
    public void addDevice(DeviceItem device) {
        deviceList.add(device);
        notifyItemInserted(deviceList.size() - 1);
    }

    /**
     * 更新设备
     * @param updatedDevice 更新后的设备对象
     */
    public void updateDevice(DeviceItem updatedDevice) {
        for (int i = 0; i < deviceList.size(); i++) {
            if (deviceList.get(i).getDeviceId().equals(updatedDevice.getDeviceId())) {
                deviceList.set(i, updatedDevice);
                notifyItemChanged(i);
                break;
            }
        }
    }

    /**
     * 删除设备
     * @param deviceId 设备ID
     */
    public void removeDevice(String deviceId) {
        for (int i = 0; i < deviceList.size(); i++) {
            if (deviceList.get(i).getDeviceId().equals(deviceId)) {
                deviceList.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    /**
     * 获取设备列表
     * @return 当前设备列表
     */
    public List<DeviceItem> getDevices() {
        return new ArrayList<>(deviceList);
    }

    /**
     * 设备视图持有者
     */
    class DeviceViewHolder extends RecyclerView.ViewHolder {

        private final ItemDeviceBinding binding;

        public DeviceViewHolder(@NonNull ItemDeviceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * 绑定设备数据到视图
         * @param device 设备对象
         */
        public void bind(final DeviceItem device) {
            binding.setDevice(device);
            binding.setOnClickListener(v -> {
                if (deviceClickListener != null) {
                    deviceClickListener.onDeviceClick(device);
                }
            });

            // 设置开关状态变化监听
            binding.switchDevice.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (deviceToggleListener != null) {
                    deviceToggleListener.onDeviceToggle(device, isChecked);
                }
            });

            // 刷新绑定
            binding.executePendingBindings();
        }
    }
}