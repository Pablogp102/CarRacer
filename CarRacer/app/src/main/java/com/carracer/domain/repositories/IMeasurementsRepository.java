package com.carracer.domain.repositories;

import com.carracer.infrastructure.db.entities.MeasurementEntity;

import java.util.List;

public interface IMeasurementsRepository {
    long insertMeasurement(MeasurementEntity measurement);
    void insertAllMeasurements(List<MeasurementEntity> measurements);
    List<MeasurementEntity> getUnsyncedMeasurements();
    void markMeasurementsAsSynced(List<MeasurementEntity> syncedMeasurements);
    List<MeasurementEntity> getAllMeasurements();
    MeasurementEntity getMeasurementById(long id);
}
