package com.carracer.infrastructure.hilt;

import android.content.Context;

import androidx.room.Room;

import com.carracer.infrastructure.db.CarRacerDatabase;
import com.carracer.infrastructure.db.DatabaseClient;
import com.carracer.infrastructure.db.dao.MeasurementDao;
import com.carracer.infrastructure.db.dao.UserDao;

import java.util.concurrent.ExecutorService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {
    @Provides
    @Singleton
    public CarRacerDatabase provideAppDatabase(@ApplicationContext Context context) {
        return DatabaseClient.getInstance(context).getDatabase();
    }

    @Provides
    @Singleton
    public MeasurementDao provideMeasurementDao(CarRacerDatabase database) {
        return database.measurementDao();
    }

    @Provides
    @Singleton
    public UserDao provideUserDao(CarRacerDatabase database) {
        return database.userDao();
    }

    @Provides
    @Singleton
    public ExecutorService provideDatabaseExecutor(@ApplicationContext Context context) {
        return DatabaseClient.getInstance(context).getDatabaseExecutor();
    }
}
