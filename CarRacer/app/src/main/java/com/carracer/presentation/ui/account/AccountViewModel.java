package com.carracer.presentation.ui.account;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.carracer.application.services.IAuthService;
import com.carracer.application.services.IUserService;
import com.carracer.domain.models.User;
import com.carracer.domain.utils.Callback;
import com.carracer.domain.utils.DateFormatter;
import com.carracer.infrastructure.services.AuthService;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AccountViewModel extends ViewModel {
    private final IAuthService authService;
    private final IUserService userService;
    private final MutableLiveData<String> username = new MutableLiveData<>();
    private final MutableLiveData<String> createdAt = new MutableLiveData<>();

    private LiveData<User> currentUserSourceLiveData;
    private Observer<User> userObserver;

    private String currentUserId;
    @Inject
    public AccountViewModel(IAuthService authService, IUserService userService) {
        this.authService = authService;
        this.userService = userService;
        loadUserData();
    }

    private void loadUserData() {
        if (currentUserSourceLiveData != null && userObserver != null) {
            currentUserSourceLiveData.removeObserver(userObserver);
        }

        this.currentUserSourceLiveData = userService.getCurrentUser();

        this.userObserver = new Observer<User>() {
            @Override
            public void onChanged(User user) {
                if (user != null) {
                    username.setValue(user.getLogin());
                    if (user.getCreatedAt() == 0L) {
                        createdAt.setValue("Nieznany");
                    } else {
                        createdAt.setValue(DateFormatter.formatTimestamp(user.getCreatedAt()));
                    }
                } else {
                    username.setValue("Gościu");
                    createdAt.setValue("Nieznany");
                }
            }
        };

        this.currentUserSourceLiveData.observeForever(this.userObserver);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (currentUserSourceLiveData != null && userObserver != null) {
            currentUserSourceLiveData.removeObserver(userObserver);
        }
    }

    public void logout() {
        authService.logout();
        username.setValue("Gościu");
        createdAt.setValue("Nieznany");
    }

    public void deleteAccount(Callback<String> callback) {
        authService.deleteAccount(callback);
    }
    public LiveData<String> getUsername() {
        return username;
    }

    public LiveData<String> getCreatedAt() {
        return createdAt;
    }
}
