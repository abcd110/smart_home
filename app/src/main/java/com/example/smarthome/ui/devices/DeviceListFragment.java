package com.example.smarthome.ui.devices;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.smarthome.databinding.FragmentDeviceListBinding;
import com.example.smarthome.model.DeviceItem;
import com.example.smarthome.ui.home.DeviceAdapter;
import com.example.smarthome.ui.home.HomeViewModel;
import java.util.ArrayList;
import java.util.List;

public class DeviceListFragment extends Fragment {
    private FragmentDeviceListBinding binding;
    private HomeViewModel viewModel;
    private DeviceAdapter adapter;
    private final List<DeviceItem> allDevices = new ArrayList<>();
    private final List<DeviceItem> filtered = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDeviceListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        adapter = new DeviceAdapter(this::onDeviceClick, this::onToggle);
        binding.recyclerDevices.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerDevices.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.loadDevices();
            binding.swipeRefresh.setRefreshing(false);
        });

        binding.editSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyFilter(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        binding.chipOnline.setOnCheckedChangeListener((buttonView, isChecked) -> applyFilter());
        binding.chipActive.setOnCheckedChangeListener((buttonView, isChecked) -> applyFilter());
        binding.chipAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.chipOnline.setChecked(false);
                binding.chipActive.setChecked(false);
                applyFilter();
            }
        });

        viewModel.getDeviceList().observe(getViewLifecycleOwner(), devices -> {
            allDevices.clear();
            if (devices != null) allDevices.addAll(devices);
            applyFilter();
        });

        viewModel.loadDevices();
    }

    private void onDeviceClick(DeviceItem device) {
        String type = device.getType() == null ? "" : device.getType().toLowerCase();
        if (isActuator(type)) {
            com.example.smarthome.ui.control.DeviceControlFragment fragment = com.example.smarthome.ui.control.DeviceControlFragment.newInstance(device.getDeviceId());
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(com.example.smarthome.R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        } else {
            android.widget.Toast.makeText(requireContext(), "该设备为传感器，仅支持查看", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isActuator(String type) {
        if (type == null) return false;
        switch (type) {
            case "actuator":
            case "light":
            case "outlet":
            case "humidifier":
            case "curtain":
            case "lock":
            case "buzzer":
                return true;
            default:
                return false;
        }
    }

    private void onToggle(DeviceItem device, boolean enabled) {
        viewModel.toggleDeviceState(device, enabled);
    }

    private void applyFilter() {
        String q = binding.editSearch.getText() != null ? binding.editSearch.getText().toString().trim().toLowerCase() : "";
        boolean onlyOnline = binding.chipOnline.isChecked();
        boolean onlyActive = binding.chipActive.isChecked();
        filtered.clear();
        for (DeviceItem d : allDevices) {
            if (onlyOnline && !d.isOnline()) continue;
            if (onlyActive && !d.isActive()) continue;
            if (!q.isEmpty()) {
                String name = d.getName() != null ? d.getName().toLowerCase() : "";
                String type = d.getType() != null ? d.getType().toLowerCase() : "";
                if (!name.contains(q) && !type.contains(q)) continue;
            }
            filtered.add(d);
        }
        if (adapter != null) {
            adapter.setDevices(filtered);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
