package com.carracer.infrastructure.network.models.Dtos;

import com.google.gson.annotations.SerializedName;

public class LeaderboardDto {
    @SerializedName("id")
    private String id;
    @SerializedName("userLogin")
    private String userLogin;
    @SerializedName("type")
    private String type;
    @SerializedName("durationS")
    private double durationS;
    @SerializedName("peakSpeedKmh")
    private double peakSpeedKmh;
    @SerializedName("distanceMeters")
    private double distanceMeters;
    @SerializedName("measuredAt")
    private long measuredAt;

    public String getId() { return id; }
    public String getUserLogin() { return userLogin; }
    public String getType() { return type; }
    public double getDurationS() { return durationS; }
    public double getPeakSpeedKmh() { return peakSpeedKmh; }
    public double getDistanceMeters() { return distanceMeters; }
    public long getMeasuredAt() { return measuredAt; }
}
