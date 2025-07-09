package com.carracer.domain.repositories;

import androidx.lifecycle.LiveData;

import com.carracer.domain.models.User;
import com.carracer.domain.utils.Callback;
import com.carracer.infrastructure.db.entities.UserEntity;

public interface IUserRepository {
    void insertUser(UserEntity user);
    LiveData<User> getCurrentUser();
    void getLoggedInUser(Callback<User> callback);
    void clearUserData();
}
