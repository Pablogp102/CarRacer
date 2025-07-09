package com.carracer.domain.models;

import com.carracer.domain.utils.MeasurementType;

import java.io.Serializable;
import java.util.UUID;

public class Measurement implements Serializable {
    private UUID id;
    private MeasurementType type;
    private float durationS;
    private float peakSpeedKmh;
    private double distanceMeters;
    private long timestamp;

    public Measurement() {}

    public Measurement(UUID id, MeasurementType type, float durationS, float peakSpeedKmh, double distanceMeters, long timestamp) {
        this.id = id;
        this.type = type;
        this.durationS = durationS;
        this.peakSpeedKmh = peakSpeedKmh;
        this.distanceMeters = distanceMeters;
        this.timestamp = timestamp;
    }
    public MeasurementType getType() {
        return type;
    }

    public float getDurationS() {
        return durationS;
    }

    public UUID getId() {
        return id;
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
