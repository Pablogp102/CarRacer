// src/main/java/com/carracer/presentation/ui/race/measurement/MeasurementTypeBottomSheetFragment.java
package com.carracer.presentation.ui.race.measurement;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.ViewModelProvider;

import com.carracer.R;
import com.carracer.domain.utils.MeasurementType;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.Serializable; // Import Serializable

/**
 * BottomSheetDialogFragment do wyboru typu pomiaru.
 * Wyświetla listę dostępnych typów pomiarów z enum MeasurementType.
 * Po wybraniu, wysyła wynik z powrotem do fragmentu wywołującego (RaceFragment).
 */
public class MeasurementTypeBottomSheetFragment extends BottomSheetDialogFragment {

    public static final String TAG = "MeasurementTypeBottomSheet";
    public static final String REQUEST_KEY = "measurement_type_request_key";
    public static final String BUNDLE_KEY_MEASUREMENT_TYPE = "selected_measurement_type";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_measurement_type_bootom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayout buttonsLayout = view.findViewById(R.id.measurement_types_layout);

        // Dynamiczne tworzenie przycisków dla każdego typu pomiaru
        for (MeasurementType type : MeasurementType.values()) {
            Button button = new Button(requireContext());
            button.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            button.setText(type.getDisplayName());
            button.setPadding(32, 32, 32, 32);
            button.setTextSize(18f);
            button.setAllCaps(false);
            button.setBackgroundResource(R.drawable.rounded_button_background); // Użyj nowego drawable
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) button.getLayoutParams();
            params.setMargins(0, 0, 0, 16); // Margines na dole każdego przycisku
            button.setLayoutParams(params);

            // Ustawienie słuchacza kliknięć dla każdego przycisku
            button.setOnClickListener(v -> {
                // Utworzenie Bundle z wybranym typem pomiaru
                Bundle result = new Bundle();
                result.putSerializable(BUNDLE_KEY_MEASUREMENT_TYPE, type); // Typ pomiaru jest Serializable
                // Wysyłanie wyniku z powrotem do fragmentu wywołującego
                getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
                dismiss(); // Zamknięcie BottomSheet po wyborze
            });
            buttonsLayout.addView(button);
        }

        // Ustawienie słuchacza dla przycisku "Anuluj"
        view.findViewById(R.id.cancel_button).setOnClickListener(v -> {
            dismiss(); // Po prostu zamknij BottomSheet
        });
    }
}
