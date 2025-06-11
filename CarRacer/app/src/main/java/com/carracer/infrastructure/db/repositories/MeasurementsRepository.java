package com.carracer.infrastructure.db.repositories;

import androidx.room.Transaction;

import com.carracer.domain.repositories.IMeasurementsRepository;
import com.carracer.domain.repositories.IUserRepository;
import com.carracer.infrastructure.db.dao.MeasurementDao;
import com.carracer.infrastructure.db.entities.MeasurementEntity;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

public class MeasurementsRepository implements IMeasurementsRepository {
    private final MeasurementDao measurementDao;

    @Inject
    public MeasurementsRepository(MeasurementDao measurementDao) {
        this.measurementDao = measurementDao;
    }

    // Wstawia pojedynczy pomiar.
    @Override
    public long insertMeasurement(MeasurementEntity measurement) {
        return measurementDao.insertMeasurement(measurement);
    }

    // Wstawia wiele pomiarów.
    @Override
    public void insertAllMeasurements(List<MeasurementEntity> measurements) {
        if (measurements != null && !measurements.isEmpty()) {
            measurementDao.insertAll(measurements);
        }
    }

    // Pobiera niezsynchronizowane pomiary.
    @Override
    public List<MeasurementEntity> getUnsyncedMeasurements() {
        return measurementDao.getUnsyncedMeasurements();
    }

    // Oznacza listę pomiarów jako zsynchronizowane.
    @Override
    @Transaction // Zapewnia atomowość aktualizacji wielu pomiarów.
    public void markMeasurementsAsSynced(List<MeasurementEntity> syncedMeasurements) {
        if (syncedMeasurements != null && !syncedMeasurements.isEmpty()) {
            List<Long> measurementIds = syncedMeasurements.stream()
                    .map(measurement -> measurement.id)
                    .collect(Collectors.toList());
            if (!measurementIds.isEmpty()) {
                measurementDao.markMeasurementsAsSynced(measurementIds);
            }
        }
    }

    // Możesz dodać inne metody, np. pobieranie wszystkich pomiarów, pomiarów po ID itp.
    @Override
    public List<MeasurementEntity> getAllMeasurements() {
        return measurementDao.getAllMeasurements();
    }
    @Override
    public MeasurementEntity getMeasurementById(long id) {
        return measurementDao.getMeasurementById(id);
    }
}
