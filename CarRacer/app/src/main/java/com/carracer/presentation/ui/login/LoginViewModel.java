package com.carracer.presentation.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.carracer.domain.models.User;
import com.carracer.infrastructure.services.AuthService;
import com.carracer.domain.utils.Callback;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LoginViewModel extends ViewModel {
    private final AuthService authService;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    @Inject
    public LoginViewModel(AuthService authService) {
        this.authService = authService;
    }

    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<User> getUser() { return userLiveData; }
    public LiveData<String> getError() { return errorLiveData; }

    public void login(String login, String password) {
        loading.setValue(true);
        authService.login(login, password, new Callback<User>() {
            @Override
            public void onSuccess(User user) {
                loading.postValue(false);
                userLiveData.postValue(user);
            }

            @Override
            public void onError(Throwable throwable) {
                loading.postValue(false);
                errorLiveData.postValue(throwable.getMessage());
            }
        });
    }
}
