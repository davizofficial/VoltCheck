package com.voltcheck.app;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.voltcheck.app.database.SessionDatabase;
import com.voltcheck.app.models.SessionEntity;
import com.voltcheck.app.utils.CalculationUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * TestModeActivity - Mode pengujian charger/kabel
 */
public class TestModeActivity extends AppCompatActivity {
    
    private static final String TAG = "TestModeActivity";
    private static final int TEST_DURATION_SECONDS = 60; // 60 detik
    
    // UI Components
    private TextView tvTimer, tvCurrentSample, tvVoltageSample, tvTempSample;
    private TextView tvAvgCurrent, tvMaxCurrent, tvStability, tvVoltageDrop;
    private TextView tvConclusion;
    private ProgressBar progressBar;
    private MaterialButton btnStartTest, btnStopTest, btnSaveSession;
    
    // Battery Manager
    private BatteryManager batteryManager;
    
    // Test data
    private List<Float> currentSamples = new ArrayList<>();
    private List<Float> voltageSamples = new ArrayList<>();
    private List<Float> temperatureSamples = new ArrayList<>();
    private CountDownTimer countDownTimer;
    private boolean isTestRunning = false;
    
    // Results
    private float avgCurrent = 0f;
    private float maxCurrent = 0f;
    private float stability = 0f;
    private float voltageDrop = 0f;
    private String conclusion = "";
    
    private Executor executor = Executors.newSingleThreadExecutor();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_mode);
        
        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Test Mode");
        }
        
        batteryManager = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);
        
        initializeViews();
        setupClickListeners();
    }
    
    private void initializeViews() {
        tvTimer = findViewById(R.id.tvTimer);
        tvCurrentSample = findViewById(R.id.tvCurrentSample);
        tvVoltageSample = findViewById(R.id.tvVoltageSample);
        tvTempSample = findViewById(R.id.tvTempSample);
        
        tvAvgCurrent = findViewById(R.id.tvAvgCurrent);
        tvMaxCurrent = findViewById(R.id.tvMaxCurrent);
        tvStability = findViewById(R.id.tvStability);
        tvVoltageDrop = findViewById(R.id.tvVoltageDrop);
        tvConclusion = findViewById(R.id.tvConclusion);
        
        progressBar = findViewById(R.id.progressBar);
        btnStartTest = findViewById(R.id.btnStartTest);
        btnStopTest = findViewById(R.id.btnStopTest);
        btnSaveSession = findViewById(R.id.btnSaveSession);
        
        // Initial state
        btnStopTest.setEnabled(false);
        btnSaveSession.setEnabled(false);
        progressBar.setMax(TEST_DURATION_SECONDS);
        progressBar.setProgress(0);
    }
    
    private void setupClickListeners() {
        btnStartTest.setOnClickListener(v -> startTest());
        btnStopTest.setOnClickListener(v -> stopTest());
        btnSaveSession.setOnClickListener(v -> saveSession());
    }
    
    /**
     * Memulai test
     */
    private void startTest() {
        // Reset data
        currentSamples.clear();
        voltageSamples.clear();
        temperatureSamples.clear();
        
        tvConclusion.setText("");
        tvAvgCurrent.setText("--");
        tvMaxCurrent.setText("--");
        tvStability.setText("--");
        tvVoltageDrop.setText("--");
        
        isTestRunning = true;
        btnStartTest.setEnabled(false);
        btnStopTest.setEnabled(true);
        btnSaveSession.setEnabled(false);
        
        // Start countdown timer
        countDownTimer = new CountDownTimer(TEST_DURATION_SECONDS * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", 
                    secondsRemaining / 60, secondsRemaining % 60));
                progressBar.setProgress(TEST_DURATION_SECONDS - secondsRemaining);
                
                // Collect sample
                collectSample();
            }
            
            @Override
            public void onFinish() {
                tvTimer.setText("00:00");
                progressBar.setProgress(TEST_DURATION_SECONDS);
                isTestRunning = false;
                btnStartTest.setEnabled(true);
                btnStopTest.setEnabled(false);
                
                // Analyze results
                analyzeResults();
            }
        };
        
        countDownTimer.start();
        Toast.makeText(this, "Test started - Keep charger connected", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Menghentikan test
     */
    private void stopTest() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        
        isTestRunning = false;
        btnStartTest.setEnabled(true);
        btnStopTest.setEnabled(false);
        
        if (currentSamples.size() >= 10) {
            analyzeResults();
        } else {
            Toast.makeText(this, "Test stopped - Not enough data", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Mengumpulkan sample data
     */
    private void collectSample() {
        try {
            float current = getCurrentNow();
            float voltage = getVoltageNow();
            float temperature = getTemperature();
            
            currentSamples.add(current);
            voltageSamples.add(voltage);
            temperatureSamples.add(temperature);
            
            // Update UI
            tvCurrentSample.setText(String.format("%.0f mA", current));
            tvVoltageSample.setText(String.format("%.2f V", voltage));
            tvTempSample.setText(String.format("%.1f°C", temperature));
            
        } catch (Exception e) {
            Log.e(TAG, "Error collecting sample: " + e.getMessage());
        }
    }
    
    /**
     * Menganalisis hasil test
     */
    private void analyzeResults() {
        if (currentSamples.isEmpty()) {
            Toast.makeText(this, "No data collected", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Calculate statistics
        avgCurrent = CalculationUtil.calculateAverage(currentSamples);
        maxCurrent = CalculationUtil.calculateMax(currentSamples);
        stability = CalculationUtil.calculateStability(currentSamples);
        voltageDrop = CalculationUtil.calculateVoltageDrop(voltageSamples);
        conclusion = CalculationUtil.generateChargerConclusion(avgCurrent, stability, voltageDrop);
        
        // Update UI
        tvAvgCurrent.setText(String.format("%.0f mA", avgCurrent));
        tvMaxCurrent.setText(String.format("%.0f mA", maxCurrent));
        tvStability.setText(String.format("%.1f%%", stability));
        tvVoltageDrop.setText(String.format("%.3f V", voltageDrop));
        tvConclusion.setText(conclusion);
        
        btnSaveSession.setEnabled(true);
        
        Toast.makeText(this, "Analysis complete!", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Menyimpan session ke database
     */
    private void saveSession() {
        executor.execute(() -> {
            try {
                SessionEntity session = new SessionEntity();
                
                // Generate session name
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String sessionName = "Test " + sdf.format(new Date());
                
                session.setSessionName(sessionName);
                session.setTimestamp(System.currentTimeMillis());
                session.setAvgCurrent(avgCurrent);
                session.setMaxCurrent(maxCurrent);
                session.setStability(stability);
                session.setVoltageDrop(voltageDrop);
                session.setConclusion(conclusion);
                
                // Convert samples to JSON
                Gson gson = new Gson();
                ChartData chartData = new ChartData();
                chartData.currentSamples = currentSamples;
                chartData.voltageSamples = voltageSamples;
                chartData.temperatureSamples = temperatureSamples;
                session.setChartDataJson(gson.toJson(chartData));
                
                // Save to database
                SessionDatabase db = SessionDatabase.getInstance(this);
                long id = db.sessionDao().insert(session);
                
                runOnUiThread(() -> {
                    if (id > 0) {
                        Toast.makeText(this, "Session saved successfully", Toast.LENGTH_SHORT).show();
                        btnSaveSession.setEnabled(false);
                    } else {
                        Toast.makeText(this, "Failed to save session", Toast.LENGTH_SHORT).show();
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error saving session: " + e.getMessage());
                runOnUiThread(() -> 
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
    
    // ===== Battery Reading Methods =====
    
    private float getCurrentNow() {
        try {
            int current = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
            return Math.abs(current / 1000f);
        } catch (Exception e) {
            return 0f;
        }
    }
    
    private float getVoltageNow() {
        try {
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = registerReceiver(null, filter);
            if (batteryStatus != null) {
                int voltage = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
                return voltage / 1000f;
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
                return temp / 10f;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading temperature: " + e.getMessage());
        }
        return 0f;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
    
    /**
     * Helper class untuk JSON serialization
     */
    private static class ChartData {
        List<Float> currentSamples;
        List<Float> voltageSamples;
        List<Float> temperatureSamples;
    }
}
