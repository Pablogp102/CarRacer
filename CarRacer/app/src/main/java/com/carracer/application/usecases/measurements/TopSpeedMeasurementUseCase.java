package com.carracer.application.usecases.measureents;

import android.os.CountDownTimer; // Będziemy potrzebować Timera
import android.util.Log;

import com.carracer.application.gps.IGPSManager;
import com.carracer.application.usecases.measurements.IMeasurementUseCase;
import com.carracer.domain.models.LocationData;
import com.carracer.domain.models.Measurement;
import com.carracer.domain.utils.MeasurementType;

import java.util.UUID;

public class TopSpeedMeasurementUseCase implements IMeasurementUseCase {
    private static final String TAG = "TopSpeedUseCase";

    private IGPSManager gpsManager;
    private LiveDataListener liveDataListener;
    private UseCaseResultListener resultListener;

    private boolean isMeasuring = false;
    private boolean completed = false;

    private long startTimeMillis = 0;
    private long lastTimestamp = 0;
    private long elapsedTimeAccumulator = 0; // Czas trwania pomiaru

    private float maxSpeedKmh = 0f; // Najwyższa osiągnięta prędkość
    private double totalDistanceMeters = 0; // Całkowity dystans pokonany w czasie pomiaru

    private CountDownTimer measurementTimer; // Timer do limitu 1 minuty

    private final long MEASUREMENT_DURATION_MILLIS = 60 * 1000; // 1 minuta w milisekundach
    private final float START_SPEED_THRESHOLD_KMH = 5f; // Rozpocznij pomiar po przekroczeniu tej prędkości

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

        gpsManager.reset(); // Resetuje wewnętrzny stan GPSManagera (dystans itp.)
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

        // Uruchom timer na 1 minutę
        measurementTimer = new CountDownTimer(MEASUREMENT_DURATION_MILLIS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Nie robimy nic w onTick, bo czas jest aktualizowany w processLocationData
            }

            @Override
            public void onFinish() {
                // Czas się skończył, zakończ pomiar
                Log.d(TAG, "Measurement timer finished. Completing measurement.");
                completeMeasurement();
            }
        }.start();
    }

    private void processLocationData(LocationData data) {
        if (!isMeasuring || completed) return;

        long currentTimestamp = data.getTimestamp();
        float speed = data.getSpeedKmh();

        // Warunek rozpoczęcia pomiaru (ruch)
        if (startTimeMillis == 0 && speed >= START_SPEED_THRESHOLD_KMH) {
            startTimeMillis = currentTimestamp;
            lastTimestamp = currentTimestamp;
            Log.d(TAG, "Measurement officially started (speed > " + START_SPEED_THRESHOLD_KMH + " km/h).");
        }

        // Jeśli pomiar jeszcze się nie rozpoczął, lub timer nie jest aktywny, ignorujemy aktualizacje danych
        if (startTimeMillis == 0) return;

        // Aktualizacja czasu i dystansu od początku aktywnego pomiaru
        if (currentTimestamp > lastTimestamp) {
            long delta = (currentTimestamp - lastTimestamp);
            elapsedTimeAccumulator += delta;
            totalDistanceMeters += data.getTotalDistanceMeters(); // Zakładam, że LocationData.getDistanceTraveled() daje dystans od ostatniej aktualizacji
            // Jeśli nie, trzeba to liczyć ręcznie z lokalizacji
        }
        lastTimestamp = currentTimestamp;


        // Aktualizacja maksymalnej prędkości
        if (speed > maxSpeedKmh) {
            maxSpeedKmh = speed;
        }

        // Aktualizacja danych na żywo
        liveDataListener.onSpeedUpdate(speed); // Aktualna prędkość
        liveDataListener.onTimeUpdate(elapsedTimeAccumulator); // Czas trwania pomiaru
        liveDataListener.onDistanceUpdate(totalDistanceMeters); // Dystans w tym pomiarze

        // WAŻNE: Pomiar kończy się TYLKO po upływie 1 minuty, co obsłuży measurementTimer
        // Brak tutaj warunku zakończenia bazującego na prędkości, bo to max prędkość w czasie
    }

    private void completeMeasurement() {
        Log.d(TAG, ">>> completeMeasurement() wywołane dla " + getType().getDisplayName());
        if (!isMeasuring || completed) return; // Upewnij się, że nie zakończysz dwa razy
        completed = true;

        if (measurementTimer != null) {
            measurementTimer.cancel(); // Anuluj timer, jeśli pomiar zakończył się przedwcześnie (np. ręczne stop)
        }

        float durationS = elapsedTimeAccumulator / 1000f;
        // Zaokrąglamy dystans i prędkość do dwóch miejsc po przecinku
        double finalDistance = Math.round(totalDistanceMeters * 100.0) / 100.0;
        float finalMaxSpeed = Math.round(maxSpeedKmh * 10.0f) / 10.0f; // Zaokrąglamy prędkość do jednego miejsca

        Measurement result = new Measurement(
                UUID.randomUUID(),
                getType(),
                durationS,
                finalMaxSpeed, // Osiągnięta maksymalna prędkość
                finalDistance,
                System.currentTimeMillis()
        );

        // Przekaż wynik
        if (resultListener != null) {
            resultListener.onMeasurementCompleted(result);
        }
        // Zatrzymaj GPS
        gpsManager.stop();
        isMeasuring = false;
        Log.d(TAG, "Measurement completed for " + getType().getDisplayName());
    }

    private void handleCancellation(String reason) {
        if (!isMeasuring || completed) {
            return;
        }
        isMeasuring = false;
        if (measurementTimer != null) {
            measurementTimer.cancel();
        }
        if (resultListener != null) {
            resultListener.onMeasurementCancelled(reason);
        }
        Log.d(TAG, "Measurement cancelled: " + reason);
        gpsManager.stop(); // Upewnij się, że GPS jest zatrzymany
    }

    private void resetState() {
        isMeasuring = false;
        startTimeMillis = 0;
        lastTimestamp = 0;
        elapsedTimeAccumulator = 0;
        maxSpeedKmh = 0f;
        totalDistanceMeters = 0;
        completed = false;
        if (measurementTimer != null) {
            measurementTimer.cancel();
            measurementTimer = null;
        }
        Log.d(TAG, "Measurement state reset.");
    }

    @Override
    public void stop() {
        if (!isMeasuring) return;
        isMeasuring = false;
        if (measurementTimer != null) {
            measurementTimer.cancel();
        }
        gpsManager.stop();
        Log.d(TAG, "Measurement manually stopped.");
        // Ręczne zatrzymanie zawsze oznacza anulowanie pomiaru
        handleCancellation("Pomiar zatrzymany przez użytkownika.");
    }

    @Override
    public MeasurementType getType() { return MeasurementType.TOP_SPEED; }
    @Override
    public boolean isActive() { return isMeasuring; }
}