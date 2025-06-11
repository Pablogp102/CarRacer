package com.carracer.domain.utils;

public interface DataCallback<T> {
    void onSuccess(T data);
    void onError(Throwable t);
}
