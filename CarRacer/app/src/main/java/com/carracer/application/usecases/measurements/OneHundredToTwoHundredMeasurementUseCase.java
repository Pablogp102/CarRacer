package com.carracer.application.usecases.measurements;

import android.util.Log;

import com.carracer.application.gps.IGPSManager;
import com.carracer.domain.models.LocationData;
import com.carracer.domain.models.Measurement;
import com.carracer.domain.utils.MeasurementType;

import java.util.UUID;

public class OneHundredToTwoHundredMeasurementUseCase implements IMeasurementUseCase {
    private static final String TAG = "OneToTwoHundredUseCase";

    private IGPSManager gpsManager;
    private LiveDataListener liveDataListener;
    private UseCaseResultListener resultListener;

    private boolean isMeasuring = false;
    private boolean completed = false;

    private long startTimeMillis = 0;
    private long lastTimestamp = 0;
    private long elapsedTimeAccumulator = 0;

    // Zmienione progi prędkości dla 100-200 km/h
    private final float START_SPEED_THRESHOLD_KMH = 100f; // Rozpoczynamy pomiar, gdy prędkość osiągnie 100 km/h
    private final float TARGET_SPEED_KMH = 200f; // Cel to 200 km/h

    @Override
    public void start(IGPSManager gpsManager,
                      LiveDataListener liveDataListener,
                      UseCaseResultListener resultListener) {
        if (isMeasuring) return;
        this.gpsManager = gpsManager;
        this.liveDataListener = liveDataListener;
        this.resultListener = resultListener;

        resetState();
        isMeasuring = true;
        completed = false;

        gpsManager.reset();
        gpsManager.start(new IGPSManager.GPSListener() {
            @Override
            public void onLocationData(LocationData data) {
                processLocationData(data);
            }
            @Override
            public void onCancelled(String reason) {
                handleCancellation(reason);
            }
        });
        Log.d(TAG, "Measurement started: " + getType().getDisplayName());
    }

    private void processLocationData(LocationData data) {
        if (!isMeasuring || completed) return;

        long currentTimestamp = data.getTimestamp();
        float speed = data.getSpeedKmh();

        // Warunek rozpoczęcia pomiaru (osiągnięcie 100 km/h)
        if (startTimeMillis == 0 && speed >= START_SPEED_THRESHOLD_KMH) {
            startTimeMillis = currentTimestamp;
            lastTimestamp = currentTimestamp;
            Log.d(TAG, "Measurement started at " + START_SPEED_THRESHOLD_KMH + " km/h. Timestamp: " + startTimeMillis);
            // Nie aktualizujemy live data od razu po starcie, czekamy na kolejne dane
            return;
        }

        // Jeśli pomiar jeszcze się nie rozpoczął, ignorujemy dane
        if (startTimeMillis == 0) return;

        // Aktualizacja czasu/dystansu
        if (currentTimestamp > lastTimestamp) {
            elapsedTimeAccumulator += (currentTimestamp - lastTimestamp);
        }
        lastTimestamp = currentTimestamp;

        // Aktualizacja danych na żywo
        liveDataListener.onSpeedUpdate(speed);
        liveDataListener.onTimeUpdate(elapsedTimeAccumulator);
        liveDataListener.onDistanceUpdate(data.getTotalDistanceMeters());

        // Warunek zakończenia pomiaru (osiągnięcie 200 km/h)
        if (speed >= TARGET_SPEED_KMH) {
            completeMeasurement(data, elapsedTimeAccumulator);
        }
    }

    private void completeMeasurement(LocationData finalData, long finalElapsedMillis) {
        Log.d(TAG, ">>> completeMeasurement() wywołane dla " + getType().getDisplayName() + ", finalElapsedMillis=" + finalElapsedMillis);
        if (!isMeasuring || completed) return;
        completed = true;

        float durationS = finalElapsedMillis / 1000f;
        double distance = Math.round(finalData.getTotalDistanceMeters() * 100.0) / 100.0;
        Measurement result = new Measurement(
                UUID.randomUUID(),
                getType(),
                durationS,
                TARGET_SPEED_KMH, // Tutaj może być target, lub finalna prędkość jeśli chcemy precyzyjnie
                distance,
                System.currentTimeMillis()
        );

        // Najpierw przekaż wynik
        if (resultListener != null) {
            resultListener.onMeasurementCompleted(result);
        }
        // Teraz zatrzymaj GPS bez generowania onCancelled
        gpsManager.stop();
        isMeasuring = false;
        Log.d(TAG, "Measurement completed for " + getType().getDisplayName());
    }

    private void handleCancellation(String reason) {
        if (!isMeasuring || completed) {
            return; // ignoruj fałszywe onCancelled po zakończeniu
        }
        isMeasuring = false;
        if (resultListener != null) {
            resultListener.onMeasurementCancelled(reason);
        }
        Log.d(TAG, "Measurement cancelled: " + reason);
    }

    private void resetState() {
        isMeasuring = false;
        startTimeMillis = 0;
        lastTimestamp = 0;
        elapsedTimeAccumulator = 0;
        completed = false;
        Log.d(TAG, "Measurement state reset.");
    }

    @Override public void stop() {
        if (!isMeasuring) return;
        isMeasuring = false;
        gpsManager.stop();
        Log.d(TAG, "Measurement manually stopped.");
    }

    @Override public MeasurementType getType() { return MeasurementType.ONE_HUNDRED_TO_TWO_HUNDRED; } // Zmieniony typ
    @Override public boolean isActive() { return isMeasuring; }
}