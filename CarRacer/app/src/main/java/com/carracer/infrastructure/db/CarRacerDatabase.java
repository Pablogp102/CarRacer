package com.carracer.infrastructure.db;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.carracer.infrastructure.db.dao.MeasurementDao;
import com.carracer.infrastructure.db.dao.UserDao;
import com.carracer.infrastructure.db.entities.MeasurementEntity;
import com.carracer.domain.utils.Converters;
import com.carracer.infrastructure.db.entities.UserEntity;

@Database(entities = {MeasurementEntity.class, UserEntity.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class CarRacerDatabase extends RoomDatabase{
    public abstract MeasurementDao measurementDao();
    public abstract UserDao userDao();
}
