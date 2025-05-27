package com.carracer.infrastructure.gps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;

import androidx.core.app.ActivityCompat;

import com.carracer.application.gps.IGPSManager;
import com.carracer.domain.models.LocationData;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Priority;


/**
 * Zarządza cyklicznym odczytem danych GPS:
 * - co updateIntervalMs tworzy nowy obiekt LocationData
 * - oblicza deltaDistance i totalDistance
 * - zwraca snapshot przez GPSListener
 */
public class GPSManager implements IGPSManager {

    private final FusedLocationProviderClient fusedClient;
    private final Context context;
    private LocationCallback locationCallback;

    private Location lastLocation;
    private double totalDistanceMeters = 0.0;
    private long updateIntervalMs = 100;

    public GPSManager(Context context) {
        this.context = context;
        this.fusedClient = LocationServices.getFusedLocationProviderClient(context);
    }

    @Override
    public void setUpdateInterval(long ms) {
        this.updateIntervalMs = ms;
    }

    @Override
    public void reset() {
        this.totalDistanceMeters = 0.0;
        this.lastLocation = null;
    }
    @Override
    @SuppressLint("MissingPermission")
    public void start(final GPSListener listener) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            throw new IllegalStateException("Brak uprawnień ACCESS_FINE_LOCATION");
        }

        LocationRequest req = new LocationRequest.Builder(updateIntervalMs)
                .setIntervalMillis(updateIntervalMs)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build();


        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                Location loc = result.getLastLocation();
                if (loc != null) {
                    processNewLocation(loc, listener);
                }
            }
        };
        try{
            fusedClient.requestLocationUpdates(req, locationCallback, Looper.getMainLooper());
        }
        catch (Exception e){
            listener.onCancelled("GPS error: " + e.getMessage());
        }
    }
    @Override
    public void stop() {
        if (locationCallback != null) {
            fusedClient.removeLocationUpdates(locationCallback);
        }
    }

    private void processNewLocation(Location loc, GPSListener listener) {
        float speedKmh = loc.hasSpeed() ? loc.getSpeed() * 3.6f : 0f;
        long ts = System.currentTimeMillis();

        double delta = 0.0;
        if (lastLocation != null) {
            delta = loc.distanceTo(lastLocation);
            totalDistanceMeters += delta;
        }
        lastLocation = loc;

        LocationData data = new LocationData(
                loc.getLatitude(),
                loc.getLongitude(),
                speedKmh,
                totalDistanceMeters,
                delta,
                ts
        );

        listener.onLocationData(data);
    }
}
