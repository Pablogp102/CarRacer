package com.carracer.infrastructure.hilt;

import android.content.Context;

import androidx.room.Room;

import com.carracer.domain.utils.Converters;
import com.carracer.infrastructure.db.CarRacerDatabase;
import com.carracer.infrastructure.db.dao.MeasurementDao;
import com.carracer.infrastructure.db.dao.UserDao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    public CarRacerDatabase provideAppDatabase(@ApplicationContext Context context, Converters converters) {
        return Room.databaseBuilder(context.getApplicationContext(), CarRacerDatabase.class, "carracer_db")
                .fallbackToDestructiveMigration() // Pamiętaj o obsłudze migracji w produkcji!
                //.addTypeConverter(converters) // Dodaj konwertery dla Room
                .build();
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
    public ExecutorService provideDatabaseExecutor() {
        return Executors.newFixedThreadPool(2); // Używaj sensownej liczby wątków
    }
}
