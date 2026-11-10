package airport.core;

import java.util.Optional;

/**
 * Generic application result wrapper for service-level operations.
 * Keeps success and error handling consistent without forcing every service to throw exceptions.
 */
public final class Result<T> {
    private final boolean success;
    private final T data;
    private final String message;
    private final Throwable error;

    private Result(boolean success, T data, String message, Throwable error) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.error = error;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(true, data, null, null);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(true, data, message, null);
    }

    public static <T> Result<T> failure(String message) {
        return new Result<>(false, null, message, null);
    }

    public static <T> Result<T> failure(String message, Throwable error) {
        return new Result<>(false, null, message, error);
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public Optional<Throwable> getError() {
        return Optional.ofNullable(error);
    }
}
