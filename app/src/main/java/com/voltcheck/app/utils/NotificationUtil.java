package com.voltcheck.app.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.voltcheck.app.MainActivity;
import com.voltcheck.app.R;

/**
 * Utility class untuk manajemen notifikasi
 */
public class NotificationUtil {
    
    private static final String CHANNEL_ID_FOREGROUND = "voltcheck_foreground";
    private static final String CHANNEL_ID_ALERTS = "voltcheck_alerts";
    private static final String CHANNEL_NAME_FOREGROUND = "Battery Monitoring";
    private static final String CHANNEL_NAME_ALERTS = "Battery Alerts";
    
    public static final int NOTIFICATION_ID_FOREGROUND = 1001;
    public static final int NOTIFICATION_ID_ALERT = 1002;
    
    /**
     * Membuat notification channels (diperlukan untuk Android O+)
     */
    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            
            // Channel untuk foreground service
            NotificationChannel foregroundChannel = new NotificationChannel(
                    CHANNEL_ID_FOREGROUND,
                    CHANNEL_NAME_FOREGROUND,
                    NotificationManager.IMPORTANCE_LOW
            );
            foregroundChannel.setDescription("Monitoring baterai aktif");
            foregroundChannel.setShowBadge(false);
            manager.createNotificationChannel(foregroundChannel);
            
            // Channel untuk alerts
            NotificationChannel alertChannel = new NotificationChannel(
                    CHANNEL_ID_ALERTS,
                    CHANNEL_NAME_ALERTS,
                    NotificationManager.IMPORTANCE_HIGH
            );
            alertChannel.setDescription("Peringatan kondisi baterai");
            alertChannel.enableVibration(true);
            manager.createNotificationChannel(alertChannel);
        }
    }
    
    /**
     * Membuat foreground notification untuk service
     */
    public static Notification createForegroundNotification(Context context, String status) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );
        
        return new NotificationCompat.Builder(context, CHANNEL_ID_FOREGROUND)
                .setContentTitle("VoltCheck Monitoring")
                .setContentText(status)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }
    
    /**
     * Mengirim notifikasi arus rendah
     */
    public static void sendLowCurrentAlert(Context context, float current) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
        
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        
        // Hapus notifikasi lama terlebih dahulu
        manager.cancel(NOTIFICATION_ID_ALERT);
        
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
                .setContentTitle("⚠ Arus Pengisian Rendah")
                .setContentText(String.format("Arus hanya %.0f mA. Periksa charger/kabel Anda.", current))
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
        
        manager.notify(NOTIFICATION_ID_ALERT, notification);
    }
    
    /**
     * Mengirim notifikasi suhu tinggi
     */
    public static void sendHighTemperatureAlert(Context context, float temp) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
        
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        
        // Hapus notifikasi lama terlebih dahulu
        manager.cancel(NOTIFICATION_ID_ALERT);
        
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
                .setContentTitle("🔥 Suhu Baterai Tinggi")
                .setContentText(String.format("Suhu mencapai %.1f°C. Cabut charger untuk mendinginkan.", temp))
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
        
        manager.notify(NOTIFICATION_ID_ALERT, notification);
    }
    
    /**
     * Mengirim notifikasi charging tidak stabil
     */
    public static void sendUnstableChargingAlert(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
        
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        
        // Hapus notifikasi lama terlebih dahulu
        manager.cancel(NOTIFICATION_ID_ALERT);
        
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
                .setContentTitle("⚠ Pengisian Tidak Stabil")
                .setContentText("Arus pengisian berfluktuasi. Periksa koneksi charger.")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
        
        manager.notify(NOTIFICATION_ID_ALERT, notification);
    }
    
    /**
     * Mengirim notifikasi charger terhubung
     */
    public static void sendChargerConnectedNotification(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
        
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        
        // Hapus notifikasi lama terlebih dahulu
        manager.cancel(NOTIFICATION_ID_ALERT);
        
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
                .setContentTitle("🔌 Charger Terhubung")
                .setContentText("Monitoring pengisian dimulai")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
        
        manager.notify(NOTIFICATION_ID_ALERT, notification);
    }
    
    /**
     * Mengirim notifikasi charger dicabut
     */
    public static void sendChargerDisconnectedNotification(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
        
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        
        // Hapus notifikasi lama terlebih dahulu
        manager.cancel(NOTIFICATION_ID_ALERT);
        
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
                .setContentTitle("🔌 Charger Dicabut")
                .setContentText("Monitoring pengisian dihentikan")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
        
        manager.notify(NOTIFICATION_ID_ALERT, notification);
    }
}
