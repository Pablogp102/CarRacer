package com.carracer.infrastructure.hilt;
import com.carracer.domain.repositories.IAuthRepository;
import com.carracer.domain.repositories.ILeaderboardRepository;
import com.carracer.domain.repositories.IUserRepository;
import com.carracer.domain.repositories.IMeasurementsRepository;
import com.carracer.infrastructure.network.repositories.AuthRepository;
import com.carracer.infrastructure.db.repositories.UserRepository;
import com.carracer.infrastructure.db.repositories.MeasurementsRepository;
import com.carracer.infrastructure.repositories.LeaderboardRepository;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;
@Module
@InstallIn(SingletonComponent.class)
public abstract class RepositoryModule {
    @Binds
    @Singleton
    public abstract IAuthRepository bindAuthRepository(AuthRepository impl);

    @Binds
    @Singleton
    public abstract IUserRepository bindUserRepository(UserRepository impl);

    @Binds
    @Singleton
    public abstract IMeasurementsRepository bindMeasurementsRepository(MeasurementsRepository impl);

    @Binds
    public abstract ILeaderboardRepository bindLeaderboardRepository(LeaderboardRepository leaderboardRepository);
}
