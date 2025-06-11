package com.carracer.infrastructure.db;

import android.content.Context;

import androidx.room.Room;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseClient {
    private static DatabaseClient instance;
    private CarRacerDatabase database;
    private final ExecutorService databaseWriteExecutor;

    private static final int NUMBER_OF_THREADS = 4;

    private DatabaseClient(Context context) {
        database = Room.databaseBuilder(context.getApplicationContext(), CarRacerDatabase.class, "carracer_db")
                .fallbackToDestructiveMigration()
                .build();

        databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    }



    public static synchronized DatabaseClient getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseClient(context);
        }
        return instance;
    }
    public CarRacerDatabase getDatabase() {
        return database;
    }

    public ExecutorService getDatabaseExecutor() { return databaseWriteExecutor; }
}
