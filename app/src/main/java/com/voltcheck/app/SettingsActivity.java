package com.voltcheck.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import android.text.TextWatcher;
import android.text.Editable;
import com.google.android.material.slider.Slider;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.voltcheck.app.utils.LocaleHelper;
import com.voltcheck.app.utils.BatteryDataLogger;

/**
 * SettingsActivity - Halaman pengaturan aplikasi
 */
public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "VoltCheckSettings";
    private SharedPreferences prefs;
    
    // UI Components - General
    private Spinner spinnerLanguage;
    private Spinner spinnerTheme;
    private Spinner spinnerRefreshInterval;
    
    // UI Components - Notifications
    private SwitchCompat switchFastCharging;
    private SwitchCompat switchSlowCharging;
    private SwitchCompat switchBatteryFull;
    private SwitchCompat switchTemperature;
    
    // UI Components - Alarm
    private SwitchCompat switchFullChargeAlarm;
    private Slider sliderAlarmLevel;
    private TextView tvAlarmLevel;
    
    // UI Components - Battery Info
    private SwitchCompat switchShowTimeToFull;
    private EditText etDesignCapacity;
    
    // UI Components - Measurement
    
    // UI Components - Measurement
    private Spinner spinnerUnits;
    private SwitchCompat switchDecimalPrecision;
    
    // UI Components - Data
    private SwitchCompat switchSessionSummary;
    // btnExportData removed - now using LinearLayout directly
    
    // UI Components - Permissions
    private SwitchCompat switchBackgroundService;
    
    // UI Components - About
    private TextView tvAppVersion;
    private TextView tvDeveloper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before super.onCreate
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int theme = prefs.getInt("theme", 2); // 0: Light, 1: Dark, 2: System
        switch (theme) {
            case 0: // Light
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                    androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
                );
                break;
            case 1: // Dark
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                    androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                );
                break;
            case 2: // System Default
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                    androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                );
                break;
        }
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        // Initialize views
        initializeViews();
        
        // Load saved settings
        loadSettings();
        
        // Setup listeners
        setupListeners();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }
    
    /**
     * Initialize all views
     */
    private void initializeViews() {
        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        // General
        spinnerLanguage = findViewById(R.id.spinnerLanguage);
        spinnerTheme = findViewById(R.id.spinnerTheme);
        spinnerRefreshInterval = findViewById(R.id.spinnerRefreshInterval);
        
        // Notifications
        switchFastCharging = findViewById(R.id.switchFastCharging);
        switchSlowCharging = findViewById(R.id.switchSlowCharging);
        switchBatteryFull = findViewById(R.id.switchBatteryFull);
        switchTemperature = findViewById(R.id.switchTemperature);
        
        // Alarm
        switchFullChargeAlarm = findViewById(R.id.switchFullChargeAlarm);
        sliderAlarmLevel = findViewById(R.id.sliderAlarmLevel);
        tvAlarmLevel = findViewById(R.id.tvAlarmLevel);
        
        // Battery Info
        switchShowTimeToFull = findViewById(R.id.switchShowTimeToFull);
        etDesignCapacity = findViewById(R.id.etDesignCapacity);
        
        // Measurement
        
        // Measurement
        spinnerUnits = findViewById(R.id.spinnerUnits);
        switchDecimalPrecision = findViewById(R.id.switchDecimalPrecision);
        
        // Data
        switchSessionSummary = findViewById(R.id.switchSessionSummary);
        // btnExportData is now a LinearLayout, not a Button
        
        // Permissions
        switchBackgroundService = findViewById(R.id.switchBackgroundService);
        
        // About
        tvAppVersion = findViewById(R.id.tvAppVersion);
        tvDeveloper = findViewById(R.id.tvDeveloper);
        
        // Set app version
        try {
            String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            tvAppVersion.setText(version);
        } catch (Exception e) {
            tvAppVersion.setText("1.0");
        }
        
        // tvDeveloper is hidden in new UI
        if (tvDeveloper != null) {
            tvDeveloper.setText("github.com/davizofficial");
        }
        
        // Setup spinners
        setupSpinners();
    }
    
    /**
     * Setup all spinners with data
     */
    private void setupSpinners() {
        // Language spinner
        ArrayAdapter<CharSequence> languageAdapter = ArrayAdapter.createFromResource(
            this, R.array.language_options, android.R.layout.simple_spinner_item
        );
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(languageAdapter);
        
        // Theme spinner
        ArrayAdapter<CharSequence> themeAdapter = ArrayAdapter.createFromResource(
            this, R.array.theme_options, android.R.layout.simple_spinner_item
        );
        themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTheme.setAdapter(themeAdapter);
        
        // Refresh interval spinner
        ArrayAdapter<CharSequence> intervalAdapter = ArrayAdapter.createFromResource(
            this, R.array.refresh_interval_options, android.R.layout.simple_spinner_item
        );
        intervalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRefreshInterval.setAdapter(intervalAdapter);
        
        // Units spinner
        ArrayAdapter<CharSequence> unitsAdapter = ArrayAdapter.createFromResource(
            this, R.array.units_options, android.R.layout.simple_spinner_item
        );
        unitsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnits.setAdapter(unitsAdapter);
    }
    
    /**
     * Load saved settings from SharedPreferences
     */
    private void loadSettings() {
        // Load Dark Mode
        int theme = prefs.getInt("theme", 2); // 0: Light, 1: Dark, 2: System
        SwitchCompat switchDarkMode = findViewById(R.id.switchDarkMode);
        switchDarkMode.setChecked(theme == 1);
        
        // Load Language
        String language = LocaleHelper.getLanguage(this);
        TextView tvLanguageValue = findViewById(R.id.tvLanguageValue);
        tvLanguageValue.setText(language.equals("in") ? "Indonesia" : "English");
        
        // Load Show Notifications
        SwitchCompat switchShowNotifications = findViewById(R.id.switchShowNotifications);
        switchShowNotifications.setChecked(prefs.getBoolean("show_notifications", true));
        
        // Load Battery Full Alert
        switchBatteryFull.setChecked(prefs.getBoolean("alert_battery_full", true));
        
        // Load Battery Low Alert (Slow Charging)
        switchSlowCharging.setChecked(prefs.getBoolean("alert_slow_charging", true));
        
        // Load Refresh Interval
        int refreshInterval = prefs.getInt("refresh_interval", 1);
        String[] intervals = {"0.5s", "1s", "2s", "5s"};
        TextView tvRefreshIntervalValue = findViewById(R.id.tvRefreshIntervalValue);
        tvRefreshIntervalValue.setText(intervals[refreshInterval]);
        
        // Load Fast Charging Alert
        switchFastCharging.setChecked(prefs.getBoolean("alert_fast_charging", true));
        
        // Load Temperature Alert
        switchTemperature.setChecked(prefs.getBoolean("alert_temperature", true));
        
        // Load Full Charge Alarm
        switchFullChargeAlarm.setChecked(prefs.getBoolean("alarm_full_charge", false));
        int savedAlarmLevel = prefs.getInt("alarm_level", 100);
        sliderAlarmLevel.setValue(savedAlarmLevel);
        tvAlarmLevel.setText(String.format(getString(R.string.alarm_level_format), savedAlarmLevel));
        
        // Load Show Time to Full
        switchShowTimeToFull.setChecked(prefs.getBoolean("show_time_to_full", false));
        
        // Load Design Capacity
        etDesignCapacity.setText(String.valueOf(prefs.getInt("design_capacity", 4500)));
        
        // Load Units
        int units = prefs.getInt("units", 0);
        String[] unitsArray = {"mA", "Ampere"};
        TextView tvUnitsValue = findViewById(R.id.tvUnitsValue);
        tvUnitsValue.setText(unitsArray[units]);
        
        // Load Decimal Precision
        switchDecimalPrecision.setChecked(prefs.getBoolean("decimal_precision", true));
        
        // Load Background Service
        switchBackgroundService.setChecked(prefs.getBoolean("background_service", false));
        
        // Load App Version
        try {
            String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            tvAppVersion.setText(version);
        } catch (Exception e) {
            tvAppVersion.setText("1.0");
        }
        
        // Hidden spinners for compatibility
        if (language.equals("in")) {
            spinnerLanguage.setSelection(1);
        } else {
            spinnerLanguage.setSelection(0);
        }
        spinnerTheme.setSelection(prefs.getInt("theme", 2));
        spinnerRefreshInterval.setSelection(prefs.getInt("refresh_interval", 1));
        
        // Hidden switches for compatibility
        switchFastCharging.setChecked(prefs.getBoolean("alert_fast_charging", true));
        switchTemperature.setChecked(prefs.getBoolean("alert_temperature", true));
        switchFullChargeAlarm.setChecked(prefs.getBoolean("alarm_full_charge", false));
        
        int alarmLevel = prefs.getInt("alarm_level", 100);
        sliderAlarmLevel.setValue(alarmLevel);
        tvAlarmLevel.setText(String.format(getString(R.string.alarm_level_format), alarmLevel));
        
        switchShowTimeToFull.setChecked(prefs.getBoolean("show_time_to_full", false));
        etDesignCapacity.setText(String.valueOf(prefs.getInt("design_capacity", 4500)));
        
        spinnerUnits.setSelection(prefs.getInt("units", 0));
        switchDecimalPrecision.setChecked(prefs.getBoolean("decimal_precision", true));
        switchSessionSummary.setChecked(prefs.getBoolean("session_summary", false));
        switchBackgroundService.setChecked(prefs.getBoolean("background_service", false));
    }
    
    /**
     * Setup all listeners
     */
    private void setupListeners() {
        // Dark Mode Switch
        SwitchCompat switchDarkMode = findViewById(R.id.switchDarkMode);
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) return; // Ignore programmatic changes
            
            int theme = isChecked ? 1 : 0; // 0: Light, 1: Dark
            prefs.edit().putInt("theme", theme).apply();
            
            // Apply theme
            if (isChecked) {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                    androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                );
            } else {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                    androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
                );
            }
            
            Toast.makeText(SettingsActivity.this, "Theme changed. Restarting...", Toast.LENGTH_SHORT).show();
            
            // Restart activity
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                recreate();
            }, 500);
        });
        
        // Language Item Click
        TextView tvLanguageValue = findViewById(R.id.tvLanguageValue);
        findViewById(R.id.itemLanguage).setOnClickListener(v -> {
            String[] languages = {"English", "Indonesia"};
            String currentLang = LocaleHelper.getLanguage(this);
            int selectedIndex = currentLang.equals("in") ? 1 : 0;
            
            new MaterialAlertDialogBuilder(this)
                .setTitle("Select Language")
                .setSingleChoiceItems(languages, selectedIndex, (dialog, which) -> {
                    String newLang = which == 1 ? "in" : "en";
                    if (!newLang.equals(currentLang)) {
                        LocaleHelper.setLocale(this, newLang);
                        tvLanguageValue.setText(languages[which]);
                        
                        // Restart app
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
        });
        
        // Show Notifications Switch
        SwitchCompat switchShowNotifications = findViewById(R.id.switchShowNotifications);
        switchShowNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) return; // Ignore programmatic changes
            prefs.edit().putBoolean("show_notifications", isChecked).apply();
            Toast.makeText(this, "Notifications " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });
        
        // Battery Full Alert Switch
        switchBatteryFull.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) return;
            prefs.edit().putBoolean("alert_battery_full", isChecked).apply();
            Toast.makeText(this, "Battery full alert " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });
        
        // Battery Low Alert Switch (Slow Charging)
        switchSlowCharging.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) return;
            prefs.edit().putBoolean("alert_slow_charging", isChecked).apply();
            Toast.makeText(this, "Battery low alert " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });
        
        // Clear History Click
        findViewById(R.id.itemClearHistory).setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                .setTitle("Clear History")
                .setMessage("Are you sure you want to clear all battery data history?")
                .setPositiveButton("Clear", (dialog, which) -> {
                    BatteryDataLogger.clearDataLog(this);
                    Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
        });
        
        // App Permissions Click
        findViewById(R.id.itemAppPermissions).setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            startActivity(intent);
        });
        
        // Terms of Service Click
        findViewById(R.id.itemTermsOfService).setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                .setTitle("Terms of Service")
                .setMessage("VoltCheck Terms of Service\n\n" +
                    "By using this app, you agree to:\n" +
                    "• Use the app for personal battery monitoring\n" +
                    "• Not hold the developer liable for any battery issues\n" +
                    "• Accept that battery readings may vary by device")
                .setPositiveButton("OK", null)
                .show();
        });
        
        // About Developer Click
        findViewById(R.id.btnAboutDeveloper).setOnClickListener(v -> {
            String message = "VoltCheck - Battery Monitoring Application\n\n" +
                    "OPEN SOURCE PROJECT\n\n" +
                    "This project is open source and free to use for anyone. " +
                    "You are welcome to use, modify, and distribute this application " +
                    "for educational and learning purposes.\n\n" +
                    "DEVELOPER\n" +
                    "Created by davizofficial\n\n" +
                    "GITHUB REPOSITORY\n" +
                    "https://github.com/davizofficial/VoltCheck\n\n" +
                    "Feel free to contribute, report issues, or fork the project. " +
                    "Your feedback and contributions are highly appreciated!\n\n" +
                    "LICENSE\n" +
                    "This project is provided as-is for educational purposes. " +
                    "Please check the GitHub repository for detailed license information.";
            
            new MaterialAlertDialogBuilder(this)
                .setTitle("About Developer")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .setNeutralButton("Visit GitHub", (dialog, which) -> {
                    try {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
                            Uri.parse("https://github.com/davizofficial/VoltCheck"));
                        startActivity(browserIntent);
                    } catch (Exception e) {
                        Toast.makeText(this, "Unable to open browser", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
        });
        
        // Refresh Interval Click
        TextView tvRefreshIntervalValue = findViewById(R.id.tvRefreshIntervalValue);
        findViewById(R.id.itemRefreshInterval).setOnClickListener(v -> {
            String[] intervals = {"0.5s", "1s", "2s", "5s"};
            int currentInterval = prefs.getInt("refresh_interval", 1);
            
            new MaterialAlertDialogBuilder(this)
                .setTitle("Refresh Interval")
                .setSingleChoiceItems(intervals, currentInterval, (dialog, which) -> {
                    prefs.edit().putInt("refresh_interval", which).apply();
                    tvRefreshIntervalValue.setText(intervals[which]);
                    Toast.makeText(this, "Refresh interval set to " + intervals[which], Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
        });
        
        // Fast Charging Alert Switch
        switchFastCharging.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) return;
            prefs.edit().putBoolean("alert_fast_charging", isChecked).apply();
            Toast.makeText(this, "Fast charging alert " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });
        
        // Temperature Alert Switch
        switchTemperature.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) return;
            prefs.edit().putBoolean("alert_temperature", isChecked).apply();
            Toast.makeText(this, "Temperature alert " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });
        
        // Full Charge Alarm Switch
        switchFullChargeAlarm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) return;
            prefs.edit().putBoolean("alarm_full_charge", isChecked).apply();
            Toast.makeText(this, "Full charge alarm " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });
        
        // Alarm Level Slider
        sliderAlarmLevel.addOnChangeListener((slider, value, fromUser) -> {
            int level = (int) value;
            tvAlarmLevel.setText(String.format(getString(R.string.alarm_level_format), level));
            prefs.edit().putInt("alarm_level", level).apply();
        });
        
        // Show Time to Full Switch
        switchShowTimeToFull.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) return;
            prefs.edit().putBoolean("show_time_to_full", isChecked).apply();
            Toast.makeText(this, "Time to full " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });
        
        // Design Capacity EditText
        etDesignCapacity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    if (s.length() > 0) {
                        int capacity = Integer.parseInt(s.toString());
                        prefs.edit().putInt("design_capacity", capacity).apply();
                    }
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        });
        
        // Units Click
        TextView tvUnitsValue = findViewById(R.id.tvUnitsValue);
        findViewById(R.id.itemUnits).setOnClickListener(v -> {
            String[] units = {"mA", "Ampere"};
            int currentUnit = prefs.getInt("units", 0);
            
            new MaterialAlertDialogBuilder(this)
                .setTitle("Select Units")
                .setSingleChoiceItems(units, currentUnit, (dialog, which) -> {
                    prefs.edit().putInt("units", which).apply();
                    tvUnitsValue.setText(units[which]);
                    Toast.makeText(this, "Unit changed to " + units[which], Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
        });
        
        // Decimal Precision Switch
        switchDecimalPrecision.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) return;
            prefs.edit().putBoolean("decimal_precision", isChecked).apply();
            Toast.makeText(this, "Decimal precision " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });
        
        // Export Data Click (btnExportData is now a LinearLayout)
        findViewById(R.id.btnExportData).setOnClickListener(v -> exportData());
        
        // Background Service Switch
        switchBackgroundService.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) return;
            prefs.edit().putBoolean("background_service", isChecked).apply();
            
            Intent serviceIntent = new Intent(SettingsActivity.this, BatteryService.class);
            if (isChecked) {
                // Start background service
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent);
                } else {
                    startService(serviceIntent);
                }
                Toast.makeText(this, "Background service enabled", Toast.LENGTH_SHORT).show();
            } else {
                // Stop background service
                stopService(serviceIntent);
                Toast.makeText(this, "Background service disabled", Toast.LENGTH_SHORT).show();
            }
        });
        
        // General - Language (Hidden Spinner - for compatibility)
        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLang = position == 1 ? "in" : "en";
                String currentLang = LocaleHelper.getLanguage(SettingsActivity.this);
                
                if (!selectedLang.equals(currentLang)) {
                    LocaleHelper.setLocale(SettingsActivity.this, selectedLang);
                    
                    // Restart app to apply language
                    Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // General - Theme
        spinnerTheme.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int oldTheme = prefs.getInt("theme", 2);
                if (oldTheme != position) {
                    prefs.edit().putInt("theme", position).apply();
                    
                    // Apply theme
                    switch (position) {
                        case 0: // Light
                            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                                androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
                            );
                            break;
                        case 1: // Dark
                            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                                androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                            );
                            break;
                        case 2: // System Default
                            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                                androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                            );
                            break;
                    }
                    
                    Toast.makeText(SettingsActivity.this, "Theme changed. Restarting app...", Toast.LENGTH_SHORT).show();
                    
                    // Restart activity to apply theme
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        recreate();
                    }, 500);
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // General - Refresh Interval
        spinnerRefreshInterval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                prefs.edit().putInt("refresh_interval", position).apply();
                
                // Get interval in milliseconds
                long interval = getRefreshIntervalMs(position);
                String intervalText = parent.getItemAtPosition(position).toString();
                
                Toast.makeText(SettingsActivity.this, 
                    "Refresh interval set to " + intervalText, 
                    Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // Notifications
        switchFastCharging.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("alert_fast_charging", isChecked).apply();
            Toast.makeText(this, "Fast charging alert " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });
        
        switchSlowCharging.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("alert_slow_charging", isChecked).apply();
            Toast.makeText(this, "Slow charging alert " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });
        
        switchBatteryFull.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("alert_battery_full", isChecked).apply();
            Toast.makeText(this, "Battery full alert " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });
        
        switchTemperature.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("alert_temperature", isChecked).apply();
            Toast.makeText(this, "Temperature alert " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });
        
        // Alarm
        switchFullChargeAlarm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("alarm_full_charge", isChecked).apply();
        });
        
        sliderAlarmLevel.addOnChangeListener((slider, value, fromUser) -> {
            int level = (int) value;
            tvAlarmLevel.setText(String.format(getString(R.string.alarm_level_format), level));
            prefs.edit().putInt("alarm_level", level).apply();
        });
        
        // Battery Info
        switchShowTimeToFull.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("show_time_to_full", isChecked).apply();
        });
        
        etDesignCapacity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    if (s.length() > 0) {
                        int capacity = Integer.parseInt(s.toString());
                        prefs.edit().putInt("design_capacity", capacity).apply();
                    }
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        });
        
        // Measurement - Units
        
        // Measurement - Units
        spinnerUnits.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                prefs.edit().putInt("units", position).apply();
                String unit = parent.getItemAtPosition(position).toString();
                Toast.makeText(SettingsActivity.this, "Unit changed to " + unit, Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        switchDecimalPrecision.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("decimal_precision", isChecked).apply();
            Toast.makeText(this, "Decimal precision " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });
        
        // Data
        switchSessionSummary.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("session_summary", isChecked).apply();
            Toast.makeText(this, "Session summary " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });
        
        // Export Data Click - already added above in new listeners section
        
        // Permissions - Battery Optimization
        findViewById(R.id.btnBatteryOptimization).setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            startActivity(intent);
        });
        
        switchBackgroundService.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("background_service", isChecked).apply();
            
            Intent serviceIntent = new Intent(SettingsActivity.this, BatteryService.class);
            if (isChecked) {
                // Start background service
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent);
                } else {
                    startService(serviceIntent);
                }
                Toast.makeText(this, "Background service enabled", Toast.LENGTH_SHORT).show();
            } else {
                // Stop background service
                stopService(serviceIntent);
                Toast.makeText(this, "Background service disabled", Toast.LENGTH_SHORT).show();
            }
        });
        
        // About - Change Log
        findViewById(R.id.btnChangeLog).setOnClickListener(v -> showChangeLog());
        
        // About - Privacy Policy
        findViewById(R.id.btnPrivacyPolicy).setOnClickListener(v -> showPrivacyPolicy());
    }
    
    /**
     * Export data to CSV
     */
    private void exportData() {
        try {
            // Get logged data
            java.util.List<BatteryDataLogger.BatteryData> dataList = BatteryDataLogger.getDataLog(this);
            
            if (dataList.isEmpty()) {
                Toast.makeText(this, "No data to export. Please monitor battery for a while first.", Toast.LENGTH_LONG).show();
                return;
            }
            
            // Create filename with timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String filename = "voltcheck_export_" + timestamp + ".csv";
            
            // Get external storage directory
            File exportDir = new File(getExternalFilesDir(null), "exports");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            
            File file = new File(exportDir, filename);
            FileWriter writer = new FileWriter(file);
            
            // Write CSV header
            writer.append("Timestamp,Current (mA),Voltage (V),Temperature (°C),Level (%),Status\n");
            
            // Write real data
            for (BatteryDataLogger.BatteryData data : dataList) {
                writer.append(data.timestamp);
                writer.append(",");
                writer.append(String.format(Locale.US, "%.2f", data.current));
                writer.append(",");
                writer.append(String.format(Locale.US, "%.3f", data.voltage));
                writer.append(",");
                writer.append(String.format(Locale.US, "%.1f", data.temperature));
                writer.append(",");
                writer.append(String.valueOf(data.level));
                writer.append(",");
                writer.append(data.status.replace("\n", " "));
                writer.append("\n");
            }
            
            writer.flush();
            writer.close();
            
            // Show success message
            String message = String.format("Data exported: %d entries\n\nFile: %s\nLocation: exports folder", 
                dataList.size(), file.getName());
            
            new MaterialAlertDialogBuilder(this)
                .setTitle("Export Successful")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .setNeutralButton("Share", (dialog, which) -> shareFile(file))
                .setNegativeButton("Clear Data", (dialog, which) -> {
                    BatteryDataLogger.clearDataLog(this);
                    Toast.makeText(this, "Data log cleared", Toast.LENGTH_SHORT).show();
                })
                .show();
                
        } catch (IOException e) {
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * Share exported file
     */
    private void shareFile(File file) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/csv");
            
            // Use FileProvider for Android 7.0+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri fileUri = androidx.core.content.FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".fileprovider",
                    file
                );
                shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            }
            
            startActivity(Intent.createChooser(shareIntent, "Share CSV"));
        } catch (Exception e) {
            Toast.makeText(this, "Share failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Show change log dialog
     */
    private void showChangeLog() {
        String changeLog = "Version 1.0.0\n" +
                "• Battery current monitoring\n" +
                "• Voltage and temperature tracking\n" +
                "• Real-time measurements\n" +
                "• Min/Max tracking\n" +
                "• Dynamic progress bar\n" +
                "• Settings page\n" +
                "• Export data to CSV\n" +
                "• Modern Material Design UI";
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("Change Log")
            .setMessage(changeLog)
            .setPositiveButton("OK", null)
            .show();
    }
    
    /**
     * Show privacy policy dialog
     */
    private void showPrivacyPolicy() {
        String policy = "Privacy Policy\n\n" +
                "VoltCheck does not collect any personal data.\n\n" +
                "All battery measurements are processed locally on your device.\n\n" +
                "No data is sent to external servers.";
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("Privacy Policy")
            .setMessage(policy)
            .setPositiveButton("OK", null)
            .show();
    }
    
    /**
     * Get refresh interval in milliseconds
     */
    private long getRefreshIntervalMs(int position) {
        switch (position) {
            case 0: return 500;   // 0.5s
            case 1: return 1000;  // 1s
            case 2: return 2000;  // 2s
            case 3: return 5000;  // 5s
            default: return 1000;
        }
    }
    
    /**
     * Get refresh interval from SharedPreferences
     */
    public static long getRefreshInterval(android.content.Context context) {
        android.content.SharedPreferences prefs = context.getSharedPreferences("VoltCheckSettings", android.content.Context.MODE_PRIVATE);
        int position = prefs.getInt("refresh_interval", 1);
        switch (position) {
            case 0: return 500;
            case 1: return 1000;
            case 2: return 2000;
            case 3: return 5000;
            default: return 1000;
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
