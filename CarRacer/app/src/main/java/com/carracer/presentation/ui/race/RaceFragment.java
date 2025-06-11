package com.carracer.presentation.ui.race;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.carracer.R;
import com.carracer.application.gps.IGPSManager;
import com.carracer.infrastructure.gps.GPSManager;

import java.util.Locale;

public class RaceFragment extends Fragment {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private TextView speedText, resultText;
    private Button startButton;
    private RaceViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_race, container, false);

        speedText = view.findViewById(R.id.speedText);
        resultText = view.findViewById(R.id.resultText);
        startButton = view.findViewById(R.id.startButton);

        checkLocationPermission(); // <== tutaj sprawdzamy uprawnienia

        return view;
    }

    private void setupViewModelAndListeners() {
        IGPSManager gpsManager = new GPSManager(requireContext());

        viewModel = new ViewModelProvider(this, new RaceViewModelFactory(gpsManager))
                .get(RaceViewModel.class);

        viewModel.getSpeed().observe(getViewLifecycleOwner(), speed -> {
            speedText.setText(String.format(Locale.US, "Prędkość: %.1f km/h", speed));
        });

        viewModel.getResult().observe(getViewLifecycleOwner(), ms -> {
            resultText.setText(String.format(Locale.US, "0-100 km/h: %.1f s", ms));
        });

        viewModel.getError().observe(getViewLifecycleOwner(), err -> {
            Toast.makeText(requireContext(), "Błąd: " + err, Toast.LENGTH_SHORT).show();
        });

        startButton.setOnClickListener(v -> {
            viewModel.startMeasurement();
        });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            setupViewModelAndListeners();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Uprawnienie przyznane", Toast.LENGTH_SHORT).show();
                setupViewModelAndListeners();
            } else {
                Toast.makeText(requireContext(), "Brak uprawnień do lokalizacji", Toast.LENGTH_LONG).show();
            }
        }
    }
}
