package com.placement.placement_intelligence.exception;

/**
 * Exception thrown when authentication is required but not provided or invalid.
 * Results in HTTP 401 Unauthorized response.
 */
public class UnauthorizedException extends RuntimeException {
    
    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
