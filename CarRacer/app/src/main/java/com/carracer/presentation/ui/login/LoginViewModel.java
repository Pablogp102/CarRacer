package com.carracer.presentation.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.carracer.application.services.IAuthService;
import com.carracer.domain.models.User;
import com.carracer.domain.utils.Callback;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LoginViewModel extends ViewModel {

    private final IAuthService authService;

    // LiveData do sygnalizowania stanu ładowania
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> getIsLoading() { return _isLoading; }

    // LiveData jako "zdarzenie" informujące o sukcesie i potrzebie nawigacji
    private final MutableLiveData<Boolean> _navigateToMainEvent = new MutableLiveData<>();
    public LiveData<Boolean> getNavigateToMainEvent() { return _navigateToMainEvent; }

    // LiveData jako "zdarzenie" do pokazywania błędów
    private final MutableLiveData<String> _errorEvent = new MutableLiveData<>();
    public LiveData<String> getErrorEvent() { return _errorEvent; }


    @Inject
    public LoginViewModel(IAuthService authService) {
        this.authService = authService;
    }

    public void login(String login, String password) {
        if (login.isEmpty() || password.isEmpty()) {
            _errorEvent.setValue("Login i hasło nie mogą być puste.");
            return;
        }
        _isLoading.setValue(true);
        authService.login(login, password, new Callback<User>() {
            @Override
            public void onSuccess(User user) {
                // Sukces! Wyślij sygnał do fragmentu. Używamy postValue, bo jesteśmy w callbacku z tła.
                _isLoading.postValue(false);
                _navigateToMainEvent.postValue(true);
            }

            @Override
            public void onError(Throwable t) {
                // Błąd! Wyślij wiadomość do fragmentu.
                _isLoading.postValue(false);
                _errorEvent.postValue(t.getMessage());
            }
        });
    }

    // Metody do "konsumowania" zdarzeń, żeby nie odpalały się ponownie np. po obrocie ekranu
    public void onNavigationComplete() {
        _navigateToMainEvent.setValue(null);
    }
    public void onErrorShown() {
        _errorEvent.setValue(null);
    }
}