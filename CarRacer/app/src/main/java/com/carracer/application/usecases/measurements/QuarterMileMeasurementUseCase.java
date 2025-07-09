package com.carracer.application.usecases.measurements;

import android.util.Log;

import com.carracer.application.gps.IGPSManager;
import com.carracer.domain.models.LocationData;
import com.carracer.domain.models.Measurement;
import com.carracer.domain.utils.MeasurementType;

import java.util.UUID;

public class QuarterMileMeasurementUseCase implements IMeasurementUseCase {
    private static final String TAG = "QuarterMileUseCase";

    private IGPSManager gpsManager;
    private LiveDataListener liveDataListener;
    private UseCaseResultListener resultListener;

    private boolean isMeasuring = false;
    private boolean completed = false;

    private long startTimeMillis = 0;
    private long lastTimestamp = 0;
    private long elapsedTimeAccumulator = 0;
    private double startDistanceMeters = 0; // Dystans początkowy do pomiaru 1/4 mili

    private final float START_SPEED_THRESHOLD_KMH = 5f; // Możemy użyć małego progu, aby upewnić się, że samochód ruszył
    private final double TARGET_DISTANCE_METERS = 402.336; // 1/4 mili w metrach (dokładniej 402.336 metra)

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

        gpsManager.reset(); // Resetuje wewnętrzny dystans i stan GPSManagera
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
        double currentTotalDistance = data.getTotalDistanceMeters(); // Całkowity dystans od startu GPSManagera

        // Warunek rozpoczęcia pomiaru (ruch lub minimalna prędkość)
        if (startTimeMillis == 0 && speed >= START_SPEED_THRESHOLD_KMH) {
            startTimeMillis = currentTimestamp;
            lastTimestamp = currentTimestamp;
            startDistanceMeters = currentTotalDistance; // Zapisujemy dystans, od którego zaczynamy liczyć 1/4 mili
            Log.d(TAG, "Measurement started at " + START_SPEED_THRESHOLD_KMH + " km/h. Start distance: " + startDistanceMeters + "m");
            return; // Czekamy na kolejne dane
        }

        // Jeśli pomiar jeszcze się nie rozpoczął (np. auto stoi), ignorujemy dane
        if (startTimeMillis == 0) return;

        // Aktualizacja czasu i danych na żywo
        if (currentTimestamp > lastTimestamp) {
            elapsedTimeAccumulator += (currentTimestamp - lastTimestamp);
        }
        lastTimestamp = currentTimestamp;

        double distanceCovered = currentTotalDistance - startDistanceMeters; // Dystans pokonany od początku pomiaru

        liveDataListener.onSpeedUpdate(speed);
        liveDataListener.onTimeUpdate(elapsedTimeAccumulator);
        liveDataListener.onDistanceUpdate(distanceCovered); // Teraz live distance pokazuje dystans w tym pomiarze

        // Warunek zakończenia pomiaru (osiągnięcie 1/4 mili)
        if (distanceCovered >= TARGET_DISTANCE_METERS) {
            completeMeasurement(data, elapsedTimeAccumulator, distanceCovered);
        }
    }

    private void completeMeasurement(LocationData finalData, long finalElapsedMillis, double finalDistanceMeters) {
        Log.d(TAG, ">>> completeMeasurement() wywołane dla " + getType().getDisplayName() + ", finalElapsedMillis=" + finalElapsedMillis);
        if (!isMeasuring || completed) return;
        completed = true;

        float durationS = finalElapsedMillis / 1000f;
        // Zaokrąglamy dystans do dwóch miejsc po przecinku
        double distance = Math.round(finalDistanceMeters * 100.0) / 100.0;

        Measurement result = new Measurement(
                UUID.randomUUID(),
                getType(),
                durationS,
                finalData.getSpeedKmh(), // Prędkość na mecie
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
        startDistanceMeters = 0;
        completed = false;
        Log.d(TAG, "Measurement state reset.");
    }

    @Override
    public void stop() {
        if (!isMeasuring) return;
        isMeasuring = false;
        gpsManager.stop();
        Log.d(TAG, "Measurement manually stopped.");
    }

    @Override
    public MeasurementType getType() { return MeasurementType.QUARTER_MILE; } // Ustawienie odpowiedniego typu
    @Override
    public boolean isActive() { return isMeasuring; }
}