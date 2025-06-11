package com.carracer.application.services;

import androidx.lifecycle.LiveData;

import com.carracer.domain.models.User;

public interface IUserService {
    LiveData<User> getCurrentUser();
}
