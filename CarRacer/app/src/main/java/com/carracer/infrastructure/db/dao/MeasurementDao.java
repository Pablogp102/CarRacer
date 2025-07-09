package com.carracer.infrastructure.db.dao;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
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

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Użyj REPLACE również dla pojedynczego wstawienia
    void insertMeasurement(MeasurementEntity measurement); // Zwraca id wstawionego wiersza

    @Query("UPDATE measurements SET is_synced = 1 WHERE id IN (:measurementIds)")
    void markMeasurementsAsSynced(List<String> measurementIds);

    // Usuwa pomiary TYLKO dla konkretnego użytkownika
    @Query("DELETE FROM measurements WHERE user_id = :userId")
    void deleteMeasurementsByUserId(String userId);

    // Nowa metoda do usuwania pojedynczego pomiaru po jego ID
    @Query("DELETE FROM measurements WHERE id = :measurementId")
    void deleteMeasurementById(String measurementId);

    // Zmieniono: Pobierz wszystkie pomiary dla konkretnego użytkownika jako LiveData
    @Query("SELECT * FROM measurements WHERE user_id = :userId ORDER BY timestamp DESC")
    LiveData<List<MeasurementEntity>> getMeasurementsByUserId(String userId);

    @Query("SELECT * FROM measurements WHERE user_id = :userId AND (:typeFilter IS NULL OR type = :typeFilter) ORDER BY duration_s ASC")
    List<MeasurementEntity> getMeasurementsForUser(@NonNull String userId, @Nullable String typeFilter);


    // Nowa metoda: Pobierz pomiary dla konkretnego użytkownika i danego typu jako LiveData
    @Query("SELECT * FROM measurements WHERE user_id = :userId AND type = :type ORDER BY timestamp DESC")
    LiveData<List<MeasurementEntity>> getMeasurementsByUserIdAndType(String userId, String type);

    // Zmieniono: Pobierz niesynchronizowane pomiary TYLKO dla konkretnego użytkownika
    @Query("SELECT * FROM measurements WHERE is_synced = 0 AND user_id = :userId")
    List<MeasurementEntity> getUnsyncedMeasurements(String userId);
}
