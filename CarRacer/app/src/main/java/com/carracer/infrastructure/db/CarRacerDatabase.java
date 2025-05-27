package com.carracer.infrastructure.db;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.carracer.infrastructure.db.dao.MeasurementDao;
import com.carracer.infrastructure.db.entities.MeasurementEntity;
import com.carracer.domain.utils.Converters;

@Database(entities = {MeasurementEntity.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class CarRacerDatabase extends RoomDatabase{
    public abstract MeasurementDao measurementDao();
}
