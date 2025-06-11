package com.carracer.infrastructure.network.models.Responses;

import com.carracer.infrastructure.network.models.Dtos.UserDto;

public class AuthResponse {
    private boolean isSuccess;
    private String message;
    private String token;
    private UserDto user;

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    public UserDto getUser() {
        return user;
    }
}
