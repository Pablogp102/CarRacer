// src/main/java/com/carracer/presentation/ui/race/RaceViewModel.java
package com.carracer.presentation.ui.race;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.carracer.application.gps.IGPSManager;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel; // Dodaj import HiltViewModel

/**
 * ViewModel dla RaceFragment.
 * Obecnie służy głównie jako kontener na dane LiveData, które mogą być wykorzystane
 * do wyświetlania na żywo prędkości lub innych informacji w RaceFragment,
 * jeśli taka funkcjonalność zostanie dodana w przyszłości.
 * Nie jest bezpośrednio zaangażowany w logikę wyboru typu pomiaru i nawigacji do MeasurementFragment.
 */
@HiltViewModel
public class RaceViewModel extends ViewModel {

    private final MutableLiveData<Float> speed = new MutableLiveData<>();
    private final MutableLiveData<Float> result = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    @Inject
    public RaceViewModel() {
    }
    public LiveData<Float> getSpeed() {
        return speed;
    }

    public LiveData<Float> getResult() {
        return result;
    }

    public LiveData<String> getError() {
        return error;
    }
}
