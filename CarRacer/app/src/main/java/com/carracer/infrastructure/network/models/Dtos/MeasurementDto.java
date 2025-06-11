package com.carracer.infrastructure.network.models.Dtos;

import java.util.Date;

public class MeasurementDto {
    private long id;
    private String type;
    private double durationS;
    private double peakSpeedKmh;
    private double distanceMeters;
    private Date measuredAt;

    public long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public double getDurationS() {
        return durationS;
    }

    public double getPeakSpeedKmh() {
        return peakSpeedKmh;
    }

    public double getDistanceMeters() {
        return distanceMeters;
    }

    public Date getMeasuredAt() {
        return measuredAt;
    }
}
