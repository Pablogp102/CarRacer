package com.carracer.infrastructure.hilt;

import com.carracer.application.services.IAuthService;
import com.carracer.application.services.IMeasurementService;
import com.carracer.application.services.IUserService;
import com.carracer.infrastructure.services.AuthService;
import com.carracer.infrastructure.services.MeasurementService;
import com.carracer.infrastructure.services.UserService;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public abstract class ServiceModule {

    @Binds
    @Singleton
    public abstract IUserService bindUserService(UserService impl);

    @Binds
    @Singleton
    public abstract IMeasurementService bindMeasurementService(MeasurementService impl);

    @Binds
    @Singleton
    public abstract IAuthService bindAuthService(AuthService impl);
}