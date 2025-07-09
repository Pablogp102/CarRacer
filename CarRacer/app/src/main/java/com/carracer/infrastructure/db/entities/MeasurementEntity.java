package com.carracer.infrastructure.db.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "measurements")
public class MeasurementEntity {
    @PrimaryKey
    @NonNull
    public String  id;

    @ColumnInfo(name = "type")
    public String type;

    @ColumnInfo(name = "duration_s")
    public float durationS;

    @ColumnInfo(name = "peak_speed_kmh")
    public float peakSpeedKmh;

    @ColumnInfo(name = "distance_meters")
    public double distanceMeters;

    @ColumnInfo(name = "timestamp")
    public long timestamp; // to jest MeasuredAt (np. System.currentTimeMillis())

    @ColumnInfo(name = "user_id")
    @NonNull
    public String userId; // UUID z API lub lokalny identyfikator

    @ColumnInfo(name = "is_synced")
    public boolean isSynced;
    public MeasurementEntity() {}

    public MeasurementEntity(String type, float durationS,
                             float peakSpeedKmh, double distanceMeters, long timestamp,
                             @NonNull String userId) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.durationS = durationS;
        this.peakSpeedKmh = peakSpeedKmh;
        this.distanceMeters = distanceMeters;
        this.timestamp = timestamp;
        this.userId = userId;
        this.isSynced = false;
    }
}
