package com.carracer.infrastructure.network.models.Dtos;

import java.util.List;

public class UserDto {
    private String id;
    private String login;
    private long createdAt;
    private List<MeasurementDto> measurements;

    public String getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public List<MeasurementDto> getMeasurements() {
        return measurements;
    }
}
