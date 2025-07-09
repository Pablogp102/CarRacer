package com.carracer.infrastructure.repositories;

import com.carracer.domain.models.LeaderboardItem;
import com.carracer.domain.repositories.ILeaderboardRepository;
import com.carracer.domain.utils.UseCaseResult;
import com.carracer.infrastructure.db.dao.MeasurementDao;
import com.carracer.infrastructure.db.entities.MeasurementEntity;
import com.carracer.infrastructure.network.ApiService;
import com.carracer.infrastructure.network.models.Requests.LeaderboardRequest;
import com.carracer.infrastructure.network.models.Responses.LeaderboardResponse;
import com.carracer.infrastructure.network.storage.TokenStorage;
import com.carracer.presentation.ui.leaderboard.LeaderboardViewModel.Mode;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.inject.Inject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeaderboardRepository implements ILeaderboardRepository {
    private final ApiService apiService;
    private final MeasurementDao measurementDao;
    private final ExecutorService databaseExecutor;
    private final TokenStorage tokenStorage;

    @Inject
    public LeaderboardRepository(ApiService apiService, MeasurementDao measurementDao, ExecutorService databaseExecutor, TokenStorage tokenStorage) {
        this.apiService = apiService;
        this.measurementDao = measurementDao;
        this.databaseExecutor = databaseExecutor;
        this.tokenStorage = tokenStorage;
    }

    @Override
    public void getLeaderboardItems(Mode mode, String typeFilter, Consumer<UseCaseResult<List<LeaderboardItem>>> callback) {
        if (mode == Mode.GLOBAL) {
            fetchFromApi(typeFilter, callback);
        } else {
            fetchFromLocalDb(typeFilter, callback);
        }
    }

    private void fetchFromApi(String typeFilter, Consumer<UseCaseResult<List<LeaderboardItem>>> callback) {
        LeaderboardRequest request = new LeaderboardRequest(typeFilter);
        apiService.getLeaderboard(request).enqueue(new Callback<LeaderboardResponse>() {
            @Override
            public void onResponse(Call<LeaderboardResponse> call, Response<LeaderboardResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<LeaderboardItem> items = response.body().getLeaderboard().stream()
                            .map(dto -> new LeaderboardItem(dto.getId(), dto.getUserLogin(), dto.getType(), dto.getDurationS(), dto.getPeakSpeedKmh(), dto.getDistanceMeters(), dto.getMeasuredAt(), false))
                            .collect(Collectors.toList());
                    callback.accept(UseCaseResult.success(items));
                } else {
                    callback.accept(UseCaseResult.error(new Exception("Błąd pobierania rankingu globalnego.")));
                }
            }
            @Override
            public void onFailure(Call<LeaderboardResponse> call, Throwable t) {
                callback.accept(UseCaseResult.error(t));
            }
        });
    }

    private void fetchFromLocalDb(String typeFilter, Consumer<UseCaseResult<List<LeaderboardItem>>> callback) {
        String userId = tokenStorage.getUserId();
        if (userId == null) {
            callback.accept(UseCaseResult.success(Collections.emptyList())); // Zwróć pustą listę zamiast błędu
            return;
        }

        databaseExecutor.execute(() -> {
            try {
                List<MeasurementEntity> localMeasurements = measurementDao.getMeasurementsForUser(userId, typeFilter);
                List<LeaderboardItem> items = localMeasurements.stream()
                        .map(entity -> new LeaderboardItem(entity.id, "Ty", entity.type, entity.durationS, entity.peakSpeedKmh, entity.distanceMeters, entity.timestamp, true))
                        .collect(Collectors.toList());
                callback.accept(UseCaseResult.success(items));
            } catch (Exception e) {
                callback.accept(UseCaseResult.error(e));
            }
        });
    }
}
