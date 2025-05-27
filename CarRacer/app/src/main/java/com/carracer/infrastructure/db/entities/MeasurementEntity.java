package com.carracer.infrastructure.db.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "measurement")
public class MeasurementEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "type")
    public String type;

    @ColumnInfo(name = "duration_ms")
    public long durationMs;

    @ColumnInfo(name = "peak_speed_kmh")
    public float peakSpeedKmh;

    @ColumnInfo(name = "distance_meters")
    public double distanceMeters;

    @ColumnInfo(name = "timestamp")
    public long timestamp;
}
