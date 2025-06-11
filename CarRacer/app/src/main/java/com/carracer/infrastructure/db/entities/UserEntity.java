package com.carracer.infrastructure.db.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;



@Entity(tableName = "users")
public class UserEntity {
    @PrimaryKey
    @NonNull
    public String id;

    @ColumnInfo(name = "login")
    @NonNull
    public String login;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    public UserEntity(@NonNull String id, @NonNull String login, long createdAt) {
        this.id = id;
        this.login = login;
        this.createdAt = createdAt;
    }

    @NonNull
    public String getId() {
        return id;
    }
    @NonNull
    public String getLogin() {
        return login;
    }
    public long getCreatedAt() {
        return createdAt;
    }
}