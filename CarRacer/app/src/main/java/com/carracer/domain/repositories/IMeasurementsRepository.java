package com.carracer.domain.repositories;

import androidx.lifecycle.LiveData;

import com.carracer.domain.models.Measurement;
import com.carracer.domain.utils.Callback;
import com.carracer.domain.utils.MeasurementType;
import com.carracer.infrastructure.db.entities.MeasurementEntity;

import java.util.List;

public interface IMeasurementsRepository {
    // Zapis pojedynczego pomiaru
    void saveMeasurement(Measurement measurement, String userId, Callback<Void> callback);

    // NOWA METODA: Zapis wielu pomiarów na raz
    void insertAll(List<MeasurementEntity> entities, Callback<Void> callback);

    // Pobierz wszystkie pomiary dla danego użytkownika
    LiveData<List<Measurement>> getAllMeasurements(String userId);

    // Pobierz pomiary danego typu dla danego użytkownika
    LiveData<List<Measurement>> getMeasurementsByType(String userId, MeasurementType type);

    void deleteMeasurement(String measurementId, Callback<Void> callback);
    // Usuń wszystkie pomiary dla danego użytkownika
    void deleteAllMeasurements(String userId, Callback<Void> callback);

    // Pobierz niesynchronizowane pomiary dla danego użytkownika (do wysłania na API)
    void getUnsyncedMeasurements(String userId, Callback<List<Measurement>> callback);

    // Oznacz pomiary jako zsynchronizowane (po udanym wysłaniu na API)
    void markMeasurementsAsSynced(List<String> measurementIds, Callback<Void> callback);

    // Metoda do synchronizacji lokalnych pomiarów z chmurą
    void syncMeasurementsToCloud(String userId, Callback<Void> callback);
}
