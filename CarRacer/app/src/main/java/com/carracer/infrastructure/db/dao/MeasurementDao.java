package com.carracer.infrastructure.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;

import com.carracer.infrastructure.db.entities.MeasurementEntity;

import java.util.List;

@Dao
public interface MeasurementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MeasurementEntity> measurements);
    @Insert
    long insertMeasurement(MeasurementEntity measurement);

    @Query("UPDATE measurements SET is_synced = 1 WHERE id IN (:measurementIds)")
    void markMeasurementsAsSynced(List<Long> measurementIds);
    @Query("DELETE FROM measurements")
    void deleteMeasurementsByUserId();

    @Query("SELECT * FROM measurements")
    List<MeasurementEntity> getAllMeasurements();

    @Query("SELECT * FROM measurements WHERE is_synced = 0")
    List<MeasurementEntity> getUnsyncedMeasurements();
    @Query("SELECT * FROM measurements WHERE id = :id LIMIT 1")
    MeasurementEntity getMeasurementById(long id);
}
