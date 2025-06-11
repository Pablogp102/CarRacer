package com.carracer.presentation.ui.auth;

import android.os.Bundle;
import android.util.Log; // Dodany import dla Log

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment; // Dodany import dla NavHostFragment
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.carracer.R;
import com.carracer.infrastructure.services.AuthService;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AuthActivity extends AppCompatActivity {

    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    @Inject
    AuthService authService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);


        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_auth);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.splashFragment,
                    R.id.login_fragment,
                    R.id.register_fragment
            ).build();


            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        } else {
            Log.e("AuthActivity", "NavHostFragment z ID R.id.nav_host_fragment_auth nie został znaleziony!");
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
