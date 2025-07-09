package com.carracer.presentation.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.carracer.R;
import com.carracer.databinding.FragmentLoginBinding; // ZMIANA: Używamy ViewBinding
import com.carracer.presentation.MainActivity;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends Fragment {

    private LoginViewModel viewModel;
    private FragmentLoginBinding binding; // ZMIANA: Używamy ViewBinding

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // ZMIANA: Inicjalizacja ViewBinding
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Krok 1: Pobieramy instancję naszego nowego ViewModelu
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        setupListeners();
        setupObservers();
    }

    private void setupListeners() {
        // Krok 2: Listener przycisku jest teraz bardzo prosty - tylko deleguje pracę
        binding.loginButton.setOnClickListener(v -> {
            String login = binding.usernameEditText.getText().toString();
            String password = binding.passwordEditText.getText().toString();
            viewModel.login(login, password);
        });

        binding.registerLink.setOnClickListener(v -> {
            NavHostFragment.findNavController(LoginFragment.this)
                    .navigate(R.id.action_loginFragment_to_registerFragment);
        });
    }

    private void setupObservers() {
        // Krok 3: Cała magia dzieje się w obserwatorach, które są bezpieczne wątkowo

        // Obserwator paska ładowania
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.loginProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.loginButton.setEnabled(!isLoading);
        });

        // Obserwator błędu - on teraz bezpiecznie pokazuje Toasta
        viewModel.getErrorEvent().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                viewModel.onErrorShown(); // "Konsumujemy" zdarzenie
            }
        });

        // Obserwator sukcesu - on teraz bezpiecznie nawiguje
        viewModel.getNavigateToMainEvent().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(getContext(), "Logowanie udane!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(requireActivity(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();
                viewModel.onNavigationComplete(); // "Konsumujemy" zdarzenie
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Czyszczenie dla ViewBinding
    }
}