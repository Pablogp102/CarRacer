package com.carracer.application.services;

import com.carracer.domain.models.User;
import com.carracer.domain.utils.Callback;

public interface IAuthService {
    void login(String login, String password, Callback<User> callback);
    void register(String login, String password, Callback<User> callback);
    void logout();
    void deleteAccount(Callback<String> callback);
}
