package com.carracer.domain.models;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class User {
    private final UUID id;
    private final String login;
    @SerializedName("created_at")
    private final long createdAt;

    public User(UUID id, String login, long createdAt) {
        this.id = id;
        this.login = login;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getLogin() { return login; }
    public long getCreatedAt() { return createdAt; }
}
