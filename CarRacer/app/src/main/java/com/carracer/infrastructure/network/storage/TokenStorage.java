package com.carracer.infrastructure.network.storage;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenStorage {
    // Zmieniono nazwę zmiennej, aby była bardziej opisowa i spójna
    private static final String PREFS_NAME = "AuthPrefs";
    // Zmieniono nazwę zmiennej, aby była bardziej opisowa i spójna
    private static final String KEY_TOKEN = "jwt_token";
    // Nowy klucz dla ID użytkownika
    private static final String KEY_USER_ID = "user_id";

    // Zmieniono nazwę zmiennej, aby była bardziej opisowa i spójna
    private final SharedPreferences sharedPreferences;

    public TokenStorage(Context context) { // Zmieniono nazwę parametru z ctx na context
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        sharedPreferences.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    // Nowa metoda do zapisu ID użytkownika
    public void saveUserId(String userId) { // userId powinien być stringiem UUID
        sharedPreferences.edit().putString(KEY_USER_ID, userId).apply();
    }

    // Nowa metoda do odczytu ID użytkownika
    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    // Zaktualizowana metoda do czyszczenia tokena i ID użytkownika
    public void clearToken() { // Zmieniono nazwę na clearAuthData, aby odzwierciedlić usunięcie ID
        sharedPreferences.edit().remove(KEY_TOKEN).remove(KEY_USER_ID).apply(); // Usuń też ID przy wylogowaniu
    }

    // Możesz również dodać osobną metodę clearUserId(), jeśli potrzebujesz tylko wyczyścić ID
    public void clearUserId() {
        sharedPreferences.edit().remove(KEY_USER_ID).apply();
    }
}
