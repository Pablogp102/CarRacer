package com.carracer.domain.models;
public class LocationData {
    private final double latitude;
    private final double longitude;
    private final float speedKmh;
    private final double totalDistanceMeters;
    private final double deltaDistanceMeters;
    private final long timestamp;
    public LocationData(double latitude,
                        double longitude,
                        float speedKmh,
                        double totalDistanceMeters,
                        double deltaDistanceMeters,
                        long timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.speedKmh = speedKmh;
        this.deltaDistanceMeters = deltaDistanceMeters;
        this.totalDistanceMeters = totalDistanceMeters;
        this.timestamp = timestamp;
    }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public float getSpeedKmh() { return speedKmh; }
    public double getTotalDistanceMeters() { return totalDistanceMeters; }
    public float getTimestamp() { return Math.round((timestamp / 100.0f)) / 10.0f; }
    public double getDeltaDistanceMeters() { return deltaDistanceMeters; }
}

