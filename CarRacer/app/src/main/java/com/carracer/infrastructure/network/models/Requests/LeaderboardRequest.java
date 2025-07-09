package com.carracer.infrastructure.network.models.Requests;

public class LeaderboardRequest {
    private String Type;
    public LeaderboardRequest(String type) {
     this.Type = type;
    }
    public String getType() {
        return Type;
    }
}
