package com.carracer.infrastructure.db.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.carracer.domain.models.User;
import com.carracer.domain.repositories.IUserRepository;
import com.carracer.infrastructure.db.dao.UserDao;
import com.carracer.infrastructure.db.entities.UserEntity;

import java.util.UUID;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

public class UserRepository implements IUserRepository {

    private final UserDao userDao;
    private final ExecutorService databaseExecutor;

    @Inject
    public UserRepository(UserDao userDao, ExecutorService databaseExecutor) {
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
    public void clearUserData() {
        databaseExecutor.execute(() -> {
            userDao.deleteCurrentUser();
        });
    }
}
