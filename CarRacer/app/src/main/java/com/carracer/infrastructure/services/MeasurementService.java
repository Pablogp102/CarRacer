package com.carracer.infrastructure.services;

import android.util.Log;
import androidx.lifecycle.LiveData;
import com.carracer.application.gps.IGPSManager;
import com.carracer.application.services.IMeasurementService;
import com.carracer.application.usecases.measurements.IMeasurementUseCase;
import com.carracer.application.usecases.measurements.OneHundredToTwoHundredMeasurementUseCase;
import com.carracer.application.usecases.measurements.QuarterMileMeasurementUseCase;
import com.carracer.application.usecases.measurements.ZeroToHundredMeasurementUseCase;
import com.carracer.domain.models.Measurement;
import com.carracer.domain.models.User;
import com.carracer.domain.repositories.IMeasurementsRepository;
import com.carracer.domain.repositories.IUserRepository;
import com.carracer.domain.utils.Callback;
import com.carracer.domain.utils.MeasurementType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MeasurementService implements IMeasurementService {

    private static final String TAG = "MeasurementService";

    private final IGPSManager gpsManager;
    private final IMeasurementsRepository measurementsRepository;
    private final IUserRepository userRepository;
    private final Map<MeasurementType, IMeasurementUseCase> measurementUseCases;
    private IMeasurementUseCase activeMeasurementUseCase;
    private MeasurementResultListener currentResultListener;

    @Inject
    public MeasurementService(
            IGPSManager gpsManager,
            IMeasurementsRepository measurementsRepository,
            IUserRepository userRepository
    ) {
        this.gpsManager = gpsManager;
        this.measurementsRepository = measurementsRepository;
        this.userRepository = userRepository;
        this.measurementUseCases = new HashMap<>();
        measurementUseCases.put(MeasurementType.ZERO_TO_HUNDRED, new ZeroToHundredMeasurementUseCase());
        measurementUseCases.put(MeasurementType.ONE_HUNDRED_TO_TWO_HUNDRED, new OneHundredToTwoHundredMeasurementUseCase());
        measurementUseCases.put(MeasurementType.QUARTER_MILE, new QuarterMileMeasurementUseCase());
        measurementUseCases.put(MeasurementType.TOP_SPEED, new com.carracer.application.usecases.measureents.TopSpeedMeasurementUseCase());
    }

    @Override
    public void startMeasurement(MeasurementType type, LiveMeasurementDataListener liveListener, MeasurementResultListener resultListener) {
        if (isMeasurementActive()) {
            resultListener.onMeasurementError(new IllegalStateException("Pomiar już trwa."));
            return;
        }
        IMeasurementUseCase useCaseToStart = measurementUseCases.get(type);
        if (useCaseToStart == null) {
            resultListener.onMeasurementError(new IllegalArgumentException("Nie znaleziono logiki dla tego typu pomiaru."));
            return;
        }

        activeMeasurementUseCase = useCaseToStart;
        currentResultListener = resultListener;

        activeMeasurementUseCase.start(
                gpsManager,
                new IMeasurementUseCase.LiveDataListener() {
                    @Override
                    public void onSpeedUpdate(float speedKmh) {
                        if (liveListener != null) liveListener.onSpeedUpdate(speedKmh);
                    }
                    @Override
                    public void onDistanceUpdate(double totalDistanceMeters) {
                        if (liveListener != null) liveListener.onDistanceUpdate(totalDistanceMeters);
                    }
                    @Override
                    public void onTimeUpdate(long elapsedMillis) {
                        if (liveListener != null) liveListener.onTimeUpdate(elapsedMillis);
                    }
                },
                new IMeasurementUseCase.UseCaseResultListener() {
                    @Override
                    public void onMeasurementCompleted(Measurement measurement) {
                        saveAndHandleMeasurementResult(measurement);
                    }
                    @Override
                    public void onMeasurementError(Throwable t) {
                        if (currentResultListener != null) currentResultListener.onMeasurementError(t);
                        cleanupAfterMeasurement();
                    }
                    @Override
                    public void onMeasurementCancelled(String reason) {
                        if (currentResultListener != null) currentResultListener.onMeasurementCancelled(reason);
                        cleanupAfterMeasurement();
                    }
                }
        );
    }

    private void saveAndHandleMeasurementResult(Measurement measurement) {
        userRepository.getLoggedInUser(new Callback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null && user.getId() != null) {
                    String userId = user.getId().toString();
                    measurementsRepository.saveMeasurement(measurement, userId, new Callback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            if (currentResultListener != null) {
                                currentResultListener.onMeasurementCompleted(measurement);
                            }
                            measurementsRepository.syncMeasurementsToCloud(userId, new Callback<Void>() {
                                @Override public void onSuccess(Void r) { Log.d(TAG, "Synchronizacja w tle udana."); }
                                @Override public void onError(Throwable t) { Log.e(TAG, "Synchronizacja w tle nieudana.", t); }
                            });
                            cleanupAfterMeasurement();
                        }
                        @Override
                        public void onError(Throwable t) {
                            if (currentResultListener != null) currentResultListener.onMeasurementError(t);
                            cleanupAfterMeasurement();
                        }
                    });
                } else {
                    if (currentResultListener != null) currentResultListener.onMeasurementError(new Exception("Brak zalogowanego użytkownika."));
                    cleanupAfterMeasurement();
                }
            }
            @Override
            public void onError(Throwable t) {
                if (currentResultListener != null) currentResultListener.onMeasurementError(t);
                cleanupAfterMeasurement();
            }
        });
    }

    private void cleanupAfterMeasurement() {
        activeMeasurementUseCase = null;
        currentResultListener = null;
    }

    @Override
    public void stopCurrentMeasurement() {
        if (activeMeasurementUseCase != null) {
            activeMeasurementUseCase.stop();
        }
        cleanupAfterMeasurement();
    }

    @Override
    public boolean isMeasurementActive() {
        return activeMeasurementUseCase != null && activeMeasurementUseCase.isActive();
    }

    @Override
    public LiveData<List<Measurement>> getAllMeasurements(String userId) {
        return measurementsRepository.getAllMeasurements(userId);
    }

    @Override
    public void deleteAllMeasurements(String userId, Callback<Void> callback) {
        measurementsRepository.deleteAllMeasurements(userId, callback);
    }

    @Override
    public void getUnsyncedMeasurements(String userId, Callback<List<Measurement>> callback) {
        measurementsRepository.getUnsyncedMeasurements(userId, callback);
    }
}