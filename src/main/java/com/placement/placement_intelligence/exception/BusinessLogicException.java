package com.placement.placement_intelligence.exception;

/**
 * Exception thrown when business logic constraints are violated.
 * Results in HTTP 422 Unprocessable Entity response.
 * 
 * Use this for semantic errors where the request is well-formed but cannot be
 * processed due to business rules (e.g., insufficient balance, duplicate entry).
 */
public class BusinessLogicException extends RuntimeException {
    
    private final String errorCode;
    
    public BusinessLogicException(String message) {
        super(message);
        this.errorCode = null;
    }
    
    public BusinessLogicException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public BusinessLogicException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
    }
    
    public BusinessLogicException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
