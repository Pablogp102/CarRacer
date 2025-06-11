package com.carracer.domain.utils;

public interface Callback<T> {
    void onSuccess(T result);
    void onError(Throwable t);
}


