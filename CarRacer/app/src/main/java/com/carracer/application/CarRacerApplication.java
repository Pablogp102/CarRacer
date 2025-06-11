package com.carracer.application;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class CarRacerApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Tutaj ustaw globalnie tryb nocny
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }
}
