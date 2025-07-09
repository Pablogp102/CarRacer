package com.carracer.infrastructure.hilt;

import android.content.Context;

import com.carracer.application.gps.IGPSManager;
import com.carracer.infrastructure.gps.GPSManager;
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
    public IGPSManager provideGPSManager(@ApplicationContext Context context) {
        return new GPSManager(context);
    }
}
