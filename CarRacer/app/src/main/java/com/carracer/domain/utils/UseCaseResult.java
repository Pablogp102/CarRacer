package com.carracer.domain.utils;

public class UseCaseResult<T> {
    private final T data;
    private final Throwable error;

    private UseCaseResult(T data, Throwable error) {
        this.data = data;
        this.error = error;
    }

    public static <T> UseCaseResult<T> success(T data) {
        return new UseCaseResult<>(data, null);
    }

    public static <T> UseCaseResult<T> error(Throwable error) {
        return new UseCaseResult<>(null, error);
    }

    public boolean isSuccess() { return error == null; }
    public T getData() { return data; }
    public Throwable getError() { return error; }
}
