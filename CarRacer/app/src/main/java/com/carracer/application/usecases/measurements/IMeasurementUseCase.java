package com.carracer.application.usecases.measurements;
import com.carracer.domain.models.LocationData;
import com.carracer.domain.models.Measurement;
import com.carracer.domain.utils.MeasurementType;
import com.carracer.application.gps.IGPSManager;

public interface IMeasurementUseCase {

    // Callbacki do komunikacji z IMeasurementService
    interface UseCaseResultListener {
        void onMeasurementCompleted(Measurement measurement); // Pomiar zakończony sukcesem
        void onMeasurementError(Throwable t); // Błąd w trakcie pomiaru
        void onMeasurementCancelled(String reason); // Pomiar anulowany
    }

    // Callbacki do aktualizacji UI na żywo
    interface LiveDataListener {
        void onSpeedUpdate(float speedKmh);
        void onDistanceUpdate(double totalDistanceMeters);
        void onTimeUpdate(long elapsedMillis);
    }

    // Metoda uruchamiająca Use Case.
    // Otrzymuje GPSManager do subskrypcji danych i Listenerów do komunikacji.
    void start(IGPSManager gpsManager, LiveDataListener liveDataListener, UseCaseResultListener resultListener);

    // Metoda do zatrzymania Use Case'a. Wyczyści jego wewnętrzny stan.
    void stop();

    // Zwraca typ pomiaru, którym zajmuje się ten Use Case
    MeasurementType getType();

    // Sprawdza, czy Use Case jest w trakcie pomiaru
    boolean isActive();
}
