package com.voltcheck.app;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;
import com.voltcheck.app.utils.NotificationUtil;
import com.voltcheck.app.utils.LocaleHelper;
import com.voltcheck.app.utils.BatteryDataLogger;
import java.util.Locale;

import java.util.ArrayList;
import java.util.List;

/**
 * MainActivity - Tampilan utama untuk monitoring realtime
 */
public class MainActivity extends AppCompatActivity {
    
    private static final String TAG = "MainActivity";
    private static final int MAX_CHART_ENTRIES = 60; // 60 data points
    private static final int SAMPLE_SIZE = 5; // Jumlah sample untuk averaging
    
    private long UPDATE_INTERVAL = 1000; // Default 1 detik, akan diupdate dari Settings
    
    // UI Components
    private TextView tvCurrentMain, tvMinCurrentLabel, tvMaxCurrentLabel;
    private TextView tvStatus, tvChargingType, tvLevel, tvHealth, tvTechnology, tvCapacity, tvTemperature, tvVoltage;
    private TextView tvManufacturer, tvModel, tvAndroidVersion, tvBuildId;
    private LineChart chartCurrent, chartVoltage, chartTemperature, chartLevel;
    
    // Battery Manager
    private BatteryManager batteryManager;
    private Handler handler;
    private Runnable updateRunnable;
    
    // Chart data
    private List<Entry> currentEntries = new ArrayList<>();
    private List<Entry> voltageEntries = new ArrayList<>();
    private List<Entry> temperatureEntries = new ArrayList<>();
    private List<Entry> levelEntries = new ArrayList<>();
    private int chartIndex = 0;
    
    // Compatibility
    private boolean isCurrentSupported = true;
    private SharedPreferences preferences;
    
    // Current measurement
    private List<Float> currentSamples = new ArrayList<>();
    private float minCurrent = Float.MAX_VALUE;
    private float maxCurrent = Float.MIN_VALUE;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before super.onCreate
        preferences = getSharedPreferences("VoltCheckSettings", MODE_PRIVATE);
        int theme = preferences.getInt("theme", 2); // 0: Light, 1: Dark, 2: System
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
        setContentView(R.layout.activity_main);
        
        // Initialize
        batteryManager = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);
        handler = new Handler(Looper.getMainLooper());
        
        // Setup UI
        initializeViews();
        initializeCharts();
        setupClickListeners();
        
        // Create notification channels
        NotificationUtil.createNotificationChannels(this);
        
        // Check compatibility
        checkCompatibility();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Load refresh interval from settings
        UPDATE_INTERVAL = SettingsActivity.getRefreshInterval(this);
        startMonitoring();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        stopMonitoring();
    }
    
    /**
     * Initialize all views
     */
    private void initializeViews() {
        // Main display
        tvCurrentMain = findViewById(R.id.tvCurrentMain);
        tvMinCurrentLabel = findViewById(R.id.tvMinCurrentLabel);
        tvMaxCurrentLabel = findViewById(R.id.tvMaxCurrentLabel);
        
        // Battery info
        tvStatus = findViewById(R.id.tvStatus);
        tvChargingType = findViewById(R.id.tvChargingType);
        tvLevel = findViewById(R.id.tvLevel);
        tvHealth = findViewById(R.id.tvHealth);
        tvTechnology = findViewById(R.id.tvTechnology);
        tvCapacity = findViewById(R.id.tvCapacity);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvVoltage = findViewById(R.id.tvVoltage);
        
        // Device info
        tvManufacturer = findViewById(R.id.tvManufacturer);
        tvModel = findViewById(R.id.tvModel);
        tvAndroidVersion = findViewById(R.id.tvAndroidVersion);
        tvBuildId = findViewById(R.id.tvBuildId);
        
        // Charts (hidden by default)
        chartCurrent = findViewById(R.id.chartCurrent);
        chartVoltage = findViewById(R.id.chartVoltage);
        chartTemperature = findViewById(R.id.chartTemperature);
        chartLevel = findViewById(R.id.chartLevel);
        
        // Initialize device info
        initializeDeviceInfo();
    }
    
    /**
     * Initialize device information
     */
    private void initializeDeviceInfo() {
        tvManufacturer.setText(Build.MANUFACTURER);
        tvModel.setText(Build.MODEL);
        tvAndroidVersion.setText(Build.VERSION.RELEASE + " (" + Build.VERSION.CODENAME + ")");
        tvBuildId.setText(Build.ID);
    }
    
    /**
     * Initialize all charts
     */
    private void initializeCharts() {
        setupChart(chartCurrent, "Arus (mA)", Color.parseColor("#FFD600"));
        setupChart(chartVoltage, "Tegangan (V)", Color.parseColor("#00E676"));
        setupChart(chartTemperature, "Suhu (°C)", Color.parseColor("#FF5252"));
        setupChart(chartLevel, "Level (%)", Color.parseColor("#448AFF"));
    }
    
    /**
     * Setup individual chart
     */
    private void setupChart(LineChart chart, String label, int color) {
        chart.setBackgroundColor(Color.TRANSPARENT);
        chart.setDrawGridBackground(false);
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        
        // Legend
        chart.getLegend().setTextColor(Color.WHITE);
        chart.getLegend().setEnabled(true);
        
        // X Axis
        XAxis xAxis = chart.getXAxis();
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(true);
        xAxis.setGridColor(Color.argb(50, 255, 255, 255));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        
        // Y Axis
        chart.getAxisLeft().setTextColor(Color.WHITE);
        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisLeft().setGridColor(Color.argb(50, 255, 255, 255));
        chart.getAxisRight().setEnabled(false);
        
        // Initial empty dataset
        LineDataSet dataSet = new LineDataSet(new ArrayList<>(), label);
        dataSet.setColor(color);
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
    }
    
    /**
     * Setup click listeners
     */
    private void setupClickListeners() {
        // Settings button
        findViewById(R.id.btnSettings).setOnClickListener(v -> openSettings());
        
        // TODO: Add these buttons to layout if needed
        // Test Mode button
        // findViewById(R.id.btnTestMode).setOnClickListener(v -> openTestMode());
        
        // History button
        // findViewById(R.id.btnHistory).setOnClickListener(v -> openHistory());
        
        // Threshold Settings button
        // findViewById(R.id.btnThreshold).setOnClickListener(v -> openThresholdSettings());
    }
    
    /**
     * Check device compatibility
     */
    private void checkCompatibility() {
        float current = getCurrentNow();
        if (current == 0f) {
            isCurrentSupported = false;
            tvCurrentMain.setText("N/A");
        } else {
            isCurrentSupported = true;
        }
    }
    
    /**
     * Start monitoring battery
     */
    private void startMonitoring() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateBatteryInfo();
                handler.postDelayed(this, UPDATE_INTERVAL);
            }
        };
        handler.post(updateRunnable);
    }
    
    /**
     * Stop monitoring battery
     */
    private void stopMonitoring() {
        if (handler != null && updateRunnable != null) {
            handler.removeCallbacks(updateRunnable);
        }
    }
    
    /**
     * Update battery information
     */
    private void updateBatteryInfo() {
        try {
            // Gunakan smoothed current untuk tampilan yang lebih stabil
            float current = getCurrentSmoothed();
            float voltage = getVoltageNow();
            float temperature = getTemperature();
            int level = getBatteryLevel();
            String chargingType = getChargingType();
            boolean charging = isCharging();
            String health = getBatteryHealth();
            String technology = getBatteryTechnology();
            int capacity = getBatteryCapacity();
            
            // Hitung daya (Power) dalam mW
            float power = voltage * Math.abs(current);
            
            // Update main current display
            if (isCurrentSupported) {
                float displayCurrent;
                if (charging) {
                    displayCurrent = Math.abs(current);
                } else {
                    displayCurrent = -Math.abs(current);
                }
                
                // Tampilkan angka saja tanpa desimal (sesuai UI mockup)
                tvCurrentMain.setText(String.format(Locale.US, "%.0f", displayCurrent));
                
                // Track min/max
                if (displayCurrent < minCurrent) {
                    minCurrent = displayCurrent;
                    tvMinCurrentLabel.setText(String.format("%.0f mA", minCurrent));
                }
                if (displayCurrent > maxCurrent) {
                    maxCurrent = displayCurrent;
                    tvMaxCurrentLabel.setText(String.format("%.0f mA", maxCurrent));
                }
            } else {
                tvCurrentMain.setText("N/A");
            }
            
            // Update battery info
            String statusText = charging ? "Charging" : "Not Charging";
            if (power > 0) {
                statusText += String.format(" (%.1f W)", power / 1000f);
            }
            
            // Time to Full Calculation
            boolean showTimeToFull = preferences.getBoolean("show_time_to_full", false);
            if (showTimeToFull && charging && current > 0) {
                int designCapacity = preferences.getInt("design_capacity", 4500);
                if (level < 100) {
                    float remainingCapacity = (designCapacity * (100 - level)) / 100f;
                    float hours = remainingCapacity / current;
                    int minutes = (int) (hours * 60);
                    
                    String timeString;
                    if (minutes >= 60) {
                        int h = minutes / 60;
                        int m = minutes % 60;
                        timeString = String.format(Locale.getDefault(), "%d h %d min", h, m);
                    } else {
                        timeString = String.format(Locale.getDefault(), "%d min", minutes);
                    }
                    
                    statusText += "\n" + String.format(getString(R.string.time_remaining), timeString);
                }
            }
            
            tvStatus.setText(statusText);
            tvChargingType.setText(chargingType);
            tvLevel.setText(level + "%");
            tvHealth.setText(health);
            tvTechnology.setText(technology);
            tvCapacity.setText(capacity + " mAh");
            tvTemperature.setText(String.format("%.1f °C", temperature));
            tvVoltage.setText(String.format("%.3f V", voltage));
            
            // Update progress bar
            android.widget.ProgressBar progressBar = findViewById(R.id.progressBarCurrent);
            if (progressBar != null) {
                int progress = 50; // Default middle
                if (isCurrentSupported && maxCurrent > minCurrent) {
                    float displayCurrent = charging ? current : -current;
                    progress = (int) (((displayCurrent - minCurrent) / (maxCurrent - minCurrent)) * 100);
                    progress = Math.max(0, Math.min(100, progress));
                }
                progressBar.setProgress(progress);
                
                if (charging) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(
                            getResources().getColor(R.color.progress_charging, getTheme())
                        ));
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(
                            getResources().getColor(R.color.progress_discharging, getTheme())
                        ));
                    }
                }
            }
            
            // Update charts
            updateCharts(current, voltage, temperature, level);
            
            // Log data for export (every 10 seconds to avoid too much data)
            if (chartIndex % 10 == 0) {
                BatteryDataLogger.logData(this, current, voltage, temperature, level, statusText);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating battery info: " + e.getMessage());
        }
    }
    
    /**
     * Update all charts
     */
    private void updateCharts(float current, float voltage, float temperature, int level) {
        chartIndex++;
        
        // Add entries
        if (isCurrentSupported) {
            currentEntries.add(new Entry(chartIndex, current));
            if (currentEntries.size() > MAX_CHART_ENTRIES) {
                currentEntries.remove(0);
            }
        }
        
        voltageEntries.add(new Entry(chartIndex, voltage));
        if (voltageEntries.size() > MAX_CHART_ENTRIES) {
            voltageEntries.remove(0);
        }
        
        temperatureEntries.add(new Entry(chartIndex, temperature));
        if (temperatureEntries.size() > MAX_CHART_ENTRIES) {
            temperatureEntries.remove(0);
        }
        
        levelEntries.add(new Entry(chartIndex, level));
        if (levelEntries.size() > MAX_CHART_ENTRIES) {
            levelEntries.remove(0);
        }
        
        // Update chart data
        if (isCurrentSupported) {
            updateChartData(chartCurrent, currentEntries);
        }
        updateChartData(chartVoltage, voltageEntries);
        updateChartData(chartTemperature, temperatureEntries);
        updateChartData(chartLevel, levelEntries);
    }
    
    /**
     * Update chart data
     */
    private void updateChartData(LineChart chart, List<Entry> entries) {
        LineData data = chart.getData();
        if (data != null && data.getDataSetCount() > 0) {
            LineDataSet dataSet = (LineDataSet) data.getDataSetByIndex(0);
            dataSet.setValues(new ArrayList<>(entries));
            data.notifyDataChanged();
            chart.notifyDataSetChanged();
            chart.invalidate();
        }
    }
    
    // ===== Battery Reading Methods =====
    
    private float getCurrentNow() {
        try {
            int currentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
            if (currentNow != Integer.MIN_VALUE && currentNow != 0) {
                float currentMA;
                if (Math.abs(currentNow) > 10000) {
                    currentMA = currentNow / 1000f;
                } else {
                    currentMA = (float) currentNow;
                }
                return currentMA;
            }
            int currentAvg = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE);
            if (currentAvg != Integer.MIN_VALUE && currentAvg != 0) {
                float currentMA;
                if (Math.abs(currentAvg) > 10000) {
                    currentMA = currentAvg / 1000f;
                } else {
                    currentMA = (float) currentAvg;
                }
                return currentMA;
            }
            return 0f;
        } catch (Exception e) {
            return 0f;
        }
    }
    
    private float getCurrentSmoothed() {
        float rawCurrent = getCurrentNow();
        currentSamples.add(rawCurrent);
        if (currentSamples.size() > SAMPLE_SIZE) {
            currentSamples.remove(0);
        }
        float sum = 0f;
        for (float sample : currentSamples) {
            sum += sample;
        }
        return sum / currentSamples.size();
    }
    
    private float getVoltageNow() {
        try {
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = registerReceiver(null, filter);
            if (batteryStatus != null) {
                int voltage = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
                return voltage / 1000f;
            }
        } catch (Exception e) {}
        return 0f;
    }
    
    private float getTemperature() {
        try {
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = registerReceiver(null, filter);
            if (batteryStatus != null) {
                int temp = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
                return temp / 10f;
            }
        } catch (Exception e) {}
        return 0f;
    }
    
    private int getBatteryLevel() {
        try {
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = registerReceiver(null, filter);
            if (batteryStatus != null) {
                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                return (int) ((level / (float) scale) * 100);
            }
        } catch (Exception e) {}
        return 0;
    }
    
    private String getChargingType() {
        try {
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = registerReceiver(null, filter);
            if (batteryStatus != null) {
                int plugged = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                switch (plugged) {
                    case BatteryManager.BATTERY_PLUGGED_AC: return "AC Charger";
                    case BatteryManager.BATTERY_PLUGGED_USB: return "USB";
                    case BatteryManager.BATTERY_PLUGGED_WIRELESS: return "Wireless";
                    default: return "Battery";
                }
            }
        } catch (Exception e) {}
        return "Unknown";
    }
    
    private String getBatteryHealth() {
        try {
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = registerReceiver(null, filter);
            if (batteryStatus != null) {
                int health = batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
                switch (health) {
                    case BatteryManager.BATTERY_HEALTH_GOOD: return "Good";
                    case BatteryManager.BATTERY_HEALTH_OVERHEAT: return "Overheat";
                    case BatteryManager.BATTERY_HEALTH_DEAD: return "Dead";
                    case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE: return "Over Voltage";
                    case BatteryManager.BATTERY_HEALTH_COLD: return "Cold";
                    default: return "Unknown";
                }
            }
        } catch (Exception e) {}
        return "Unknown";
    }
    
    private String getBatteryTechnology() {
        try {
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = registerReceiver(null, filter);
            if (batteryStatus != null) {
                String tech = batteryStatus.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
                return tech != null ? tech : "Unknown";
            }
        } catch (Exception e) {}
        return "Unknown";
    }
    
    private int getBatteryCapacity() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int capacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
                if (capacity > 0) return capacity / 1000;
            }
            Object powerProfile = Class.forName("com.android.internal.os.PowerProfile")
                    .getConstructor(Context.class).newInstance(this);
            double batteryCapacity = (double) Class.forName("com.android.internal.os.PowerProfile")
                    .getMethod("getBatteryCapacity").invoke(powerProfile);
            return (int) batteryCapacity;
        } catch (Exception e) {}
        return 0;
    }
    
    private boolean isCharging() {
        try {
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = registerReceiver(null, filter);
            if (batteryStatus != null) {
                int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                       status == BatteryManager.BATTERY_STATUS_FULL;
            }
        } catch (Exception e) {}
        return false;
    }
    
    // ===== Navigation Methods =====
    
    private void openTestMode() {
        if (!isCurrentSupported) {
            Toast.makeText(this, "Test Mode memerlukan pembacaan arus yang tidak didukung device Anda", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(this, TestModeActivity.class);
        startActivity(intent);
    }
    
    private void openHistory() {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }
    
    private void openThresholdSettings() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_threshold, null);
        
        Slider sliderCurrent = dialogView.findViewById(R.id.sliderLowCurrent);
        Slider sliderTemp = dialogView.findViewById(R.id.sliderHighTemp);
        Slider sliderStability = dialogView.findViewById(R.id.sliderStability);
        TextView tvCurrentValue = dialogView.findViewById(R.id.tvCurrentValue);
        TextView tvTempValue = dialogView.findViewById(R.id.tvTempValue);
        TextView tvStabilityValue = dialogView.findViewById(R.id.tvStabilityValue);
        
        float savedCurrent = preferences.getFloat("threshold_low_current", 500f);
        float savedTemp = preferences.getFloat("threshold_high_temp", 45f);
        float savedStability = preferences.getFloat("threshold_stability", 80f);
        
        sliderCurrent.setValue(savedCurrent);
        sliderTemp.setValue(savedTemp);
        sliderStability.setValue(savedStability);
        
        tvCurrentValue.setText(String.format("%.0f mA", savedCurrent));
        tvTempValue.setText(String.format("%.0f°C", savedTemp));
        tvStabilityValue.setText(String.format("%.0f%%", savedStability));
        
        sliderCurrent.addOnChangeListener((slider, value, fromUser) -> 
            tvCurrentValue.setText(String.format("%.0f mA", value)));
        sliderTemp.addOnChangeListener((slider, value, fromUser) -> 
            tvTempValue.setText(String.format("%.0f°C", value)));
        sliderStability.addOnChangeListener((slider, value, fromUser) -> 
            tvStabilityValue.setText(String.format("%.0f%%", value)));
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("Threshold Settings")
            .setView(dialogView)
            .setPositiveButton("Save", (dialog, which) -> {
                preferences.edit()
                    .putFloat("threshold_low_current", sliderCurrent.getValue())
                    .putFloat("threshold_high_temp", sliderTemp.getValue())
                    .putFloat("threshold_stability", sliderStability.getValue())
                    .apply();
                Toast.makeText(this, "Threshold saved", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            openSettings();
            return true;
        } else if (id == R.id.action_about) {
            new MaterialAlertDialogBuilder(this)
                .setTitle("About VoltCheck")
                .setMessage("VoltCheck v1.0\n\nAplikasi monitoring kesehatan baterai dan analisis charger/kabel.\n\nDeveloped with ❤️")
                .setPositiveButton("OK", null)
                .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
