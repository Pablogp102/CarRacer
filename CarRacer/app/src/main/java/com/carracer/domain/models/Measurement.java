package com.carracer.domain.models;

import com.carracer.domain.utils.MeasurementType;

public class Measurement {
    private MeasurementType type;
    private long durationMs;
    private float peakSpeedKmh;
    private double distanceMeters;
    private long timestamp;

    public Measurement() {}

    public Measurement(MeasurementType type, long durationMs, float peakSpeedKmh, double distanceMeters, long timestamp) {
        this.type = type;
        this.durationMs = durationMs;
        this.peakSpeedKmh = peakSpeedKmh;
        this.distanceMeters = distanceMeters;
        this.timestamp = timestamp;
    }
    public MeasurementType getType() {
        return type;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public float getPeakSpeedKmh() {
        return peakSpeedKmh;
    }

    public double getDistanceMeters() {
        return distanceMeters;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
