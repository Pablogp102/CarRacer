package com.carracer.infrastructure.db.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "measurements")
public class MeasurementEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

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
}
