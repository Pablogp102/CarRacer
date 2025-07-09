package com.carracer.infrastructure.network.models.Dtos;

import com.google.gson.annotations.SerializedName;

public class MeasurementDto {
    // ID jest Stringiem, bo przesyłamy w nim UUID
    @SerializedName("id")
    private String id;

    @SerializedName("type")
    private String type;

    @SerializedName("durationS")
    private double durationS;

    @SerializedName("peakSpeedKmh")
    private double peakSpeedKmh;

    @SerializedName("distanceMeters")
    private double distanceMeters;

    // ZMIANA: Z 'Date' na 'long', żeby pasowało do timestampa z API
    @SerializedName("measuredAt")
    private long measuredAt;

    // --- GETTERY ---
    public String getId() { return id; }
    public String getType() { return type; }
    public double getDurationS() { return durationS; }
    public double getPeakSpeedKmh() { return peakSpeedKmh; }
    public double getDistanceMeters() { return distanceMeters; }
    public long getMeasuredAt() { return measuredAt; }

    public void setId(String id) { this.id = id; }
    public void setType(String type) { this.type = type; }
    public void setDurationS(double durationS) { this.durationS = durationS; }
    public void setPeakSpeedKmh(double peakSpeedKmh) { this.peakSpeedKmh = peakSpeedKmh; }
    public void setDistanceMeters(double distanceMeters) { this.distanceMeters = distanceMeters; }
    public void setMeasuredAt(long measuredAt) { this.measuredAt = measuredAt; }
}