package com.carracer.infrastructure.services;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.carracer.application.services.IAuthService;
import com.carracer.domain.models.Measurement;
import com.carracer.domain.models.User;
import com.carracer.domain.repositories.IAuthRepository;
import com.carracer.domain.repositories.IMeasurementsRepository;
import com.carracer.domain.repositories.IUserRepository;
import com.carracer.domain.utils.Callback;
import com.carracer.domain.utils.ModelMapper;
import com.carracer.infrastructure.db.entities.MeasurementEntity;
import com.carracer.infrastructure.db.entities.UserEntity;
import com.carracer.infrastructure.network.models.LoginModel;
import com.carracer.infrastructure.network.storage.TokenStorage;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

public class AuthService implements IAuthService {
    private static final String TAG = "AuthService";
    private final IAuthRepository authRepository;
    private final IUserRepository userRepository;
    private final IMeasurementsRepository measurementsRepository;
    private final TokenStorage tokenStorage;

    @Inject
    public AuthService(IAuthRepository authRepository, IUserRepository userRepository, IMeasurementsRepository measurementsRepository, TokenStorage tokenStorage) {
        this.authRepository = authRepository;
        this.userRepository = userRepository;
        this.measurementsRepository = measurementsRepository;
        this.tokenStorage = tokenStorage;
    }
    @Override
    public void login(String login, String password, Callback<User> finalCallback) {
        authRepository.login(login, password, new Callback<LoginModel>() {
            @Override
            public void onSuccess(LoginModel loginModel) {
                User domainUser = loginModel.getUser();
                List<Measurement> domainMeasurements = loginModel.getMeasurements();

                if (domainUser == null) {
                    finalCallback.onError(new Exception("Błąd logowania: brak danych użytkownika."));
                    return;
                }

                userRepository.insertUser(ModelMapper.fromDomain(domainUser));

                if (domainMeasurements != null && !domainMeasurements.isEmpty()) {
                    List<MeasurementEntity> measurementEntities = domainMeasurements.stream()
                            .map(m -> {
                                MeasurementEntity entity = ModelMapper.fromDomainModel(m);
                                entity.userId = domainUser.getId().toString();
                                entity.isSynced = true;
                                return entity;
                            })
                            .collect(Collectors.toList());

                    // Zleć repozytorium zapisanie całej paczki
                    measurementsRepository.insertAll(measurementEntities, new Callback<Void>() {
                        @Override public void onSuccess(Void r) {
                            Log.d("AuthService", "Zapisano usera i jego pomiary.");
                            finalCallback.onSuccess(domainUser);
                        }
                        @Override public void onError(Throwable t) {
                            Log.e("AuthService", "Zapisano usera, ale nie udało się zapisać pomiarów.", t);
                            finalCallback.onSuccess(domainUser);
                        }
                    });
                }

                // Zwracamy sukces od razu, zapis w tle się wykona
                finalCallback.onSuccess(domainUser);
            }

            @Override
            public void onError(Throwable t) {
                finalCallback.onError(t);
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
    public void logout(Callback<Integer> unsyncedItemsCallback) {
        userRepository.getLoggedInUser(new Callback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null || user.getId() == null) {
                    unsyncedItemsCallback.onSuccess(0);
                    return;
                }
                String userId = user.getId().toString();
                measurementsRepository.getUnsyncedMeasurements(userId, new Callback<List<Measurement>>() {
                    @Override
                    public void onSuccess(List<Measurement> measurements) {
                        unsyncedItemsCallback.onSuccess(measurements.size());
                    }
                    @Override
                    public void onError(Throwable t) {
                        unsyncedItemsCallback.onError(t);
                    }
                });
            }
            @Override
            public void onError(Throwable t) {
                unsyncedItemsCallback.onError(t);
            }
        });
    }

    @Override
    public void finalizeLogout(boolean sync, Callback<Void> finalCallback) {
        userRepository.getLoggedInUser(new Callback<User>() {
            @Override
            public void onSuccess(User user) {
                String userId = (user != null && user.getId() != null) ? user.getId().toString() : null;

                if (sync && userId != null) {
                    // SCENARIUSZ 1: Użytkownik wybrał "Synchronizuj i Wyloguj"
                    measurementsRepository.syncMeasurementsToCloud(userId, new Callback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Log.d(TAG, "Finalna synchronizacja udana. Rozpoczynam czyszczenie danych.");
                            // Skoro synchronizacja się udała, czyścimy wszystko i kończymy.
                            performFullCleanup(userId, finalCallback);
                        }

                        @Override
                        public void onError(Throwable t) {
                            // --- KLUCZOWA ZMIANA ---
                            // Jeśli synchronizacja się NIE UDAŁA, przerywamy wylogowanie i zwracamy błąd.
                            Log.e(TAG, "Finalna synchronizacja nieudana. PRZERYWAM Wylogowanie.", t);
                            finalCallback.onError(new Exception("Synchronizacja nie powiodła się. Spróbuj ponownie później."));
                        }
                    });
                } else {
                    // SCENARIUSZ 2: Użytkownik wybrał "Porzuć i Wyloguj"
                    Log.d(TAG, "Użytkownik pominął synchronizację. Rozpoczynam czyszczenie danych.");
                    performFullCleanup(userId, finalCallback);
                }
            }
            @Override
            public void onError(Throwable t) {
                performFullCleanup(null, finalCallback);
            }
        });
    }

    // Prywatna metoda, która wykonuje ostateczne czyszczenie lokalnych danych
    private void performFullCleanup(String userId, Callback<Void> callback) {
        Log.d(TAG, "Rozpoczynam pełne czyszczenie danych lokalnych...");

        // Krok A: Wyczyść token JWT
        authRepository.logout();

        if (userId != null) {
            // Krok B: Wyczyść WSZYSTKIE pomiary tego usera z lokalnej bazy
            measurementsRepository.deleteAllMeasurements(userId, new Callback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    // Krok C: Dopiero po usunięciu pomiarów, usuń dane usera
                    userRepository.clearUserData();
                    Log.d(TAG, "Wszystkie dane lokalne wyczyszczone.");
                    callback.onSuccess(null); // Dopiero teraz informujemy, że proces się zakończył.
                }

                @Override
                public void onError(Throwable t) {
                    Log.e(TAG, "Błąd podczas czyszczenia pomiarów, ale kontynuujemy.", t);
                    userRepository.clearUserData();
                    callback.onSuccess(null);
                }
            });
        } else {
            // Jeśli nie było ID usera, po prostu wyczyść co się da
            userRepository.clearUserData();
            callback.onSuccess(null);
        }
    }
    @Override
    public void deleteAccount(Callback<String> callback) {
        String userIdToDelete = tokenStorage.getUserId();
        if (userIdToDelete == null) {
            callback.onError(new Exception("Brak ID użytkownika do usunięcia."));
            return;
        }

        authRepository.deleteAccount(userIdToDelete, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                performFullCleanup(userIdToDelete, new Callback<Void>() {
                    @Override
                    public void onSuccess(Void cleanupResult) {
                        Log.d(TAG, "Konto usunięte z serwera, dane lokalne wyczyszczone.");
                        callback.onSuccess("Konto zostało pomyślnie usunięte!");
                    }
                    @Override
                    public void onError(Throwable t) {
                        // Mimo błędu czyszczenia, operacja z punktu widzenia usera się udała
                        Log.e(TAG, "Błąd podczas czyszczenia danych po usunięciu konta.", t);
                        callback.onSuccess("Konto usunięte z serwera, wystąpił błąd przy czyszczeniu danych lokalnych.");
                    }
                });
            }

            @Override
            public void onError(Throwable t) {
                Log.e(TAG, "Błąd podczas usuwania konta w AuthRepository: " + t.getMessage(), t);
                callback.onError(t);
            }
        });
    }
}
