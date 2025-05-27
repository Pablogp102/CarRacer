package com.carracer.application.gps;

import android.location.Location;

import androidx.core.util.Consumer;

import com.carracer.domain.models.LocationData;

public interface IGPSManager {
    interface GPSListener {
        void onLocationData(LocationData data);
        void onCancelled(String reason);
    }

    void start(GPSListener listener);
    void stop();
    void setUpdateInterval(long ms);
    void reset();
}
