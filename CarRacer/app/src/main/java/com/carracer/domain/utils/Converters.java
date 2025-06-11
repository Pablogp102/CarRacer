package com.carracer.domain.utils;

import androidx.room.TypeConverter;

import com.carracer.domain.models.Measurement;
import com.carracer.infrastructure.db.entities.MeasurementEntity;

public class Converters {

    @TypeConverter
    public static String measurementTypeToString(MeasurementType type) {
        return type == null ? null : type.name();
    }

    @TypeConverter
    public static MeasurementType fromString(String value) {
        return value == null ? null : MeasurementType.valueOf(value);
    }

    public Measurement toDomainModel(MeasurementEntity entity) {
        MeasurementType type = MeasurementType.valueOf(entity.type);
        return new Measurement(type, entity.durationS, entity.peakSpeedKmh, entity.distanceMeters, entity.timestamp);
    }

    public MeasurementEntity fromDomainModel(Measurement measurement) {
        MeasurementEntity entity = new MeasurementEntity();
        entity.type = measurement.getType().name(); // enum → string
        entity.durationS = measurement.getDurationMs();
        entity.peakSpeedKmh = measurement.getPeakSpeedKmh();
        entity.distanceMeters = measurement.getDistanceMeters();
        entity.timestamp = measurement.getTimestamp();
        return entity;
    }


}