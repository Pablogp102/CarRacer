package com.carracer.infrastructure.network.storage;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenStorage {
    private static final String PREFS = "auth_prefs";
    private static final String KEY = "jwt_token";

    private final SharedPreferences prefs;

    public TokenStorage(Context ctx) {
        prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        prefs.edit().putString(KEY, token).apply();
    }

    public String getToken() {
        return prefs.getString(KEY, null);
    }

    public void clearToken() {
        prefs.edit().remove(KEY).apply();
    }
}
