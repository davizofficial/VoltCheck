package com.voltcheck.app;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.annotation.Nullable;

import com.voltcheck.app.utils.NotificationUtil;
import com.voltcheck.app.SettingsActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Foreground Service untuk monitoring baterai di background
 */
public class BatteryService extends Service {
    
    private static final String TAG = "BatteryService";
    private static final long MONITORING_INTERVAL = 2000; // 2 detik
    private static final int STABILITY_SAMPLE_SIZE = 10; // Sample untuk cek stabilitas
    
    private Handler handler;
    private Runnable monitoringRunnable;
    private BatteryManager batteryManager;
    private SharedPreferences preferences;
    
    private List<Float> recentCurrentSamples = new ArrayList<>();
    private long lastLowCurrentAlert = 0;
    private long lastHighTempAlert = 0;
    private long lastUnstableAlert = 0;
    private static final long ALERT_COOLDOWN = 60000; // 1 menit cooldown antar alert
    private boolean hasAlarmPlayed = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        
        batteryManager = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);
        preferences = getSharedPreferences("VoltCheckPrefs", MODE_PRIVATE);
        handler = new Handler(Looper.getMainLooper());
        
        // Buat notification channels
        NotificationUtil.createNotificationChannels(this);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        
        // Start foreground dengan notifikasi
        startForeground(
                NotificationUtil.NOTIFICATION_ID_FOREGROUND,
                NotificationUtil.createForegroundNotification(this, "Monitoring aktif...")
        );
        
        // Mulai monitoring
        startMonitoring();
        
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
        stopMonitoring();
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // Unbound service
    }
    
    /**
     * Memulai monitoring periodik
     */
    private void startMonitoring() {
        long interval = SettingsActivity.getRefreshInterval(this);
        monitoringRunnable = new Runnable() {
            @Override
            public void run() {
                checkBatteryStatus();
                handler.postDelayed(this, interval);
            }
        };
        handler.post(monitoringRunnable);
    }
    
    /**
     * Menghentikan monitoring
     */
    private void stopMonitoring() {
        if (handler != null && monitoringRunnable != null) {
            handler.removeCallbacks(monitoringRunnable);
        }
    }
    
    /**
     * Memeriksa status baterai dan threshold
     */
    private void checkBatteryStatus() {
        try {
            // Baca data baterai
            float current = getCurrentNow();
            float voltage = getVoltageNow();
            float temperature = getTemperature();
            boolean isCharging = isCharging();
            int level = getBatteryLevel();
            
            // Format current dengan tanda yang benar
            // Positif saat charging, negatif saat discharging
            float displayCurrent;
            if (isCharging) {
                displayCurrent = Math.abs(current);
            } else {
                displayCurrent = -Math.abs(current);
            }
            
            // Update foreground notification
            String status = String.format("%.0f mA | %.2f V | %.1f°C", 
                    displayCurrent, voltage, temperature);
            updateForegroundNotification(status);
            
            // Cek threshold jika sedang charging
            if (isCharging) {
                checkThresholds(current, voltage, temperature, level);
            } else {
                // Reset alarm flag if not charging
                hasAlarmPlayed = false;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error checking battery status: " + e.getMessage());
        }
    }
    
    /**
     * Memeriksa threshold dan kirim alert jika perlu
     */
    private void checkThresholds(float current, float voltage, float temperature, int level) {
        long currentTime = System.currentTimeMillis();
        
        // Cek Alarm Baterai Penuh
        boolean alarmEnabled = preferences.getBoolean("alarm_full_charge", false);
        if (alarmEnabled) {
            int alarmLevel = preferences.getInt("alarm_level", 100);
            if (level >= alarmLevel && !hasAlarmPlayed) {
                playAlarmSound();
                NotificationUtil.createForegroundNotification(this, "Battery Alarm: " + level + "%");
                hasAlarmPlayed = true;
            }
        }
        
        // Cek arus rendah
        float thresholdLowCurrent = preferences.getFloat("threshold_low_current", 500f);
        if (current > 0 && current < thresholdLowCurrent) {
            boolean alertEnabled = preferences.getBoolean("alert_slow_charging", true);
            if (alertEnabled && currentTime - lastLowCurrentAlert > ALERT_COOLDOWN) {
                NotificationUtil.sendLowCurrentAlert(this, current);
                lastLowCurrentAlert = currentTime;
            }
        }
        
        // Cek suhu tinggi
        float thresholdHighTemp = preferences.getFloat("threshold_high_temp", 45f);
        if (temperature > thresholdHighTemp) {
            boolean alertEnabled = preferences.getBoolean("alert_temperature", true);
            if (alertEnabled && currentTime - lastHighTempAlert > ALERT_COOLDOWN) {
                NotificationUtil.sendHighTemperatureAlert(this, temperature);
                lastHighTempAlert = currentTime;
            }
        }
        
        // Cek stabilitas charging
        recentCurrentSamples.add(current);
        if (recentCurrentSamples.size() > STABILITY_SAMPLE_SIZE) {
            recentCurrentSamples.remove(0);
        }
        
        if (recentCurrentSamples.size() >= STABILITY_SAMPLE_SIZE) {
            if (isChargingUnstable()) {
                boolean alertEnabled = preferences.getBoolean("alert_fast_charging", true); // Using fast charging toggle for stability for now as per UI
                if (alertEnabled && currentTime - lastUnstableAlert > ALERT_COOLDOWN) {
                    NotificationUtil.sendUnstableChargingAlert(this);
                    lastUnstableAlert = currentTime;
                }
            }
        }
    }
    
    /**
     * Cek apakah charging tidak stabil
     */
    private boolean isChargingUnstable() {
        if (recentCurrentSamples.size() < STABILITY_SAMPLE_SIZE) {
            return false;
        }
        
        float avg = 0;
        for (Float sample : recentCurrentSamples) {
            avg += sample;
        }
        avg /= recentCurrentSamples.size();
        
        // Hitung standard deviation
        float variance = 0;
        for (Float sample : recentCurrentSamples) {
            variance += Math.pow(sample - avg, 2);
        }
        variance /= recentCurrentSamples.size();
        float stdDev = (float) Math.sqrt(variance);
        
        // Jika coefficient of variation > 20%, dianggap tidak stabil
        float cv = (stdDev / avg) * 100f;
        float thresholdStability = preferences.getFloat("threshold_stability", 80f);
        
        return cv > (100f - thresholdStability);
    }
    
    /**
     * Update foreground notification
     */
    private void updateForegroundNotification(String status) {
        try {
            startForeground(
                    NotificationUtil.NOTIFICATION_ID_FOREGROUND,
                    NotificationUtil.createForegroundNotification(this, status)
            );
        } catch (Exception e) {
            Log.e(TAG, "Error updating notification: " + e.getMessage());
        }
    }
    
    // ===== Battery Reading Methods =====
    
    /**
     * Membaca arus dengan deteksi otomatis unit
     */
    private float getCurrentNow() {
        try {
            int currentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
            
            Log.d(TAG, "Raw CURRENT_NOW: " + currentNow);
            
            if (currentNow != Integer.MIN_VALUE && currentNow != 0) {
                float currentMA;
                
                if (Math.abs(currentNow) > 10000) {
                    // Nilai besar = mikroampere (µA)
                    currentMA = currentNow / 1000f;
                } else {
                    // Nilai kecil = miliampere (mA)
                    currentMA = (float) currentNow;
                }
                
                return Math.abs(currentMA);
            }
            
            // Fallback: CURRENT_AVERAGE
            int currentAvg = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE);
            
            if (currentAvg != Integer.MIN_VALUE && currentAvg != 0) {
                float currentMA;
                
                if (Math.abs(currentAvg) > 10000) {
                    currentMA = currentAvg / 1000f;
                } else {
                    currentMA = (float) currentAvg;
                }
                
                return Math.abs(currentMA);
            }
            
            return 0f;
            
        } catch (Exception e) {
            Log.e(TAG, "Error reading current: " + e.getMessage());
            return 0f;
        }
    }
    
    private float getVoltageNow() {
        try {
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = registerReceiver(null, filter);
            if (batteryStatus != null) {
                int voltage = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
                return voltage / 1000f; // Convert to V
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading voltage: " + e.getMessage());
        }
        return 0f;
    }
    
    private float getTemperature() {
        try {
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = registerReceiver(null, filter);
            if (batteryStatus != null) {
                int temp = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
                return temp / 10f; // Convert to Celsius
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading temperature: " + e.getMessage());
        }
        return 0f;
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
        } catch (Exception e) {
            Log.e(TAG, "Error checking charging status: " + e.getMessage());
        }
        return false;
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
        } catch (Exception e) {
            Log.e(TAG, "Error reading battery level: " + e.getMessage());
        }
        return 0;
    }

    private void playAlarmSound() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (notification == null) {
                notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
