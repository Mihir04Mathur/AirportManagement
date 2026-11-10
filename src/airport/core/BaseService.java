package airport.core;

import airport.util.AppLogger;

/**
 * Shared base class for service-tier logic.
 * Provides a common logging style and service-level result helpers.
 */
public abstract class BaseService {
    protected void logInfo(String message) {
        AppLogger.info(message);
    }

    protected void logWarn(String message) {
        AppLogger.warn(message);
    }

    protected void logDebug(String message) {
        AppLogger.debug(message);
    }

    protected void logError(String message, Throwable error) {
        AppLogger.error(message, error);
    }

    protected <T> Result<T> success(T data) {
        return Result.success(data);
    }

    protected <T> Result<T> success(String message, T data) {
        return Result.success(message, data);
    }

    protected <T> Result<T> failure(String message) {
        return Result.failure(message);
    }

    protected <T> Result<T> failure(String message, Throwable error) {
        return Result.failure(message, error);
    }
}
