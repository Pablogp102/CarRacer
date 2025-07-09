package com.carracer.infrastructure.db.repositories;


import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.carracer.domain.models.User;
import com.carracer.domain.repositories.IUserRepository;
import com.carracer.domain.utils.Callback;
import com.carracer.infrastructure.db.dao.UserDao;
import com.carracer.infrastructure.db.entities.UserEntity;
import com.carracer.infrastructure.network.storage.TokenStorage;


import java.util.UUID;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

public class UserRepository implements IUserRepository {

    private static final String TAG = "UserRepository";
    private final TokenStorage tokenStorage;
    private final UserDao userDao;
    private final ExecutorService databaseExecutor;

    @Inject
    public UserRepository(TokenStorage tokenStorage, UserDao userDao, ExecutorService databaseExecutor) {
        this.tokenStorage = tokenStorage;
        this.userDao = userDao;
        this.databaseExecutor = databaseExecutor;
    }

    @Override
    public void insertUser(UserEntity user) {
        databaseExecutor.execute(() -> {
            userDao.insert(user);
        });
    }

    @Override
    public LiveData<User> getCurrentUser() {
        LiveData<UserEntity> userEntityLiveData = userDao.getCurrentUser();
        return Transformations.map(userEntityLiveData, userEntity -> {
            if (userEntity == null) {
                return null;
            }
            UUID userId = null;
            try {
                userId = UUID.fromString(userEntity.id);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }

            return new User(userId, userEntity.login, userEntity.createdAt);
        });
    }

    @Override
    public void getLoggedInUser(Callback<User> callback) {
        databaseExecutor.execute(() -> { // Wykonaj na wątku tła
            String currentUserId = tokenStorage.getUserId();
            if (currentUserId == null) {
                Log.d(TAG, "No current user ID found in TokenStorage. User is not logged in locally.");
                callback.onSuccess(null); // Brak zalogowanego użytkownika
                return;
            }
            try {
                UserEntity userEntity = userDao.getUserByIdSync(currentUserId); // Użycie synchronicznej metody DAO
                if (userEntity != null) {
                    UUID userId = UUID.fromString(userEntity.id);
                    User user = new User(userId, userEntity.login, userEntity.createdAt);
                    callback.onSuccess(user);
                    Log.d(TAG, "User data fetched from DB for ID: " + currentUserId);
                } else {
                    Log.d(TAG, "No user found in DB for ID: " + currentUserId + " after checking TokenStorage.");
                    callback.onSuccess(null); // Użytkownik nie znaleziony w DB (mimo ID w TokenStorage)
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting logged-in user data from DB: " + e.getMessage(), e);
                callback.onError(e);
            }
        });
    }

    @Override
    public void clearUserData() {
        databaseExecutor.execute(userDao::deleteCurrentUser);
    }
}
