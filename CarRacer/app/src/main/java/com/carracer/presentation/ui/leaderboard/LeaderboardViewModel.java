package com.carracer.presentation.ui.leaderboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.carracer.domain.models.LeaderboardItem;
import com.carracer.application.usecases.leaderboard.GetLeaderboardUseCase;
import com.carracer.application.usecases.leaderboard.DeleteMeasurementUseCase;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LeaderboardViewModel extends ViewModel {

    public enum Mode { MY_MEASUREMENTS, GLOBAL }

    private final GetLeaderboardUseCase getLeaderboardUseCase;
    private final DeleteMeasurementUseCase deleteMeasurementUseCase;

    private final MutableLiveData<List<LeaderboardItem>> _items = new MutableLiveData<>();
    public LiveData<List<LeaderboardItem>> getItems() { return _items; }

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> getIsLoading() { return _isLoading; }

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> getError() { return _error; }

    private Mode currentMode = Mode.MY_MEASUREMENTS;
    private String currentTypeFilter = null;

    @Inject
    public LeaderboardViewModel(GetLeaderboardUseCase getLeaderboardUseCase, DeleteMeasurementUseCase deleteMeasurementUseCase) {
        this.getLeaderboardUseCase = getLeaderboardUseCase;
        this.deleteMeasurementUseCase = deleteMeasurementUseCase;
        fetchData(); // Pobierz dane na starcie
    }

    public void setMode(Mode mode) {
        if (currentMode == mode) return; // Nie rób nic, jeśli tryb się nie zmienia
        currentMode = mode;
        fetchData();
    }

    public void setFilter(String type) {
        currentTypeFilter = type;
        fetchData();
    }

    public void fetchData() {
        // Zawsze używamy postValue, bo nie wiemy, z jakiego wątku ta metoda będzie wywołana
        _isLoading.postValue(true);
        getLeaderboardUseCase.execute(currentMode, currentTypeFilter, result -> {
            _isLoading.postValue(false);
            if (result.isSuccess()) {
                _items.postValue(result.getData());
            } else {
                _error.postValue(result.getError().getMessage());
            }
        });
    }

    public void deleteMeasurement(String measurementId) {
        // Zawsze używamy postValue, żeby było bezpiecznie
        _isLoading.postValue(true);
        deleteMeasurementUseCase.execute(measurementId, result -> {
            _isLoading.postValue(false);
            if (result.isSuccess()) {
                fetchData(); // Odśwież listę po usunięciu
            } else {
                _error.postValue(result.getError().getMessage());
            }
        });
    }
}