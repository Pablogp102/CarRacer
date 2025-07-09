package com.carracer.presentation.ui.leaderboard;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.carracer.R;
import com.carracer.databinding.FragmentLeaderboardBinding;
import com.carracer.domain.utils.MeasurementType;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LeaderboardFragment extends Fragment {

    private FragmentLeaderboardBinding binding;
    private LeaderboardViewModel viewModel;
    private LeaderboardAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLeaderboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LeaderboardViewModel.class);

        setupRecyclerView();
        setupListeners();
        setupObservers();
    }

    private void setupRecyclerView() {
        adapter = new LeaderboardAdapter(measurementId -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Usuń pomiar")
                    .setMessage("Czy na pewno chcesz usunąć ten pomiar? Operacja jest nieodwracalna.")
                    .setPositiveButton("Usuń", (dialog, which) -> viewModel.deleteMeasurement(measurementId))
                    .setNegativeButton("Anuluj", null)
                    .show();
        });
        binding.leaderboardRecyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.toggleButtonGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.button_my_measurements) {
                    viewModel.setMode(LeaderboardViewModel.Mode.MY_MEASUREMENTS);
                } else if (checkedId == R.id.button_global_leaderboard) {
                    viewModel.setMode(LeaderboardViewModel.Mode.GLOBAL);
                }
            }
        });
        binding.toggleButtonGroup.check(R.id.button_my_measurements);

        binding.filterButton.setOnClickListener(v -> showFilterDialog());
    }

    private void setupObservers() {
        viewModel.getItems().observe(getViewLifecycleOwner(), items -> {
            adapter.submitList(items);
            binding.emptyListText.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.loadingProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showFilterDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheetView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_filter_leaderboard, null);
        LinearLayout container = sheetView.findViewById(R.id.filter_buttons_container);

        for (MeasurementType type : MeasurementType.values()) {
            Button button = new Button(requireContext());
            button.setText(type.getDisplayName());
            button.setOnClickListener(v -> {
                viewModel.setFilter(type.name());
                dialog.dismiss();
            });
            container.addView(button);
        }

        sheetView.findViewById(R.id.filter_all).setOnClickListener(v -> {
            viewModel.setFilter(null);
            dialog.dismiss();
        });

        dialog.setContentView(sheetView);
        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}