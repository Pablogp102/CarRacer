package com.carracer.presentation.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment; // Import NavHostFragment

import com.carracer.R;
import com.carracer.domain.repositories.IAuthRepository; // Zakładam, że masz ten interfejs
import com.carracer.presentation.MainActivity;
import com.carracer.presentation.ui.auth.AuthActivity; // Import AuthActivity

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SplashFragment extends Fragment {

    @Inject // Wstrzyknij AuthRepository, aby sprawdzić stan logowania
    IAuthRepository authRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false); // Twój layout splash screen
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Sprawdź, czy użytkownik jest zalogowany
            boolean isLoggedIn = authRepository.isLoggedIn(); // Metoda isLoggedIn() musi być zaimplementowana

            if (isLoggedIn) {
                // Użytkownik jest zalogowany, przejdź do MainActivity
                Intent intent = new Intent(requireActivity(), MainActivity.class);
                startActivity(intent);
            } else {
                // Użytkownik nie jest zalogowany, nawiguj do LoginFragment w ramach AuthActivity
                NavHostFragment.findNavController(this).navigate(R.id.action_splashFragment_to_login_fragment);
            }
            // Ważne: NIE wywołuj tu finish() dla requireActivity() od razu,
            // jeśli nawigujesz do LoginFragment w tym samym Activity.
            // finish() wywołaj tylko wtedy, gdy uruchamiasz nowe Activity i chcesz zamknąć stare.
            // Jeśli isLoggedIn, AuthActivity zostanie zamknięte po uruchomieniu MainActivity.
            // Jeśli nie jest zalogowany, AuthActivity pozostaje otwarte z LoginFragment.
            if (isLoggedIn) {
                requireActivity().finish(); // Zamyka AuthActivity po przejściu do MainActivity
            }
        }, 2000); // 2 sekundy opóźnienia
    }
}
