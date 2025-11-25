package com.voltcheck.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Utility class untuk logging data baterai
 */
public class BatteryDataLogger {
    
    private static final String PREFS_NAME = "BatteryDataLog";
    private static final String KEY_DATA_LOG = "data_log";
    private static final int MAX_LOG_ENTRIES = 1000; // Maksimal 1000 entries
    
    public static class BatteryData {
        public String timestamp;
        public float current;
        public float voltage;
        public float temperature;
        public int level;
        public String status;
        
        public BatteryData(float current, float voltage, float temperature, int level, String status) {
            this.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            this.current = current;
            this.voltage = voltage;
            this.temperature = temperature;
            this.level = level;
            this.status = status;
        }
    }
    
    /**
     * Log battery data
     */
    public static void logData(Context context, float current, float voltage, float temperature, int level, String status) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            List<BatteryData> dataList = getDataLog(context);
            
            // Add new data
            dataList.add(new BatteryData(current, voltage, temperature, level, status));
            
            // Keep only last MAX_LOG_ENTRIES
            if (dataList.size() > MAX_LOG_ENTRIES) {
                dataList = dataList.subList(dataList.size() - MAX_LOG_ENTRIES, dataList.size());
            }
            
            // Save to SharedPreferences
            Gson gson = new Gson();
            String json = gson.toJson(dataList);
            prefs.edit().putString(KEY_DATA_LOG, json).apply();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Get all logged data
     */
    public static List<BatteryData> getDataLog(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String json = prefs.getString(KEY_DATA_LOG, "[]");
            
            Gson gson = new Gson();
            Type type = new TypeToken<List<BatteryData>>(){}.getType();
            List<BatteryData> dataList = gson.fromJson(json, type);
            
            return dataList != null ? dataList : new ArrayList<>();
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Clear all logged data
     */
    public static void clearDataLog(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_DATA_LOG).apply();
    }
    
    /**
     * Get data count
     */
    public static int getDataCount(Context context) {
        return getDataLog(context).size();
    }
}
