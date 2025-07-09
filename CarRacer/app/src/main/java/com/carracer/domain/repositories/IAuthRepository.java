package com.carracer.domain.repositories;

import com.carracer.domain.models.User;
import com.carracer.domain.utils.Callback;
import com.carracer.infrastructure.network.models.LoginModel;

public interface IAuthRepository {
    void login(String login, String password, Callback<LoginModel> callback);
    void register(String login, String password, Callback<User> callback);
    void logout();
    boolean isLoggedIn();
    void deleteAccount(String id, Callback<Void> callback); }
