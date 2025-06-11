package com.carracer.infrastructure.services;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.carracer.application.services.IAuthService;
import com.carracer.domain.models.User;
import com.carracer.domain.repositories.IAuthRepository;
import com.carracer.domain.repositories.IMeasurementsRepository;
import com.carracer.domain.repositories.IUserRepository;
import com.carracer.domain.utils.Callback;
import com.carracer.infrastructure.db.entities.UserEntity;

import javax.inject.Inject;

public class AuthService implements IAuthService {
    private static final String TAG = "AuthService";
    private final IAuthRepository authRepository;
    private final IUserRepository userRepository;
    private final IMeasurementsRepository measurementsRepository;
    @Inject
    public AuthService(IAuthRepository authRepository, IUserRepository userRepository, IMeasurementsRepository measurementsRepository) {
        this.authRepository = authRepository;
        this.userRepository = userRepository;
        this.measurementsRepository = measurementsRepository;
    }
    @Override
    public void login(String login, String password, Callback<User> callback) {
        authRepository.login(login, password, new Callback<User>() {
            @Override
            public void onSuccess(User domainUser) {
                if (domainUser != null) {
                    UserEntity userEntity = new UserEntity(
                            domainUser.getId().toString(),
                            domainUser.getLogin(),
                            domainUser.getCreatedAt()
                    );
                    userRepository.insertUser(userEntity);
                }
                callback.onSuccess(domainUser);
            }

            @Override
            public void onError(Throwable t) {
                callback.onError(t); // Pass the error up
            }
        });
    }
    @Override
    public void register(String login, String password, Callback<User> callback) {
        authRepository.register(login, password, new Callback<User>() {
            @Override
            public void onSuccess(User domainUser) {
                if(domainUser != null) {
                    UserEntity userEntity = new UserEntity(
                            domainUser.getId().toString(),
                            domainUser.getLogin(),
                            domainUser.getCreatedAt()
                    );
                userRepository.insertUser(userEntity);
                }
                callback.onSuccess(domainUser);
            }

            @Override
            public void onError(Throwable t) {
                callback.onError(t);
            }
        });
    }
    @Override
    public void logout() {
        authRepository.logout();
        userRepository.clearUserData();
        //TODO: sync measurements
        //TODO: Clear measurements
    }

    @Override
    public void deleteAccount(Callback<String> callback) {
        LiveData<User> currentUserLiveData = userRepository.getCurrentUser();

        final Observer<User> userObserver = new Observer<User>() {
            @Override
            public void onChanged(User user) {
                currentUserLiveData.removeObserver(this);

                if (user != null) {
                    if (user.getId() != null) {
                        String userIdToDelete = user.getId().toString();
                        Log.d(TAG, "Pobrano ID użytkownika z bazy Room do usunięcia: " + userIdToDelete);

                        authRepository.deleteAccount(userIdToDelete, new Callback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                logout();
                                Log.d(TAG, "Account deleted successfully from server. Local data cleared.");
                                callback.onSuccess("Konto zostało pomyślnie usunięte!");
                            }

                            @Override
                            public void onError(Throwable t) {
                                Log.e(TAG, "Error during account deletion from AuthRepository: " + t.getMessage(), t);
                                callback.onError(t);
                            }
                        });
                    } else {
                        Log.e(TAG, "User object from Room has null ID. Cannot delete account.");
                        callback.onError(new Exception("Błąd: Dane użytkownika są niekompletne (brak ID)."));
                    }
                } else {
                    Log.e(TAG, "No user data found in Room database. Cannot delete account.");
                    callback.onError(new Exception("Błąd: Nie znaleziono danych użytkownika w bazie lokalnej. Proszę zalogować się ponownie."));
                }
            }
        };
        currentUserLiveData.observeForever(userObserver);
    }
}
