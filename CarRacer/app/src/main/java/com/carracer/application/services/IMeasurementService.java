package com.carracer.application.services;

import androidx.lifecycle.LiveData;

import com.carracer.domain.models.Measurement;
import com.carracer.domain.utils.Callback;
import com.carracer.domain.utils.MeasurementType;

import java.util.List;

public interface IMeasurementService {

    interface LiveMeasurementDataListener {
        void onSpeedUpdate(float speedKmh);
        void onDistanceUpdate(double totalDistanceMeters);
        void onTimeUpdate(long elapsedMillis);
    }

    interface MeasurementResultListener {
        void onMeasurementCompleted(Measurement measurement);
        void onMeasurementCancelled(String reason);
        void onMeasurementError(Throwable t);
    }

    // Rozpoczyna konkretny typ pomiaru
    void startMeasurement(MeasurementType type, LiveMeasurementDataListener liveListener, MeasurementResultListener resultListener);

    // Zatrzymuje aktualnie aktywny pomiar (force stop). Nie zapisuje wyniku.
    void stopCurrentMeasurement();

    // Sprawdza, czy jakiś pomiar jest aktualnie aktywny
    boolean isMeasurementActive();

    // Synchronizuje lokalne pomiary z chmurą
    //void syncMeasurementsToCloud(String userId, Callback<Void> callback);

    // Pobiera wszystkie pomiary dla danego użytkownika (z repozytorium)
    LiveData<List<Measurement>> getAllMeasurements(String userId);

    // Usuwa wszystkie pomiary dla danego użytkownika
    void deleteAllMeasurements(String userId, Callback<Void> callback);

    // Pobiera niesynchronizowane pomiary (z repozytorium)
    // Zmieniono, aby przyjmowała userId dla bezpieczeństwa w multi-user
    void getUnsyncedMeasurements(String userId, Callback<List<Measurement>> callback);
}
