package com.carracer.infrastructure.network.models.Requests;

import com.carracer.infrastructure.network.models.Dtos.MeasurementDto;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SyncRequest {

    @SerializedName("measurements")
    private List<MeasurementDto> measurements;

    public SyncRequest(List<MeasurementDto> measurements) {
        this.measurements = measurements;
    }
}