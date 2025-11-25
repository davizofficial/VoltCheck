package com.voltcheck.app.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity class untuk menyimpan data sesi pengujian charger
 */
@Entity(tableName = "sessions")
public class SessionEntity {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String sessionName;
    private long timestamp;
    private float avgCurrent;
    private float maxCurrent;
    private float stability;
    private float voltageDrop;
    private String conclusion;
    private String chartDataJson; // JSON string untuk data grafik
    
    // Constructor
    public SessionEntity() {
    }
    
    // Getters
    public long getId() {
        return id;
    }
    
    public String getSessionName() {
        return sessionName;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public float getAvgCurrent() {
        return avgCurrent;
    }
    
    public float getMaxCurrent() {
        return maxCurrent;
    }
    
    public float getStability() {
        return stability;
    }
    
    public float getVoltageDrop() {
        return voltageDrop;
    }
    
    public String getConclusion() {
        return conclusion;
    }
    
    public String getChartDataJson() {
        return chartDataJson;
    }
    
    // Setters
    public void setId(long id) {
        this.id = id;
    }
    
    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public void setAvgCurrent(float avgCurrent) {
        this.avgCurrent = avgCurrent;
    }
    
    public void setMaxCurrent(float maxCurrent) {
        this.maxCurrent = maxCurrent;
    }
    
    public void setStability(float stability) {
        this.stability = stability;
    }
    
    public void setVoltageDrop(float voltageDrop) {
        this.voltageDrop = voltageDrop;
    }
    
    public void setConclusion(String conclusion) {
        this.conclusion = conclusion;
    }
    
    public void setChartDataJson(String chartDataJson) {
        this.chartDataJson = chartDataJson;
    }
}
