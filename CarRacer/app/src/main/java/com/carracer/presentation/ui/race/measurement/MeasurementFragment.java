package com.carracer.presentation.ui.race.measurement;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.carracer.databinding.FragmentMeasurementBinding;
import com.carracer.domain.models.Measurement;
import com.carracer.domain.utils.MeasurementType;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MeasurementFragment extends Fragment {

    private MeasurementViewModel viewModel;
    private FragmentMeasurementBinding binding;
    private MeasurementType currentMeasurementType;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMeasurementBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MeasurementViewModel.class);

        // Pobieramy typ pomiaru z argumentów nawigacji
        if (getArguments() != null) {
            currentMeasurementType = MeasurementFragmentArgs.fromBundle(getArguments()).getMeasurementType();
        }

        setupListeners();
        setupObservers();
    }

    private void setupListeners() {
        // Główny przycisk akcji - jego działanie zależy od aktualnego stanu UI
        binding.actionButton.setOnClickListener(v -> {
            MeasurementViewModel.UiState currentState = viewModel.getUiState().getValue();
            if (currentState == MeasurementViewModel.UiState.IDLE || currentState == MeasurementViewModel.UiState.SUMMARY) {
                viewModel.startMeasurementProcess(currentMeasurementType);
            } else {
                viewModel.stopCurrentMeasurement();
            }
        });

        // Przycisk "Wróć" - zawsze robi to samo
        binding.goBackButton.setOnClickListener(v -> {
            viewModel.stopCurrentMeasurement(); // Na wszelki wypadek zatrzymaj wszystko
            Navigation.findNavController(v).popBackStack();
        });

        // Nasłuchujemy na akcje z dialogu podsumowania
        getParentFragmentManager().setFragmentResultListener(
                MeasurementSummaryDialogFragment.REQUEST_KEY_SUMMARY_ACTION,
                getViewLifecycleOwner(),
                (requestKey, bundle) -> {
                    String action = bundle.getString(MeasurementSummaryDialogFragment.BUNDLE_KEY_ACTION);
                    if (MeasurementSummaryDialogFragment.ACTION_GO_BACK.equals(action)) {
                        Navigation.findNavController(requireView()).popBackStack();
                    } else if (MeasurementSummaryDialogFragment.ACTION_NEW_MEASUREMENT.equals(action)) {
                        viewModel.startMeasurementProcess(currentMeasurementType);
                    }
                }
        );
    }

    private void setupObservers() {
        // Główny obserwator, który steruje całym UI
        viewModel.getUiState().observe(getViewLifecycleOwner(), this::updateUiForState);

        // Obserwatory, które tylko aktualizują tekst/dane na żywo
        viewModel.getCountdownText().observe(getViewLifecycleOwner(), text -> binding.countdownTextView.setText(text));
        viewModel.getLiveSpeedKmh().observe(getViewLifecycleOwner(), speed -> binding.liveSpeedTextView.setText(String.format("Prędkość: %.1f km/h", speed)));
        viewModel.getLiveTimeMillis().observe(getViewLifecycleOwner(), time -> binding.liveTimeTextView.setText(String.format("Czas: %.1f s", time / 1000.0f)));
        viewModel.getLiveDistanceMeters().observe(getViewLifecycleOwner(), distance -> binding.liveDistanceTextView.setText(String.format("Dystans: %.2f m", distance)));

        // Obserwator, który reaguje na nowy wynik i pokazuje dialog
        viewModel.getMeasurementResult().observe(getViewLifecycleOwner(), measurement -> {
            if (measurement != null && isAdded() && !isRemoving()) {
                MeasurementSummaryDialogFragment.newInstance(measurement)
                        .show(getParentFragmentManager(), "MeasurementSummaryDialog");
                viewModel.onSummaryResultConsumed(); // Mówimy VM, że wynik został "skonsumowany"
            }
        });

        // Obserwator błędów
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                viewModel.clearErrorMessage();
            }
        });
    }

    // Jedna centralna metoda do zarządzania widocznością wszystkich elementów UI
    private void updateUiForState(MeasurementViewModel.UiState state) {
        if (state == null) return;

        // Przycisk "Wróć" jest widoczny tylko wtedy, gdy pomiar nie jest w toku
        binding.goBackButton.setVisibility(
                (state == MeasurementViewModel.UiState.IDLE || state == MeasurementViewModel.UiState.SUMMARY)
                        ? View.VISIBLE : View.GONE
        );

        switch (state) {
            case IDLE:
                binding.liveGroup.setVisibility(View.GONE);
                binding.countdownTextView.setVisibility(View.GONE);
                binding.measurementStatusTextView.setText("Gotowy do pomiaru");
                binding.actionButton.setText("Start Pomiaru");
                binding.actionButton.setVisibility(View.VISIBLE);
                break;
            case COUNTDOWN:
                binding.liveGroup.setVisibility(View.GONE);
                binding.countdownTextView.setVisibility(View.VISIBLE);
                binding.measurementStatusTextView.setText("Przygotuj się...");
                binding.actionButton.setText("Anuluj");
                binding.actionButton.setVisibility(View.VISIBLE);
                break;
            case MEASURING:
                binding.liveGroup.setVisibility(View.VISIBLE);
                binding.countdownTextView.setVisibility(View.GONE);
                binding.measurementStatusTextView.setText("Pomiar w trakcie...");
                binding.actionButton.setText("Zatrzymaj");
                binding.actionButton.setVisibility(View.VISIBLE);
                break;
            case SUMMARY:
                binding.liveGroup.setVisibility(View.GONE);
                binding.countdownTextView.setVisibility(View.GONE);
                binding.measurementStatusTextView.setText("Pomiar zakończony!");
                binding.actionButton.setVisibility(View.VISIBLE);
                binding.actionButton.setText("Nowy pomiar");
                binding.goBackButton.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}