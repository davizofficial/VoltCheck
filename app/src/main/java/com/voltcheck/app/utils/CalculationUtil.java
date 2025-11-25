package com.voltcheck.app.utils;

import java.util.List;

/**
 * Utility class untuk perhitungan statistik dan analisis
 */
public class CalculationUtil {
    
    /**
     * Menghitung rata-rata dari list nilai
     */
    public static float calculateAverage(List<Float> values) {
        if (values == null || values.isEmpty()) {
            return 0f;
        }
        
        float sum = 0f;
        for (Float value : values) {
            sum += value;
        }
        return sum / values.size();
    }
    
    /**
     * Mencari nilai maksimum dari list
     */
    public static float calculateMax(List<Float> values) {
        if (values == null || values.isEmpty()) {
            return 0f;
        }
        
        float max = values.get(0);
        for (Float value : values) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }
    
    /**
     * Mencari nilai minimum dari list
     */
    public static float calculateMin(List<Float> values) {
        if (values == null || values.isEmpty()) {
            return 0f;
        }
        
        float min = values.get(0);
        for (Float value : values) {
            if (value < min) {
                min = value;
            }
        }
        return min;
    }
    
    /**
     * Menghitung standard deviation
     */
    public static float calculateStandardDeviation(List<Float> values) {
        if (values == null || values.isEmpty()) {
            return 0f;
        }
        
        float mean = calculateAverage(values);
        float sumSquaredDiff = 0f;
        
        for (Float value : values) {
            float diff = value - mean;
            sumSquaredDiff += diff * diff;
        }
        
        return (float) Math.sqrt(sumSquaredDiff / values.size());
    }
    
    /**
     * Menghitung stabilitas arus dalam persentase
     * Stabilitas tinggi = variasi rendah
     */
    public static float calculateStability(List<Float> currentValues) {
        if (currentValues == null || currentValues.isEmpty()) {
            return 0f;
        }
        
        float avg = calculateAverage(currentValues);
        if (avg == 0) {
            return 0f;
        }
        
        float stdDev = calculateStandardDeviation(currentValues);
        float coefficientOfVariation = (stdDev / avg) * 100f;
        
        // Stabilitas = 100% - coefficient of variation
        // Semakin rendah variasi, semakin tinggi stabilitas
        float stability = 100f - coefficientOfVariation;
        
        // Clamp antara 0-100
        if (stability < 0) stability = 0;
        if (stability > 100) stability = 100;
        
        return stability;
    }
    
    /**
     * Menghitung voltage drop (penurunan tegangan)
     */
    public static float calculateVoltageDrop(List<Float> voltageValues) {
        if (voltageValues == null || voltageValues.isEmpty()) {
            return 0f;
        }
        
        float max = calculateMax(voltageValues);
        float min = calculateMin(voltageValues);
        
        return max - min;
    }
    
    /**
     * Generate kesimpulan kualitas charger/kabel
     */
    public static String generateChargerConclusion(float avgCurrent, float stability, float voltageDrop) {
        StringBuilder conclusion = new StringBuilder();
        
        // Analisis arus rata-rata
        if (avgCurrent >= 1500) {
            conclusion.append("✓ Arus pengisian SANGAT BAIK (").append(String.format("%.0f", avgCurrent)).append(" mA)\n");
        } else if (avgCurrent >= 1000) {
            conclusion.append("✓ Arus pengisian BAIK (").append(String.format("%.0f", avgCurrent)).append(" mA)\n");
        } else if (avgCurrent >= 500) {
            conclusion.append("⚠ Arus pengisian CUKUP (").append(String.format("%.0f", avgCurrent)).append(" mA)\n");
        } else {
            conclusion.append("✗ Arus pengisian RENDAH (").append(String.format("%.0f", avgCurrent)).append(" mA)\n");
        }
        
        // Analisis stabilitas
        if (stability >= 90) {
            conclusion.append("✓ Stabilitas SANGAT BAIK (").append(String.format("%.1f", stability)).append("%)\n");
        } else if (stability >= 80) {
            conclusion.append("✓ Stabilitas BAIK (").append(String.format("%.1f", stability)).append("%)\n");
        } else if (stability >= 70) {
            conclusion.append("⚠ Stabilitas CUKUP (").append(String.format("%.1f", stability)).append("%)\n");
        } else {
            conclusion.append("✗ Stabilitas BURUK (").append(String.format("%.1f", stability)).append("%)\n");
        }
        
        // Analisis voltage drop
        if (voltageDrop <= 0.1f) {
            conclusion.append("✓ Voltage drop MINIMAL (").append(String.format("%.3f", voltageDrop)).append(" V)\n");
        } else if (voltageDrop <= 0.3f) {
            conclusion.append("✓ Voltage drop NORMAL (").append(String.format("%.3f", voltageDrop)).append(" V)\n");
        } else if (voltageDrop <= 0.5f) {
            conclusion.append("⚠ Voltage drop TINGGI (").append(String.format("%.3f", voltageDrop)).append(" V)\n");
        } else {
            conclusion.append("✗ Voltage drop SANGAT TINGGI (").append(String.format("%.3f", voltageDrop)).append(" V)\n");
        }
        
        // Kesimpulan akhir
        conclusion.append("\n");
        if (avgCurrent >= 1000 && stability >= 80 && voltageDrop <= 0.3f) {
            conclusion.append("🎯 KESIMPULAN: Charger dan kabel dalam kondisi SANGAT BAIK");
        } else if (avgCurrent >= 500 && stability >= 70 && voltageDrop <= 0.5f) {
            conclusion.append("👍 KESIMPULAN: Charger dan kabel dalam kondisi BAIK");
        } else if (avgCurrent >= 500 && stability >= 60) {
            conclusion.append("⚠ KESIMPULAN: Charger atau kabel perlu perhatian");
        } else {
            conclusion.append("⚠ KESIMPULAN: Disarankan ganti charger atau kabel");
        }
        
        return conclusion.toString();
    }
}
