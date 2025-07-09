// src/main/java/com/carracer/presentation/ui/race/RaceFragment.java
package com.carracer.presentation.ui.race;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.lifecycle.ViewModelProvider;

import com.carracer.R;
import com.carracer.domain.utils.MeasurementType;
import com.carracer.presentation.ui.race.RaceFragmentDirections;
import com.carracer.presentation.ui.race.measurement.MeasurementTypeBottomSheetFragment;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Fragment odpowiedzialny za wyświetlanie głównego ekranu wyścigów,
 * w tym sprawdzanie uprawnień lokalizacyjnych i uruchamianie wyboru typu pomiaru.
 */
@AndroidEntryPoint
public class RaceFragment extends Fragment {

    private static final String TAG = "RaceFragment";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private FrameLayout raceButtonContainer; // FrameLayout działający jako przycisk

    // RaceViewModel może być użyty do innych danych RaceFragment, ale nie do logiki pomiarów GPS w tym kontekście.
    private RaceViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_race, container, false);

        raceButtonContainer = view.findViewById(R.id.race_button_container);

        // Od razu sprawdzamy uprawnienia lokalizacyjne.
        // UI będzie aktywne tylko jeśli uprawnienia są przyznane.
        checkLocationPermission();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicjalizacja ViewModelu.
        // Należy upewnić się, że Hilt prawidłowo dostarcza IGPSManager do konstruktora RaceViewModel.
        viewModel = new ViewModelProvider(this).get(RaceViewModel.class);

        // Ustawienie słuchacza kliknięcia na FrameLayout, który działa jak przycisk "Race"
        raceButtonContainer.setOnClickListener(v -> {
            // Sprawdź ponownie uprawnienia przed pokazaniem BottomSheet
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Race button (FrameLayout) clicked. Showing MeasurementTypeBottomSheetFragment.");
                // Tworzenie i wyświetlanie BottomSheetDialogFragment do wyboru typu pomiaru
                MeasurementTypeBottomSheetFragment bottomSheet = new MeasurementTypeBottomSheetFragment();
                bottomSheet.show(getParentFragmentManager(), MeasurementTypeBottomSheetFragment.TAG);
            } else {
                // Jeśli uprawnienia zostały cofnięte, poproś o nie ponownie
                Toast.makeText(requireContext(), "Potrzebne są uprawnienia lokalizacyjne do rozpoczęcia pomiaru.", Toast.LENGTH_SHORT).show();
                checkLocationPermission();
            }
        });

        // Ustawienie słuchacza wyników z MeasurementTypeBottomSheetFragment
        getParentFragmentManager().setFragmentResultListener(
                MeasurementTypeBottomSheetFragment.REQUEST_KEY,
                getViewLifecycleOwner(),
                (requestKey, bundle) -> {
                    if (requestKey.equals(MeasurementTypeBottomSheetFragment.REQUEST_KEY)) {
                        // Pobranie wybranego typu pomiaru z Bundle
                        MeasurementType selectedType = (MeasurementType) bundle.getSerializable(MeasurementTypeBottomSheetFragment.BUNDLE_KEY_MEASUREMENT_TYPE);
                        if (selectedType != null) {
                            Toast.makeText(requireContext(), "Wybrano: " + selectedType.getDisplayName(), Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Selected measurement type: " + selectedType.getDisplayName() + ". Navigating to MeasurementFragment.");

                            // Nawigacja do MeasurementFragment, przekazując wybrany typ pomiaru
                            // Upewnij się, że w pliku navigation.xml zdefiniowano argument 'measurementType'
                            // w akcji przechodzącej do MeasurementFragment.
                            NavHostFragment.findNavController(this).navigate(
                                    RaceFragmentDirections.actionNavigationRaceToMeasurementFragment(selectedType)
                            );
                        } else {
                            Log.w(TAG, "Selected measurement type was null from bottom sheet.");
                            Toast.makeText(requireContext(), "Nie wybrano typu pomiaru.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    /**
     * Sprawdza uprawnienia do lokalizacji. Jeśli nie są przyznane, prosi o nie.
     * W przeciwnym razie ustawia UI w trybie gotowości.
     */
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Location permission not granted. Requesting permission.");
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            Log.d(TAG, "Location permission already granted. Setting up UI.");
            setupRaceFragmentUI(true); // Ustaw UI na aktywny
        }
    }

    /**
     * Obsługuje wynik żądania uprawnień.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Uprawnienie lokalizacyjne przyznane", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Location permission granted after request.");
                setupRaceFragmentUI(true); // Aktywuj UI
            } else {
                Toast.makeText(requireContext(), "Aplikacja wymaga uprawnień do lokalizacji, aby przeprowadzać pomiary.", Toast.LENGTH_LONG).show();
                Log.w(TAG, "Location permission denied.");
                setupRaceFragmentUI(false); // Dezaktywuj UI i wyświetl komunikat
            }
        }
    }

    /**
     * Ustawia stan UI RaceFragment w zależności od tego, czy uprawnienia są przyznane.
     * @param permissionsGranted Czy uprawnienia lokalizacyjne są przyznane.
     */
    private void setupRaceFragmentUI(boolean permissionsGranted) {
        if (permissionsGranted) {
            raceButtonContainer.setEnabled(true);
            raceButtonContainer.setAlpha(1.0f); // Pełna widoczność
            // Jeśli RaceInfoTextView istniał, tutaj byś go ukrył/zmienił tekst na "Gotowy na wyścig"
            // W nowym layoutcie nie ma już tego TextView, więc usunięto jego obsługę.
        } else {
            raceButtonContainer.setEnabled(false);
            raceButtonContainer.setAlpha(0.5f); // Przyciemnij, aby wskazać nieaktywność
            // Tutaj można by wyświetlić Toast lub jakiś inny komunikat o braku uprawnień.
            // Obecnie jest to już w onRequestPermissionsResult.
        }
    }
}
