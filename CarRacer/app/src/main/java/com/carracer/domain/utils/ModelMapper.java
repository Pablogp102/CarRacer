package com.carracer.domain.utils;

import com.carracer.domain.models.Measurement;
import com.carracer.domain.models.User;
import com.carracer.infrastructure.db.entities.MeasurementEntity;
import com.carracer.infrastructure.db.entities.UserEntity;

import java.util.UUID;

public class ModelMapper {
    public static Measurement toDomainModel(MeasurementEntity entity) {
        if (entity == null) return null;
        MeasurementType type = MeasurementType.valueOf(entity.type);

        // Konwersja String -> UUID
        UUID measurementId = UUID.fromString(entity.id);

        return new Measurement(measurementId, type, entity.durationS, entity.peakSpeedKmh, entity.distanceMeters, entity.timestamp);
    }

    public static MeasurementEntity fromDomainModel(Measurement measurement) {
        if (measurement == null) return null;
        MeasurementEntity entity = new MeasurementEntity();
        entity.id = measurement.getId().toString();
        entity.type = measurement.getType().name(); // enum → string
        entity.durationS = measurement.getDurationS();
        entity.peakSpeedKmh = measurement.getPeakSpeedKmh();
        entity.distanceMeters = measurement.getDistanceMeters();
        entity.timestamp = measurement.getTimestamp();
        return entity;
    }

    public static User toDomain(UserEntity entity) {
        if (entity == null) return null;
        return new User(UUID.fromString(entity.id), entity.login, entity.createdAt);
    }

    public static UserEntity fromDomain(User user) {
        if (user == null) return null;
        return new UserEntity(user.getId().toString(), user.getLogin(), user.getCreatedAt());
    }
}
