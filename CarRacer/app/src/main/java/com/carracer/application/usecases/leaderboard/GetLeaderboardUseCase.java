package com.carracer.application.usecases.leaderboard;

import com.carracer.domain.models.LeaderboardItem;
import com.carracer.domain.repositories.ILeaderboardRepository;
import com.carracer.domain.utils.UseCaseResult;
import com.carracer.presentation.ui.leaderboard.LeaderboardViewModel.Mode;
import java.util.List;
import java.util.function.Consumer;
import javax.inject.Inject;

public class GetLeaderboardUseCase {
    private final ILeaderboardRepository repository;

    // Ta adnotacja mówi Hiltowi: "Wiem, jak stworzyć ten obiekt, po prostu daj mi ILeaderboardRepository"
    @Inject
    public GetLeaderboardUseCase(ILeaderboardRepository repository) {
        this.repository = repository;
    }

    public void execute(Mode mode, String typeFilter, Consumer<UseCaseResult<List<LeaderboardItem>>> callback) {
        repository.getLeaderboardItems(mode, typeFilter, callback);
    }
}