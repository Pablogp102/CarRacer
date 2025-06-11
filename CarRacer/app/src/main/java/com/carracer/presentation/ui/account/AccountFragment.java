package com.carracer.presentation.ui.account;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.carracer.R;
import com.carracer.domain.utils.Callback;
import com.carracer.presentation.ui.auth.AuthActivity; // Import AuthActivity

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AccountFragment extends Fragment {

    private static final String TAG = "AccountFragment";
    private AccountViewModel viewModel;
    private TextView usernameTextView;
    private TextView createdAtTextView;
    private Button logoutButton;
    private Button deleteAccountButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        usernameTextView = view.findViewById(R.id.text_login);
        createdAtTextView = view.findViewById(R.id.text_since);
        logoutButton = view.findViewById(R.id.button_logout);
        deleteAccountButton = view.findViewById(R.id.button_delete_account);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AccountViewModel.class);

        viewModel.getUsername().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                usernameTextView.setText("Zalogowany jako: " + s);
            }
        });

        viewModel.getCreatedAt().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                createdAtTextView.setText("Jesteś Racerem od: " + s);
            }
        });

        logoutButton.setOnClickListener(v -> {
            viewModel.logout();
            Toast.makeText(requireContext(), "Wylogowano pomyślnie!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(requireActivity(), AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        deleteAccountButton.setOnClickListener(v -> {
            // Pokaż dialog potwierdzenia
            new AlertDialog.Builder(requireContext())
                    .setTitle("Usuń konto")
                    .setMessage("Czy na pewno chcesz usunąć swoje konto? Tej operacji nie można cofnąć.")
                    .setPositiveButton("Usuń", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Użytkownik potwierdził, wywołaj usuwanie konta
                            Toast.makeText(requireContext(), "Usuwanie konta...", Toast.LENGTH_SHORT).show(); // Informacja o rozpoczęciu operacji
                            viewModel.deleteAccount(new Callback<String>() {
                                @Override
                                public void onSuccess(String message) {
                                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(requireActivity(), AuthActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    requireActivity().finish();
                                }

                                @Override
                                public void onError(Throwable t) {
                                    Toast.makeText(requireContext(), "Błąd: " + t.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    })
                    .setNegativeButton("Anuluj", null)
                    .show();
        });
    }
}
