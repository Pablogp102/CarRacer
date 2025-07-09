package com.carracer.domain.utils;

import androidx.room.ProvidedTypeConverter;
import androidx.room.TypeConverter;

import com.carracer.domain.models.Measurement;
import com.carracer.infrastructure.db.entities.MeasurementEntity;

import javax.inject.Inject;

@ProvidedTypeConverter
public class Converters  {
    @Inject
    public Converters() {
    }
    @TypeConverter
    public static String measurementTypeToString(MeasurementType type) {
        return type == null ? null : type.name();
    }

    @TypeConverter
    public static MeasurementType fromString(String value) {
        return value == null ? null : MeasurementType.valueOf(value);
    }
}


