package com.carracer.infrastructure.network;

import com.carracer.infrastructure.network.models.Requests.*;
import com.carracer.infrastructure.network.models.Responses.*;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("api/Auth/login")
    Call<AuthResponse> login(@Body AuthRequest request);

    @POST("api/Auth/register")
    Call<AuthResponse> register(@Body AuthRequest request);

    @POST("api/Auth/delete")
    Call<DeleteResponse> delete(@Body DeleteRequest request);

    @POST("api/measurement/sync")
    Call<SyncResponse> syncMeasurements(@Body SyncRequest request);

    @POST("api/measurement/leaderboard")
    Call<LeaderboardResponse> getLeaderboard(@Body LeaderboardRequest request);

    @POST("api/measurement/delete")
    Call<DeleteResponse> deleteMeasurement(@Body DeleteRequest request);
}
