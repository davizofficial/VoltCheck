package com.voltcheck.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import com.voltcheck.app.utils.LocaleHelper;

public class VoltCheckApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Theme
        SharedPreferences prefs = getSharedPreferences("VoltCheckSettings", MODE_PRIVATE);
        int theme = prefs.getInt("theme", 2); // Default: System Default
        
        switch (theme) {
            case 0: // Light
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case 1: // Dark
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case 2: // System Default
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "en"));
    }
}
