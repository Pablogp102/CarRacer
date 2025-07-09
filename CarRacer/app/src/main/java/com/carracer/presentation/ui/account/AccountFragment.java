package com.carracer.presentation.ui.account;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.carracer.databinding.FragmentAccountBinding; // ZMIANA: Import dla ViewBinding
import com.carracer.domain.utils.Callback;
import com.carracer.presentation.ui.auth.AuthActivity;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AccountFragment extends Fragment {

    private static final String TAG = "AccountFragment";
    private AccountViewModel viewModel;
    private FragmentAccountBinding binding; // ZMIANA: Używamy ViewBinding

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // ZMIANA: Inicjalizacja ViewBinding
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AccountViewModel.class);

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        viewModel.getUsername().observe(getViewLifecycleOwner(), username ->
                binding.textLogin.setText("Zalogowany jako: " + username));

        viewModel.getCreatedAt().observe(getViewLifecycleOwner(), date ->
                binding.textSince.setText("Jesteś Racerem od: " + date));

        // NOWY OBSERWATOR: Czeka na sygnał, żeby pokazać dialog o synchronizacji
        viewModel.getShowSyncDialogEvent().observe(getViewLifecycleOwner(), count -> {
            if (count != null && count > 0) {
                showSyncConfirmationDialog(count);
                viewModel.onShowSyncDialogConsumed();
            }
        });

        // NOWY OBSERWATOR: Czeka na sygnał, żeby ostatecznie wylogować i przejść do AuthActivity
        viewModel.getNavigateToLoginEvent().observe(getViewLifecycleOwner(), navigate -> {
            if (navigate != null && navigate) {
                navigateToAuthScreen();
                viewModel.onNavigateToLoginConsumed();
            }
        });

        // Obserwator dla błędów
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(getContext(), "Błąd: " + message, Toast.LENGTH_LONG).show();
                viewModel.clearErrorMessage();
            }
        });
    }

    private void setupListeners() {
        // ZMIANA: Przycisk wylogowania teraz tylko rozpoczyna proces
        binding.buttonLogout.setOnClickListener(v -> {
            viewModel.initiateLogout();
        });

        binding.buttonDeleteAccount.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Usuń konto")
                    .setMessage("Czy na pewno chcesz usunąć swoje konto? Tej operacji nie można cofnąć.")
                    .setPositiveButton("Usuń", (dialog, which) -> {
                        Toast.makeText(requireContext(), "Usuwanie konta...", Toast.LENGTH_SHORT).show();
                        viewModel.deleteAccount(new Callback<String>() {
                            @Override
                            public void onSuccess(String message) {
                                // Nawigacja jest już obsługiwana przez _navigateToLoginEvent,
                                // bo deleteAccount woła logout, który go triggeruje.
                                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                            }
                            @Override
                            public void onError(Throwable t) {
                                Toast.makeText(requireContext(), "Błąd usuwania konta: " + t.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    })
                    .setNegativeButton("Anuluj", null)
                    .show();
        });
    }

    // NOWA METODA: Buduje i pokazuje dialog z pytaniem o synchronizację
    private void showSyncConfirmationDialog(int unsyncedCount) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Niezapisane Pomiary")
                .setMessage("Masz " + unsyncedCount + " niezsynchronizowanych pomiarów. Co chcesz zrobić?")
                .setPositiveButton("Synchronizuj i Wyloguj", (dialog, which) -> {
                    Toast.makeText(getContext(), "Synchronizowanie...", Toast.LENGTH_SHORT).show();
                    viewModel.finalizeLogout(true); // Tak, synchronizuj
                })
                .setNegativeButton("Porzuć i Wyloguj", (dialog, which) -> {
                    viewModel.finalizeLogout(false); // Nie, nie synchronizuj
                })
                .setNeutralButton("Anuluj", null) // Anuluj - nic nie rób
                .show();
    }

    // NOWA METODA: Centralne miejsce do nawigacji, żeby uniknąć powtórzeń
    private void navigateToAuthScreen() {
        if (getContext() == null) return;
        Toast.makeText(requireContext(), "Wylogowano pomyślnie!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(requireActivity(), AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Czyszczenie dla ViewBinding
    }
}