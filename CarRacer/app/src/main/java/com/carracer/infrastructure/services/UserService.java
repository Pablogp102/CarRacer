package com.carracer.infrastructure.services;

import androidx.lifecycle.LiveData;

import com.carracer.application.services.IUserService;
import com.carracer.domain.models.User;
import com.carracer.domain.repositories.IUserRepository;


import javax.inject.Inject;

public class UserService implements IUserService {
    private final IUserRepository userRepository;
    @Inject
    public UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Override
    public LiveData<User> getCurrentUser() {
        return userRepository.getCurrentUser();
    }
}
