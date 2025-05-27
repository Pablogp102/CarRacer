package com.carracer.presentation.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.carracer.application.gps.AccelerationMeasurementService;
import com.carracer.application.gps.IGPSManager;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<Float> speed = new MutableLiveData<>();
    private final MutableLiveData<Float> result = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    private final AccelerationMeasurementService service;

    public HomeViewModel(IGPSManager gpsManager) {
        this.service = new AccelerationMeasurementService(gpsManager);

        service.setSpeedUpdateListener(speed::postValue);
    }

    public void startMeasurement() {
        service.startMeasurement(new AccelerationMeasurementService.ResultListener() {
            @Override
            public void onMeasured(float seconds) {
                result.postValue(seconds);
            }

            @Override
            public void onCancelled(String reason) {
                error.postValue(reason);
            }
        });
    }

    public void stopMeasurement() {
        service.stop();
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
