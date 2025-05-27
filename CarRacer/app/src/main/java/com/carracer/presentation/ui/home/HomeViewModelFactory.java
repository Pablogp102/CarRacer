package com.carracer.presentation.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.carracer.application.gps.IGPSManager;

public class HomeViewModelFactory implements ViewModelProvider.Factory {

    private final IGPSManager gpsManager;

    public HomeViewModelFactory(IGPSManager gpsManager) {
        this.gpsManager = gpsManager;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new HomeViewModel(gpsManager);
    }
}
