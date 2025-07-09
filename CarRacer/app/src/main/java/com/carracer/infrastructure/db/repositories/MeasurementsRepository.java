package com.carracer.infrastructure.db.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.room.Transaction; // Import Transaction

import com.carracer.domain.models.Measurement;
import com.carracer.domain.repositories.IMeasurementsRepository; // Import interfejsu domenowego
import com.carracer.domain.utils.Callback;
import com.carracer.domain.utils.Converters; // Import Converters
import com.carracer.domain.utils.MeasurementType;
import com.carracer.domain.utils.ModelMapper;
import com.carracer.infrastructure.db.CarRacerDatabase;
import com.carracer.infrastructure.db.dao.MeasurementDao;
import com.carracer.infrastructure.db.entities.MeasurementEntity;
import com.carracer.infrastructure.network.ApiService; // Import ApiService
import com.carracer.infrastructure.network.models.Dtos.MeasurementDto; // Import MeasurementDto
import com.carracer.infrastructure.network.models.Requests.DeleteRequest;
import com.carracer.infrastructure.network.models.Requests.SyncRequest;
import com.carracer.infrastructure.network.models.Responses.DeleteResponse;
import com.carracer.infrastructure.network.models.Responses.SyncResponse;

import org.json.JSONObject; // Import JSONObject for error parsing
import android.util.Log; // Import Log

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors; // Potrzebne do streamów (Java 8+)

import javax.inject.Inject;
import retrofit2.Call;
import retrofit2.Response;

public class MeasurementsRepository implements IMeasurementsRepository {

    private static final String TAG = "MeasurementsRepository";
    private final MeasurementDao measurementDao;
    private final CarRacerDatabase appDatabase;
    private final ExecutorService databaseExecutor; // Do operacji na bazie danych
    private final ApiService apiService; // Do synchronizacji z API

    @Inject
    public MeasurementsRepository(MeasurementDao measurementDao, CarRacerDatabase appDatabase, ExecutorService databaseExecutor, ApiService apiService) {
        this.measurementDao = measurementDao;
        this.appDatabase = appDatabase;
        this.databaseExecutor = databaseExecutor;
        this.apiService = apiService;
    }

    @Override
    public void saveMeasurement(Measurement measurement, String userId, Callback<Void> callback) {
        databaseExecutor.execute(() -> {
            try {
                // Konwertuj model domenowy na encję Room
                MeasurementEntity entity = ModelMapper.fromDomainModel(measurement);
                if (entity != null) {
                    entity.userId = userId;
                    entity.isSynced = false; // Nowy pomiar domyślnie nie jest zsynchronizowany
                    measurementDao.insertMeasurement(entity);
                    Log.d(TAG, "Pojedynczy pomiar zapisany lokalnie: " + measurement.getType());
                    callback.onSuccess(null);
                } else {
                    callback.onError(new Exception("Błąd konwersji modelu Measurement na encję (pojedynczy zapis)."));
                }
            } catch (Exception e) {
                Log.e(TAG, "Błąd zapisu pojedynczego pomiaru do bazy danych: " + e.getMessage(), e);
                callback.onError(e);
            }
        });
    }

    @Override
    public void insertAll(List<MeasurementEntity> entities, Callback<Void> callback) {
        databaseExecutor.execute(() -> {
            try {
                measurementDao.insertAll(entities);
                Log.d(TAG, "Zapisano " + entities.size() + " pomiarów (z API) do bazy Room.");
                callback.onSuccess(null);
            } catch (Exception e) {
                Log.e(TAG, "Błąd grupowego zapisu pomiarów do bazy Room.", e);
                callback.onError(e);
            }
        });
    }

    @Override
    public LiveData<List<Measurement>> getAllMeasurements(String userId) {
        // Obserwuj LiveData z DAO i mapuj encje na modele domenowe
        return Transformations.map(measurementDao.getMeasurementsByUserId(userId), entities -> {
            if (entities == null) {
                return new ArrayList<>(); // Zwróć pustą listę zamiast null
            }
            return entities.stream()
                    .map(ModelMapper::toDomainModel)
                    .filter(java.util.Objects::nonNull) // Upewnij się, że nie mapujesz nulli
                    .collect(Collectors.toList());
        });
    }

    @Override
    public LiveData<List<Measurement>> getMeasurementsByType(String userId, MeasurementType type) {
        // Obserwuj LiveData z DAO i mapuj encje na modele domenowe
        return Transformations.map(measurementDao.getMeasurementsByUserIdAndType(userId, type.name()), entities -> {
            if (entities == null) {
                return new ArrayList<>(); // Zwróć pustą listę zamiast null
            }
            return entities.stream()
                    .map(ModelMapper::toDomainModel)
                    .filter(java.util.Objects::nonNull) // Upewnij się, że nie mapujesz nulli
                    .collect(Collectors.toList());
        });
    }

    @Override
    public void deleteMeasurement(String measurementId, Callback<Void> callback) {
        databaseExecutor.execute(() -> {
            // Najpierw usuwamy lokalnie
            measurementDao.deleteMeasurementById(measurementId);
            Log.d(TAG, "Usunięto pomiar lokalnie: " + measurementId);

            // Następnie wysyłamy żądanie do API
            apiService.deleteMeasurement(new DeleteRequest(measurementId)).enqueue(new retrofit2.Callback<DeleteResponse>() {
                @Override
                public void onResponse(Call<DeleteResponse> call, Response<DeleteResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Log.d(TAG, "Pomiar pomyślnie usunięty z serwera.");
                        callback.onSuccess(null);
                    } else {
                        Log.e(TAG, "Błąd usuwania pomiaru na serwerze.");
                        callback.onError(new Exception("Błąd serwera przy usuwaniu pomiaru."));
                    }
                }
                @Override
                public void onFailure(Call<DeleteResponse> call, Throwable t) {
                    Log.e(TAG, "Błąd sieciowy przy usuwaniu pomiaru.", t);
                    callback.onError(t);
                }
            });
        });
    }

    @Override
    public void deleteAllMeasurements(String userId, Callback<Void> callback) {
        databaseExecutor.execute(() -> {
            try {
                appDatabase.runInTransaction(() -> {
                    Log.d(TAG, "DAO: Próba usunięcia wszystkich pomiarów dla usera o ID: " + userId);
                    measurementDao.deleteMeasurementsByUserId(userId);
                });
                Log.d(TAG, "Transakcja usunięcia pomiarów zakończona pomyślnie.");
                callback.onSuccess(null);
            } catch (Exception e) {
                Log.e(TAG, "Błąd usuwania wszystkich pomiarów: " + e.getMessage(), e);
                callback.onError(e);
            }
        });
    }

    @Override
    public void getUnsyncedMeasurements(String userId, Callback<List<Measurement>> callback) {
        databaseExecutor.execute(() -> {
            try {
                List<MeasurementEntity> unsyncedEntities = measurementDao.getUnsyncedMeasurements(userId);
                List<Measurement> unsyncedMeasurements = unsyncedEntities.stream()
                        .map(ModelMapper::toDomainModel)
                        .filter(java.util.Objects::nonNull)
                        .collect(Collectors.toList());
                Log.d(TAG, "Pobrano " + unsyncedMeasurements.size() + " niesynchronizowanych pomiarów dla użytkownika: " + userId);
                callback.onSuccess(unsyncedMeasurements);
            } catch (Exception e) {
                Log.e(TAG, "Błąd pobierania niesynchronizowanych pomiarów: " + e.getMessage(), e);
                callback.onError(e);
            }
        });
    }

    @Override
    @Transaction // Zapewnia atomowość operacji w bazie Room
    public void markMeasurementsAsSynced(List<String> measurementIds, Callback<Void> callback) {
        if (measurementIds == null || measurementIds.isEmpty()) {
            Log.d(TAG, "Brak ID pomiarów do oznaczenia jako zsynchronizowane. Pomięto.");
            callback.onSuccess(null);
            return;
        }
        databaseExecutor.execute(() -> {
            try {
                measurementDao.markMeasurementsAsSynced(measurementIds);
                Log.d(TAG, "Oznaczono " + measurementIds.size() + " pomiarów jako zsynchronizowane.");
                callback.onSuccess(null);
            } catch (Exception e) {
                Log.e(TAG, "Błąd oznaczania pomiarów jako zsynchronizowanych: " + e.getMessage(), e);
                callback.onError(e);
            }
        });
    }


    @Override
    public void syncMeasurementsToCloud(String userId, Callback<Void> callback) {
        Log.d(TAG, "Rozpoczynam synchronizację dla usera: " + userId);

        databaseExecutor.execute(() -> {
            try {
                // 1. Pobierz niesynchronizowane pomiary z lokalnej bazy
                List<MeasurementEntity> unsyncedEntities = measurementDao.getUnsyncedMeasurements(userId);

                if (unsyncedEntities.isEmpty()) {
                    Log.d(TAG, "Brak pomiarów do synchronizacji.");
                    callback.onSuccess(null);
                    return;
                }

                // 2. Ręcznie zmapuj Encje na DTOs
                List<MeasurementDto> dtosToSync = unsyncedEntities.stream()
                        .map(entity -> {
                            MeasurementDto dto = new MeasurementDto();
                            dto.setId(entity.id);
                            dto.setType(entity.type);
                            dto.setDurationS(entity.durationS);
                            dto.setPeakSpeedKmh(entity.peakSpeedKmh);
                            dto.setDistanceMeters(entity.distanceMeters);
                            dto.setMeasuredAt(entity.timestamp);
                            return dto;
                        })
                        .collect(Collectors.toList());

                // 3. Stwórz obiekt Request (bez userId!) i wywołaj API
                SyncRequest request = new SyncRequest(dtosToSync);
                apiService.syncMeasurements(request).enqueue(new retrofit2.Callback<SyncResponse>() {
                    @Override
                    public void onResponse(Call<SyncResponse> call, Response<SyncResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {

                            // 4. SUKCES! Pobierz PRAWDZIWĄ listę ID z odpowiedzi API
                            List<String> syncedIds = response.body().getSyncedMeasurementIds();

                            if (syncedIds != null && !syncedIds.isEmpty()) {
                                // 5. Oznacz w bazie jako zsynchronizowane TYLKO te pomiary, które potwierdziło API
                                markMeasurementsAsSynced(syncedIds, new Callback<Void>() {
                                    @Override
                                    public void onSuccess(Void result) {
                                        Log.d(TAG, "Synchronizacja udana. Oznaczono " + syncedIds.size() + " pomiarów.");
                                        callback.onSuccess(null);
                                    }
                                    @Override
                                    public void onError(Throwable t) {
                                        Log.e(TAG, "Błąd oznaczania pomiarów jako zsynchronizowanych.", t);
                                        callback.onError(t);
                                    }
                                });
                            } else {
                                Log.d(TAG, "Synchronizacja udana, ale API nie zwróciło żadnych ID do oznaczenia (prawdopodobnie wszystkie były duplikatami).");
                                callback.onSuccess(null);
                            }
                        } else {
                            // 6. Obsługa błędów, tak jak w AuthRepository
                            String errorMessage = "Błąd synchronizacji";
                            if(response.body() != null && response.body().getMessage() != null) {
                                errorMessage = response.body().getMessage();
                            } else if (response.errorBody() != null) {
                                try {
                                    errorMessage = response.errorBody().string();
                                } catch (Exception e) { /* ignorujemy */ }
                            }
                            Log.e(TAG, "Błąd synchronizacji z API: " + errorMessage + " (Code: " + response.code() + ")");
                            callback.onError(new Exception(errorMessage));
                        }
                    }

                    @Override
                    public void onFailure(Call<SyncResponse> call, Throwable t) {
                        Log.e(TAG, "Błąd sieci podczas synchronizacji.", t);
                        callback.onError(t);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Błąd wewnętrzny podczas przygotowywania synchronizacji.", e);
                callback.onError(e);
            }
        });
    }
}
