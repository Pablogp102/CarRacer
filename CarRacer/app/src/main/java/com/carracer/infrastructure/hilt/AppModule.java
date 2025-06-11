package com.carracer.infrastructure.hilt;

import android.content.Context;

import com.carracer.infrastructure.network.ApiService;
import com.carracer.infrastructure.network.RetrofitClient;
import com.carracer.infrastructure.network.storage.TokenStorage;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
@Module
@InstallIn(SingletonComponent.class)
public class AppModule {
    @Provides
    @Singleton
    public TokenStorage provideTokenStorage(@ApplicationContext Context context) {
        return new TokenStorage(context);
    }
    @Provides
    @Singleton
    public ApiService provideApiService(TokenStorage tokenStorage) {
        return RetrofitClient.getApiService(tokenStorage);
    }
}
