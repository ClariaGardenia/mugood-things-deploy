package com.muGood.common.api;

public record Result<T>(String code, String message, T result) {
    public static <T> Result<T> ok(T result) {
        return new Result<>("000000", "success", result);
    }

    public static <T> Result<T> fail(String code, String message) {
        return new Result<>(code, message, null);
    }
}
