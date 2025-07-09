package com.carracer.domain.models;

import java.util.Objects;

// Model UI dla jednego wiersza na liście leaderboardu
public class LeaderboardItem {
    private final String id;
    private final String userLogin;
    private final String type;
    private final double durationS;
    private final double peakSpeedKmh;
    private final double distanceMeters;
    private final long measuredAt;
    private final boolean isDeletable;

    public LeaderboardItem(String id, String userLogin, String type, double durationS, double peakSpeedKmh, double distanceMeters, long measuredAt, boolean isDeletable) {
        this.id = id;
        this.userLogin = userLogin;
        this.type = type;
        this.durationS = durationS;
        this.peakSpeedKmh = peakSpeedKmh;
        this.distanceMeters = distanceMeters;
        this.measuredAt = measuredAt;
        this.isDeletable = isDeletable;
    }

    // Gettery...
    public String getId() { return id; }
    public String getUserLogin() { return userLogin; }
    public String getType() { return type; }
    public double getDurationS() { return durationS; }
    public double getPeakSpeedKmh() { return peakSpeedKmh; }
    public double getDistanceMeters() { return distanceMeters; }
    public long getMeasuredAt() { return measuredAt; }
    public boolean isDeletable() { return isDeletable; }

    // Potrzebne dla DiffUtil w adapterze
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LeaderboardItem that = (LeaderboardItem) o;
        return id.equals(that.id);
    }
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}