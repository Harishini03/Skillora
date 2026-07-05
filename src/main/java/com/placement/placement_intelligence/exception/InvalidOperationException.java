package com.placement.placement_intelligence.exception;

/**
 * Exception thrown when an operation cannot be performed due to invalid state or conditions.
 * Results in HTTP 400 Bad Request response.
 */
public class InvalidOperationException extends RuntimeException {
    
    public InvalidOperationException(String message) {
        super(message);
    }

    public InvalidOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
