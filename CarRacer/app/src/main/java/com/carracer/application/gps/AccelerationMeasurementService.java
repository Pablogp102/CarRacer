package com.carracer.application.gps;

import com.carracer.domain.models.LocationData;

public class AccelerationMeasurementService {

    public interface ResultListener {
        void onMeasured(float seconds);
        void onCancelled(String reason);
    }

    public interface SpeedUpdateListener {
        void onSpeedUpdated(float speed);
    }

    private SpeedUpdateListener speedListener;
    private final IGPSManager gpsManager;
    private boolean measuring = false;
    private float startTime = 0;

    public AccelerationMeasurementService(IGPSManager gpsManager) {
        this.gpsManager = gpsManager;
    }

    public void setSpeedUpdateListener(SpeedUpdateListener listener) {
        this.speedListener = listener;
    }

    public void startMeasurement(ResultListener listener) {
        if (measuring) return;

        gpsManager.reset();
        measuring = true;

        gpsManager.start(new IGPSManager.GPSListener() {
            @Override
            public void onLocationData(LocationData data) {
                float speed = data.getSpeedKmh();

                if (speedListener != null) {
                    speedListener.onSpeedUpdated(speed);
                }

                if (speed >= 5 && startTime == 0) {
                    startTime = data.getTimestamp();
                }

                if (speed >= 100 && startTime > 0) {
                    float result = data.getTimestamp() - startTime;
                    stop();
                    listener.onMeasured(result);
                }
            }

            @Override
            public void onCancelled(String reason) {
                listener.onCancelled(reason);
                stop();
            }
        });
    }

    public void stop() {
        if (!measuring) return;

        gpsManager.stop();
        measuring = false;
        startTime = 0;
    }
}
