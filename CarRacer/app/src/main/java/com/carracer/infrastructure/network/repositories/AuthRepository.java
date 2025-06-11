package com.carracer.infrastructure.network.repositories;

import android.util.Log;

import com.carracer.domain.models.User;
import com.carracer.domain.repositories.IAuthRepository;
import com.carracer.domain.utils.Callback;
import com.carracer.infrastructure.network.models.Dtos.UserDto;
import com.carracer.infrastructure.network.models.Requests.DeleteRequest;
import com.carracer.infrastructure.network.models.Responses.DeleteResponse;
import com.carracer.infrastructure.network.storage.TokenStorage;
import com.carracer.infrastructure.network.ApiService;
import com.carracer.infrastructure.network.models.Requests.AuthRequest;
import com.carracer.infrastructure.network.models.Responses.AuthResponse;

import org.json.JSONObject;
import javax.inject.Inject;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Response;

public class AuthRepository  implements IAuthRepository {
    private static final String TAG = "AuthRepository";
    private final ApiService apiService;
    private final TokenStorage tokenStorage;

    @Inject
    public AuthRepository(ApiService apiService, TokenStorage tokenStorage) {
        this.apiService = apiService;
        this.tokenStorage = tokenStorage;
    }

    @Override
    public void login(String login, String password, Callback<User> callback) {
        apiService.login(new AuthRequest(login, password)).enqueue(new retrofit2.Callback<AuthResponse>() {
            @Override
            public void onResponse(retrofit2.Call<AuthResponse> call, retrofit2.Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    if (authResponse.isSuccess()) {
                        String token = authResponse.getToken();
                        UserDto userDto = authResponse.getUser();

                        if(token != null) {
                            tokenStorage.saveToken(token);
                        } else {
                            Log.w(TAG, "Login successful but token is null. This might indicate an API issue.");
                        }

                        if (userDto != null) {
                            UUID userId = null;
                            try {
                                if(userDto.getId() != null) {
                                    userId = UUID.fromString(userDto.getId());
                                } else {
                                    callback.onError(new Exception("User ID is null after successful login."));
                                    return;
                                }
                            } catch (IllegalArgumentException e) {
                                Log.e(TAG, "Invalid User ID format from API during login.", e);
                                callback.onError(new Exception("Invalid User ID format received from API."));
                                return;
                            }

                            long createdAtMillis = userDto.getCreatedAt();
                            Log.d(TAG, "UserDto createdAt (long): " + createdAtMillis);


                            User domainUser = new User(userId, userDto.getLogin(), createdAtMillis);
                            callback.onSuccess(domainUser);
                        } else {
                            Log.w(TAG, "Login successful (isSuccess=true), but user data (UserDto) was null.");
                            callback.onError(new Exception("Login successful, but user data was missing."));
                        }
                    } else {
                        String apiErrorMessage = authResponse.getMessage() != null ? authResponse.getMessage() : "Login failed with unspecified error from API.";
                        Log.w(TAG, "Login failed (isSuccess=false): " + apiErrorMessage);
                        callback.onError(new Exception(apiErrorMessage));
                    }
                } else {
                    String errorMessage = "Login request failed";
                    int responseCode = response.code();
                    if (response.errorBody() != null) {
                        try {
                            String errorJson = response.errorBody().string();
                            Log.e(TAG, "Login error body: " + errorJson);
                            JSONObject errorObj = new JSONObject(errorJson);
                            errorMessage = errorObj.optString("message", errorMessage);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing error body for login.", e);
                        }
                    } else if (response.body() == null && response.isSuccessful()) {
                        errorMessage = "Login response body was null despite successful HTTP status.";
                        Log.e(TAG, errorMessage + " (Code: " + responseCode + ")");
                    }
                    callback.onError(new Exception(errorMessage + " (Code: " + responseCode + ")"));
                }
            }

            @Override
            public void onFailure(retrofit2.Call<AuthResponse> call, Throwable t) {
                Log.e(TAG, "Login network failure.", t);
                callback.onError(t);
            }
        });
    }

    @Override
    public void register(String login, String password, Callback<User> callback) {
        apiService.register(new AuthRequest(login, password)).enqueue(new retrofit2.Callback<AuthResponse>() {
            @Override
            public void onResponse(retrofit2.Call<AuthResponse> call, retrofit2.Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    if (authResponse.isSuccess()) {
                        String token = authResponse.getToken();
                        UserDto userDto = authResponse.getUser();

                        if (token != null) {
                            tokenStorage.saveToken(token);
                        } else {
                            Log.w(TAG, "Registration successful but token is null. This might indicate an API issue.");
                        }

                        if (userDto != null) {
                            UUID userId = null;
                            try {
                                if (userDto.getId() != null) {
                                    userId = UUID.fromString(userDto.getId());
                                } else {
                                    callback.onError(new Exception("User ID is null during registration."));
                                    return;
                                }
                            } catch (IllegalArgumentException e) {
                                Log.e(TAG, "Invalid User ID format from API during registration.", e);
                                callback.onError(new Exception("Invalid User ID format received from API during registration."));
                                return;
                            }

                            long createdAtMillis = userDto.getCreatedAt();
                            Log.d(TAG, "UserDto createdAt (long): " + createdAtMillis);

                            User domainUser = new User(userId, userDto.getLogin(), createdAtMillis);
                            callback.onSuccess(domainUser);
                        } else {
                            callback.onError(new Exception("Registration successful, but user data (UserDto) was null."));
                        }
                    } else {
                        String errorMessage = "Registration failed";
                        if (response.errorBody() != null) {
                            try {
                                String errorJson = response.errorBody().string();
                                JSONObject errorObj = new JSONObject(errorJson);
                                errorMessage = errorObj.optString("message", errorMessage);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing error body for registration.", e);
                            }
                        }
                        callback.onError(new Exception(errorMessage + " (Code: " + response.code() + ")"));
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<AuthResponse> call, Throwable t) {
                Log.e(TAG, "Registration network failure.", t);
                callback.onError(t);
            }
        });
    }

    @Override
    public boolean isLoggedIn() {
        return tokenStorage.getToken() != null;
    }

    public void logout() {
        tokenStorage.clearToken();
    }

    @Override
    public void deleteAccount(String id, Callback<Void> callback) {
        apiService.delete(new DeleteRequest(id)).enqueue(new retrofit2.Callback<DeleteResponse>() {
            @Override
            public void onResponse(Call<DeleteResponse> call, Response<DeleteResponse> response) {
                if (response.isSuccessful()) {
                    DeleteResponse deleteResponse = response.body();
                    if (deleteResponse != null && deleteResponse.isSuccess()) {
                        callback.onSuccess(null); // Sukces, przekazujemy null zgodnie z Callback<Void>
                    } else if (deleteResponse != null) {
                        // API zwróciło isSuccess=false, ale nie błąd HTTP, przekazujemy wiadomość z API jako błąd
                        Log.w(TAG, "Delete account failed from server: " + deleteResponse.getMessage());
                        callback.onError(new Exception(deleteResponse.getMessage() != null ? deleteResponse.getMessage() : "Usunięcie konta nie powiodło się."));
                    } else {
                        // Pusta odpowiedź (200 OK, ale bez ciała)
                        Log.w(TAG, "Delete account received empty body with successful HTTP status.");
                        callback.onError(new Exception("Pusta odpowiedź z serwera."));
                    }
                } else {
                    // Obsługa błędów HTTP (np. 400, 401, 404, 500)
                    String errorMessage = "Błąd HTTP podczas usuwania konta";
                    int responseCode = response.code();
                    if (response.errorBody() != null) {
                        try {
                            String errorJson = response.errorBody().string();
                            Log.e(TAG, "Delete error body: " + errorJson);
                            JSONObject errorObj = new JSONObject(errorJson);
                            errorMessage = errorObj.optString("message", errorMessage);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing error body for delete account.", e);
                        }
                    }
                    Log.e(TAG, "HTTP Error during delete account: " + errorMessage + " (Code: " + responseCode + ")");
                    callback.onError(new Exception(errorMessage + " (Kod: " + responseCode + ")"));
                }
            }

            @Override
            public void onFailure(Call<DeleteResponse> call, Throwable t) {
                String errorMessage = "Błąd sieci podczas usuwania konta.";
                if (t.getMessage() != null) {
                    errorMessage += ": " + t.getMessage();
                }
                Log.e(TAG, "Network failure during delete account: " + errorMessage, t);
                callback.onError(new Exception(errorMessage));
            }
        });
    }


}
