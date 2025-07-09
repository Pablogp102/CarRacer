package com.carracer.presentation.ui.race.measurement;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.carracer.databinding.DialogMeasurementSummaryBinding;
import com.carracer.domain.models.Measurement;
import com.carracer.domain.utils.MeasurementType;
import java.io.Serializable;

public class MeasurementSummaryDialogFragment extends DialogFragment {

    private static final String ARG_MEASUREMENT = "measurement_arg";
    public static final String REQUEST_KEY_SUMMARY_ACTION = "summary_action_request";
    public static final String BUNDLE_KEY_ACTION = "action";
    public static final String ACTION_NEW_MEASUREMENT = "new_measurement";
    public static final String ACTION_GO_BACK = "go_back";

    private DialogMeasurementSummaryBinding binding;

    public static MeasurementSummaryDialogFragment newInstance(Measurement measurement) {
        MeasurementSummaryDialogFragment fragment = new MeasurementSummaryDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_MEASUREMENT, (Serializable) measurement);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogMeasurementSummaryBinding.inflate(LayoutInflater.from(getContext()));
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(binding.getRoot());

        if (getArguments() != null) {
            Measurement measurement = (Measurement) getArguments().getSerializable(ARG_MEASUREMENT);
            if (measurement != null) {
                String summaryText = String.format(
                        "Typ: %s\nCzas: %.2f s\nPrędkość max: %.1f km/h\nDystans: %.2f m",
                        formatMeasurementType(measurement.getType()),
                        measurement.getDurationS(),
                        measurement.getPeakSpeedKmh(),
                        measurement.getDistanceMeters()
                );
                binding.summaryDetailsTextView.setText(summaryText);
            }
        }

        binding.newMeasurementButton.setOnClickListener(v -> {
            Bundle result = new Bundle();
            result.putString(BUNDLE_KEY_ACTION, ACTION_NEW_MEASUREMENT);
            getParentFragmentManager().setFragmentResult(REQUEST_KEY_SUMMARY_ACTION, result);
            dismiss();
        });

        binding.goBackButton.setOnClickListener(v -> {
            Bundle result = new Bundle();
            result.putString(BUNDLE_KEY_ACTION, ACTION_GO_BACK);
            getParentFragmentManager().setFragmentResult(REQUEST_KEY_SUMMARY_ACTION, result);
            dismiss();
        });

        // Uniemożliwia zamknięcie dialogu przez kliknięcie obok
        setCancelable(false);
        return builder.create();
    }

    // Potrzebujesz, żeby Measurement implementował Serializable
    // public class Measurement implements Serializable { ... }

    // Oraz żeby MeasurementType miało metodę do formatowania nazwy
    private String formatMeasurementType(MeasurementType type) {
        if (type == null) return "Nieznany";
        switch (type) {
            case ZERO_TO_HUNDRED: return "0-100 km/h";
            case ONE_HUNDRED_TO_TWO_HUNDRED: return "100-200 km/h";
            case QUARTER_MILE: return "1/4 Mili";
            case TOP_SPEED: return "Prędkość Maksymalna";
            default: return type.name();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Zapobieganie wyciekom pamięci
    }
}