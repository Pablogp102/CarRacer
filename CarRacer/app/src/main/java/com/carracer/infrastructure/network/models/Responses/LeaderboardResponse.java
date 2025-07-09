package com.carracer.infrastructure.network.models.Responses;

import com.carracer.infrastructure.network.models.Dtos.LeaderboardDto;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class LeaderboardResponse {
    @SerializedName("isSuccess")
    private boolean isSuccess;
    @SerializedName("message")
    private String message;
    @SerializedName("leaderboard")
    private List<LeaderboardDto> leaderboard;

    public boolean isSuccess() { return isSuccess; }
    public String getMessage() { return message; }
    public List<LeaderboardDto> getLeaderboard() { return leaderboard; }
}
