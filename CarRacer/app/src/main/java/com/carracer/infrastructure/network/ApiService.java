package com.carracer.infrastructure.network;

import com.carracer.infrastructure.network.models.Requests.AuthRequest;
import com.carracer.infrastructure.network.models.Requests.DeleteRequest;
import com.carracer.infrastructure.network.models.Responses.AuthResponse;
import com.carracer.infrastructure.network.models.Responses.DeleteResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/Auth/login")
    Call<AuthResponse> login(@Body AuthRequest request);

    @POST("api/Auth/register")
    Call<AuthResponse> register(@Body AuthRequest request);

    @POST("api/Auth/delete")
    Call<DeleteResponse> delete(@Body DeleteRequest request);

}
