package com.carracer.infrastructure.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;

import com.carracer.infrastructure.db.entities.MeasurementEntity;

import java.util.List;

@Dao
public interface MeasurementDao {

    @Insert
    long insertMeasurement(MeasurementEntity measurement);

    @Update
    void updateMeasurement(MeasurementEntity measurement);

    @Delete
    void deleteMeasurement(MeasurementEntity measurement);

    @Query("SELECT * FROM measurement")
    List<MeasurementEntity> getAllMeasurements();

    @Query("SELECT * FROM measurement WHERE id = :id LIMIT 1")
    MeasurementEntity getMeasurementById(long id);
}
