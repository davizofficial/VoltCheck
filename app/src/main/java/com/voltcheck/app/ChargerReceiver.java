package com.voltcheck.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.voltcheck.app.utils.NotificationUtil;

/**
 * BroadcastReceiver untuk mendeteksi event charger connected/disconnected
 */
public class ChargerReceiver extends BroadcastReceiver {
    
    private static final String TAG = "ChargerReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        
        String action = intent.getAction();
        Log.d(TAG, "Received action: " + action);
        
        if (Intent.ACTION_POWER_CONNECTED.equals(action)) {
            handlePowerConnected(context);
        } else if (Intent.ACTION_POWER_DISCONNECTED.equals(action)) {
            handlePowerDisconnected(context);
        }
    }
    
    /**
     * Handle ketika charger dicolokkan
     */
    private void handlePowerConnected(Context context) {
        Log.d(TAG, "Power connected");
        
        // Kirim notifikasi
        NotificationUtil.sendChargerConnectedNotification(context);
        
        // Start battery service
        startBatteryService(context);
    }
    
    /**
     * Handle ketika charger dicabut
     */
    private void handlePowerDisconnected(Context context) {
        Log.d(TAG, "Power disconnected");
        
        // Kirim notifikasi
        NotificationUtil.sendChargerDisconnectedNotification(context);
    }
    
    /**
     * Memulai BatteryService
     */
    private void startBatteryService(Context context) {
        Intent serviceIntent = new Intent(context, BatteryService.class);
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
            Log.d(TAG, "BatteryService started");
        } catch (Exception e) {
            Log.e(TAG, "Failed to start BatteryService: " + e.getMessage());
        }
    }
}
