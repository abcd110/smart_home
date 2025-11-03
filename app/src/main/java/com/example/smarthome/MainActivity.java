package com.example.smarthome;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.smarthome.ui.home.HomeFragment;
import com.example.smarthome.ui.features.FeaturesFragment;
import com.example.smarthome.ui.profile.ProfileFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private FragmentManager fragmentManager;
    
    // Fragment实例
    private HomeFragment homeFragment;
    private FeaturesFragment featuresFragment;
    private ProfileFragment profileFragment;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        initFragments();
        setupBottomNavigation();
        
        // 默认显示主页
        if (savedInstanceState == null) {
            showFragment(homeFragment);
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        }
    }

    private void initViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation);
        fragmentManager = getSupportFragmentManager();
    }

    private void initFragments() {
        homeFragment = new HomeFragment();
        featuresFragment = new FeaturesFragment();
        profileFragment = new ProfileFragment();
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_home) {
                showFragment(homeFragment);
                return true;
            } else if (itemId == R.id.nav_features) {
                showFragment(featuresFragment);
                return true;
            } else if (itemId == R.id.nav_profile) {
                showFragment(profileFragment);
                return true;
            }
            
            return false;
        });
    }

    private void showFragment(Fragment fragment) {
        if (currentFragment == fragment) {
            return;
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        
        // 隐藏当前Fragment
        if (currentFragment != null) {
            transaction.hide(currentFragment);
        }
        
        // 显示目标Fragment
        if (fragment.isAdded()) {
            transaction.show(fragment);
        } else {
            transaction.add(R.id.nav_host_fragment, fragment);
        }
        
        transaction.commit();
        currentFragment = fragment;
    }
}