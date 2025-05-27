package com.carracer.infrastructure.db;

import android.content.Context;

import androidx.room.Room;

public class DatabaseClient {
    private static DatabaseClient instance;
    private CarRacerDatabase database;

    private DatabaseClient(Context context) {
        database = Room.databaseBuilder(context, CarRacerDatabase.class, "carracer_db")
                .fallbackToDestructiveMigration()
                .build();
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
}
