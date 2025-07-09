package com.carracer.domain.utils;

public enum MeasurementType {
    ZERO_TO_HUNDRED("0-100 km/h Przyspieszenie"),
    ONE_HUNDRED_TO_TWO_HUNDRED("100-200 km/h Przyspieszenie"),
    QUARTER_MILE("1/4 mili"),
    TOP_SPEED("Prędkość maksymalna(1min)");

    private final String displayName;

    MeasurementType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}