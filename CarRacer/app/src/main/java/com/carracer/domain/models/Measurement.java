package com.carracer.domain.models;

import com.carracer.domain.utils.MeasurementType;

public class Measurement {
    private MeasurementType type;
    private float durationS;
    private float peakSpeedKmh;
    private double distanceMeters;
    private long timestamp;

    public Measurement() {}

    public Measurement(MeasurementType type, float durationS, float peakSpeedKmh, double distanceMeters, long timestamp) {
        this.type = type;
        this.durationS = durationS;
        this.peakSpeedKmh = peakSpeedKmh;
        this.distanceMeters = distanceMeters;
        this.timestamp = timestamp;
    }
    public MeasurementType getType() {
        return type;
    }

    public float getDurationMs() {
        return durationS;
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
