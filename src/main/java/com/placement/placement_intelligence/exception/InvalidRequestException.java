package com.placement.placement_intelligence.exception;

/**
 * Exception thrown when a request is invalid or malformed.
 * Results in HTTP 400 Bad Request response.
 * 
 * Use this for syntactic errors or missing required parameters where the
 * request cannot be understood or processed.
 */
public class InvalidRequestException extends RuntimeException {
    
    private final String errorCode;
    
    public InvalidRequestException(String message) {
        super(message);
        this.errorCode = null;
    }
    
    public InvalidRequestException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
    }
    
    public InvalidRequestException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
