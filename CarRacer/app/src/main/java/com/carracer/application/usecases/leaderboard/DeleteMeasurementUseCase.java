package com.carracer.application.usecases.leaderboard;

import com.carracer.domain.repositories.IMeasurementsRepository;
import com.carracer.domain.utils.Callback;
import com.carracer.domain.utils.UseCaseResult;
import java.util.function.Consumer;
import javax.inject.Inject;

public class DeleteMeasurementUseCase {
    private final IMeasurementsRepository repository;

    @Inject
    public DeleteMeasurementUseCase(IMeasurementsRepository repository) {
        this.repository = repository;
    }

    public void execute(String measurementId, Consumer<UseCaseResult<Void>> callback) {
        repository.deleteMeasurement(measurementId, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                callback.accept(UseCaseResult.success(null));
            }
            @Override
            public void onError(Throwable t) {
                callback.accept(UseCaseResult.error(t));
            }
        });
    }
}
