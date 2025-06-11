package com.carracer.presentation.ui.login;

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
import androidx.navigation.fragment.NavHostFragment;

import com.carracer.R;
import com.carracer.domain.models.User;
import com.carracer.domain.utils.Callback;
import com.carracer.infrastructure.services.AuthService;
import com.carracer.presentation.MainActivity;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends Fragment {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button registerLinkButton;
    private ProgressBar loginProgressBar;

    @Inject
    AuthService authService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        usernameEditText = view.findViewById(R.id.usernameEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        loginButton = view.findViewById(R.id.loginButton);
        registerLinkButton = view.findViewById(R.id.registerLink);
        loginProgressBar = view.findViewById(R.id.loginProgressBar);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loginButton.setOnClickListener(v -> {
            String login = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (login.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Proszę podać login i hasło.", Toast.LENGTH_SHORT).show();
                return;
            }

            loginProgressBar.setVisibility(View.VISIBLE);
            authService.login(login, password, new Callback<User>() {
                @Override
                public void onSuccess(User user) {
                    loginProgressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Logowanie udane!", Toast.LENGTH_SHORT).show();
                    // Przejdź do MainActivity i zakończ AuthActivity
                    Intent intent = new Intent(requireActivity(), MainActivity.class);
                    startActivity(intent);
                    requireActivity().finish(); // Zamyka AuthActivity
                }

                @Override
                public void onError(Throwable t) {
                    loginProgressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Logowanie nieudane: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });

        registerLinkButton.setOnClickListener(v -> {
            // Nawiguj do RegisterFragment za pomocą akcji Navigation Component
            NavHostFragment.findNavController(LoginFragment.this)
                    .navigate(R.id.action_loginFragment_to_registerFragment);
        });
    }
}
