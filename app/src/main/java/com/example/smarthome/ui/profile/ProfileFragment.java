package com.example.smarthome.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.smarthome.R;
import android.widget.Toast;
import com.example.smarthome.auth.AuthService;
import android.content.Intent;
import com.example.smarthome.auth.LoginActivity;

public class ProfileFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        TextView name = v.findViewById(R.id.tv_user_name);
        TextView role = v.findViewById(R.id.tv_user_role);
        Button logout = v.findViewById(R.id.btn_logout);
        AuthService auth = new AuthService(requireContext());
        String email = auth.getCurrentUserEmail();
        if (email != null) name.setText(email);
        logout.setOnClickListener(view -> {
            auth.signOut(new AuthService.AuthCallback() {
                @Override public void onSuccess(AuthService.AuthResponse response) {
                    if (getActivity()==null) return;
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "已退出登录", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(requireContext(), LoginActivity.class);
                        startActivity(i);
                        getActivity().finish();
                    });
                }
                @Override public void onError(String error) {
                    if (getActivity()==null) return;
                    getActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "登出失败: "+error, Toast.LENGTH_SHORT).show());
                }
            });
        });
        return v;
    }
}
