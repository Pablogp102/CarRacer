package com.carracer.domain.repositories;

import com.carracer.domain.models.LeaderboardItem;
import com.carracer.domain.utils.UseCaseResult;
import com.carracer.presentation.ui.leaderboard.LeaderboardViewModel;
import java.util.List;
import java.util.function.Consumer;

public interface ILeaderboardRepository {
    void getLeaderboardItems(LeaderboardViewModel.Mode mode, String typeFilter, Consumer<UseCaseResult<List<LeaderboardItem>>> callback);
}