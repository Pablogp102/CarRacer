package com.carracer.infrastructure.network.models.Responses;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SyncResponse {

    @SerializedName("isSuccess")
    private boolean isSuccess;

    @SerializedName("message")
    private String message;

    @SerializedName("syncedMeasurementIds")
    private List<String> syncedMeasurementIds;

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getSyncedMeasurementIds() {
        return syncedMeasurementIds;
    }
}