package com.carracer.application.usecases.measurements;

import android.util.Log;

import com.carracer.application.gps.IGPSManager;
import com.carracer.domain.models.LocationData;
import com.carracer.domain.models.Measurement;
import com.carracer.domain.utils.MeasurementType;

import java.util.UUID;


public class ZeroToHundredMeasurementUseCase implements IMeasurementUseCase {
    private static final String TAG = "ZeroToHundredUseCase";

    private IGPSManager gpsManager;
    private LiveDataListener liveDataListener;
    private UseCaseResultListener resultListener;

    // Flaga informująca, czy pomiar już się zakończył
    private boolean isMeasuring = false;
    private boolean completed = false;

    private long startTimeMillis = 0;
    private long lastTimestamp = 0;
    private long elapsedTimeAccumulator = 0;

    private final float START_SPEED_THRESHOLD_KMH = 5f;
    private final float TARGET_SPEED_KMH = 100f;

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
    }

    private void processLocationData(LocationData data) {
        if (!isMeasuring || completed) return;

        long currentTimestamp = data.getTimestamp();
        float speed = data.getSpeedKmh();

        if (startTimeMillis == 0 && speed >= START_SPEED_THRESHOLD_KMH) {
            startTimeMillis = currentTimestamp;
            lastTimestamp = currentTimestamp;
            return;
        }
        if (startTimeMillis == 0) return;

        // aktualizacja czasu/dystansu
        if (currentTimestamp > lastTimestamp) {
            elapsedTimeAccumulator += (currentTimestamp - lastTimestamp);
        }
        lastTimestamp = currentTimestamp;
        liveDataListener.onSpeedUpdate(speed);
        liveDataListener.onTimeUpdate(elapsedTimeAccumulator);
        liveDataListener.onDistanceUpdate(data.getTotalDistanceMeters());

        if (speed >= TARGET_SPEED_KMH) {
            completeMeasurement(data, elapsedTimeAccumulator);
        }
    }

    private void completeMeasurement(LocationData finalData, long finalElapsedMillis) {
        Log.d(TAG, ">>> completeMeasurement() wywołane, finalElapsedMillis=" + finalElapsedMillis);
        if (!isMeasuring || completed) return;
        completed = true;

        float durationS = finalElapsedMillis / 1000f;
        double distance = Math.round(finalData.getTotalDistanceMeters() * 100.0) / 100.0;
        Measurement result = new Measurement(
                UUID.randomUUID(),
                getType(),
                durationS,
                TARGET_SPEED_KMH,
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
    }

    private void handleCancellation(String reason) {
        if (!isMeasuring || completed) {
            return; // ignoruj fałszywe onCancelled po zakończeniu
        }
        isMeasuring = false;
        if (resultListener != null) {
            resultListener.onMeasurementCancelled(reason);
        }
    }

    private void resetState() {
        isMeasuring = false;
        startTimeMillis = 0;
        lastTimestamp = 0;
        elapsedTimeAccumulator = 0;
        completed = false;
    }

    @Override public void stop() {
        if (!isMeasuring) return;
        isMeasuring = false;
        gpsManager.stop();
    }
    @Override public MeasurementType getType() { return MeasurementType.ZERO_TO_HUNDRED; }
    @Override public boolean isActive() { return isMeasuring; }
}