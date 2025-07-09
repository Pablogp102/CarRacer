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
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> getErrorMessage() { return _errorMessage; }
    private final MutableLiveData<Integer> _showSyncDialogEvent = new MutableLiveData<>();
    public LiveData<Integer> getShowSyncDialogEvent() { return _showSyncDialogEvent; }
    private final MutableLiveData<Boolean> _navigateToLoginEvent = new MutableLiveData<>();
    public LiveData<Boolean> getNavigateToLoginEvent() { return _navigateToLoginEvent; }
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

    public void initiateLogout() {
        authService.logout(new Callback<Integer>() {
            @Override
            public void onSuccess(Integer unsyncedCount) {
                if (unsyncedCount > 0) {
                    // Mamy niesynchronizowane dane - pokaż dialog
                    _showSyncDialogEvent.postValue(unsyncedCount);
                } else {
                    // Nie ma nic do synchronizacji - wyloguj od razu
                    finalizeLogout(false);
                }
            }
            @Override
            public void onError(Throwable t) {
                _errorMessage.postValue("Błąd przy sprawdzaniu danych: " + t.getMessage());
                // Mimo błędu, pozwalamy na wylogowanie
                finalizeLogout(false);
            }
        });
    }

    public void finalizeLogout(boolean shouldSync) {
        authService.finalizeLogout(shouldSync, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                _navigateToLoginEvent.postValue(true);
            }
            @Override
            public void onError(Throwable t) {
                _errorMessage.postValue("Błąd podczas wylogowywania: " + t.getMessage());
            }
        });
    }

    public void deleteAccount(Callback<String> callback) {
        authService.deleteAccount(callback);
    }

    public void onNavigateToLoginConsumed() { _navigateToLoginEvent.setValue(null); }
    public void onShowSyncDialogConsumed() { _showSyncDialogEvent.setValue(null); }
    public void clearErrorMessage() { _errorMessage.setValue(null); }


    public LiveData<String> getUsername() {
        return username;
    }

    public LiveData<String> getCreatedAt() {
        return createdAt;
    }
}
