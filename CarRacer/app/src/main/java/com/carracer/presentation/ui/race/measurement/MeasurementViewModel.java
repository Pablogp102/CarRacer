package com.carracer.presentation.ui.race.measurement;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.carracer.application.services.IMeasurementService;
import com.carracer.domain.models.Measurement;
import com.carracer.domain.utils.MeasurementType;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MeasurementViewModel extends ViewModel {

    // Definiujemy wszystkie możliwe stany, w jakich może być nasz ekran
    public enum UiState {
        IDLE,       // Czeka na akcję, gotowy do startu lub po zatrzymaniu
        COUNTDOWN,  // Trwa odliczanie 3-2-1
        MEASURING,   // Trwa właściwy pomiar
        SUMMARY      // Podsumowanie
    }

    private final IMeasurementService measurementService;

    private final MutableLiveData<UiState> _uiState = new MutableLiveData<>();
    public LiveData<UiState> getUiState() { return _uiState; }

    private final MutableLiveData<String> _countdownText = new MutableLiveData<>();
    public LiveData<String> getCountdownText() { return _countdownText; }

    private final MutableLiveData<Float> _liveSpeedKmh = new MutableLiveData<>();
    public LiveData<Float> getLiveSpeedKmh() { return _liveSpeedKmh; }

    private final MutableLiveData<Double> _liveDistanceMeters = new MutableLiveData<>();
    public LiveData<Double> getLiveDistanceMeters() { return _liveDistanceMeters; }

    private final MutableLiveData<Long> _liveTimeMillis = new MutableLiveData<>();
    public LiveData<Long> getLiveTimeMillis() { return _liveTimeMillis; }

    // LiveData do przekazania wyniku do dialogu
    private final MutableLiveData<Measurement> _measurementResult = new MutableLiveData<>();
    public LiveData<Measurement> getMeasurementResult() { return _measurementResult; }

    // LiveData do pokazywania błędów
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> getErrorMessage() { return _errorMessage; }

    private CountDownTimer countdownTimer;

    @Inject
    public MeasurementViewModel(IMeasurementService measurementService) {
        this.measurementService = measurementService;
        resetToIdle(); // Ustawiamy stan początkowy
    }

    // Metoda wywoływana z fragmentu, żeby rozpocząć cały proces
    public void startMeasurementProcess(MeasurementType type) {
        if (_uiState.getValue() != UiState.IDLE && _uiState.getValue() != UiState.SUMMARY) {
            _errorMessage.setValue("Pomiar już trwa.");
            return;
        }
        startCountdown(type);
    }

    private void startCountdown(MeasurementType type) {
        _uiState.setValue(UiState.COUNTDOWN);
        countdownTimer = new CountDownTimer(4000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                _countdownText.setValue(seconds > 0 ? String.valueOf(seconds) : "START");
            }

            @Override
            public void onFinish() {
                startActualMeasurement(type);
            }
        }.start();
    }

    private void startActualMeasurement(MeasurementType type) {
        _uiState.setValue(UiState.MEASURING);
        Log.d("MeasurementVM", "START MEASURING");
        measurementService.startMeasurement(type,
                new IMeasurementService.LiveMeasurementDataListener() {
                    @Override public void onSpeedUpdate(float s) { _liveSpeedKmh.postValue(s); }
                    @Override public void onDistanceUpdate(double d) { _liveDistanceMeters.postValue(d); }
                    @Override public void onTimeUpdate(long t) { _liveTimeMillis.postValue(t); }
                },
                new IMeasurementService.MeasurementResultListener() {
                    @Override
                    public void onMeasurementCompleted(Measurement m) {
                        Log.d("MeasurementVM", "onMeasurementCompleted()");
                        // 1) Przekaż wynik
                        _measurementResult.postValue(m);
                        // 2) Ustaw stan UI na SUMMARY
                        _uiState.postValue(UiState.SUMMARY);
                    }
                    @Override
                    public void onMeasurementCancelled(String reason) {
                        Log.d("MeasurementVM", "onMeasurementCancelled(): " + reason);
                        _errorMessage.postValue("Pomiar anulowany: " + reason);
                        _uiState.postValue(UiState.IDLE);
                    }
                    @Override
                    public void onMeasurementError(Throwable t) {
                        Log.e("MeasurementVM", "onMeasurementError()", t);
                        _errorMessage.postValue("Błąd pomiaru: " + t.getMessage());
                        _uiState.postValue(UiState.IDLE);
                    }
                }
        );
    }

    // Metoda wywoływana z fragmentu, gdy user klika "Stop/Anuluj"
    public void stopCurrentMeasurement() {
        if (countdownTimer != null) {
            countdownTimer.cancel();
        }
        measurementService.stopCurrentMeasurement();
        resetToIdle(); // Zawsze wracaj do stanu spoczynku po zatrzymaniu
    }
    public void onSummaryResultConsumed() {
        _measurementResult.setValue(null);
    }

    public void prepareForNewMeasurement() {
        resetToIdle();
    }

    // Centralna metoda do resetowania stanu
    private void resetToIdle() {
        _uiState.setValue(UiState.IDLE);
        _countdownText.setValue("");
        _liveSpeedKmh.setValue(0.0f);
        _liveDistanceMeters.setValue(0.0);
        _liveTimeMillis.setValue(0L);
    }

    public void clearErrorMessage() {
        _errorMessage.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (countdownTimer != null) {
            countdownTimer.cancel();
        }
    }
}