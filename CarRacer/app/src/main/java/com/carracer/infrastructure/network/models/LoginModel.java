package com.carracer.infrastructure.network.models;

import com.carracer.domain.models.Measurement;
import com.carracer.domain.models.User;

import java.util.List;

public class LoginModel {
    private final User user;
    private final List<Measurement> measurements;

    public LoginModel(User user, List<Measurement> measurements) {
        this.user = user;
        this.measurements = measurements;
    }

    public User getUser() {
        return user;
    }

    public List<Measurement> getMeasurements() {
        return measurements;
    }
}
