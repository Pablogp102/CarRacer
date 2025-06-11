package com.carracer.presentation.ui.register;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment; // Import NavHostFragment

import com.carracer.R;
import com.carracer.domain.models.User;
import com.carracer.domain.utils.Callback;
import com.carracer.infrastructure.services.AuthService;
import com.carracer.presentation.MainActivity;
import com.carracer.presentation.ui.auth.AuthActivity;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegisterFragment extends Fragment {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button registerButton;
    private Button loginLinkButton; // Przycisk "Log in"
    private ProgressBar registerProgressBar;

    @Inject
    AuthService authService; // Wstrzyknij AuthService

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        usernameEditText = view.findViewById(R.id.usernameEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        registerButton = view.findViewById(R.id.registerButton);
        loginLinkButton = view.findViewById(R.id.loginLink);
        registerProgressBar = view.findViewById(R.id.registerProgressBar);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        registerButton.setOnClickListener(v -> {
            String login = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (login.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Proszę podać login i hasło.", Toast.LENGTH_SHORT).show();
                return;
            }

            registerProgressBar.setVisibility(View.VISIBLE);
            authService.register(login, password, new Callback<User>() {
                @Override
                public void onSuccess(User user) {
                    registerProgressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Rejestracja udana!", Toast.LENGTH_SHORT).show();
                    // Przejdź do MainActivity i zakończ AuthActivity
                    Intent intent = new Intent(requireActivity(), MainActivity.class);
                    startActivity(intent);
                    requireActivity().finish(); // Zamyka AuthActivity
                }

                @Override
                public void onError(Throwable t) {
                    registerProgressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Rejestracja nieudana: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });

        loginLinkButton.setOnClickListener(v -> {
            // Nawiguj z powrotem do LoginFragment za pomocą akcji Navigation Component.
            // popUpTo w auth_navigation.xml zapewni, że RegisterFragment zostanie usunięty ze stosu.
            NavHostFragment.findNavController(RegisterFragment.this)
                    .navigate(R.id.action_registerFragment_to_loginFragment);
        });
    }
}
