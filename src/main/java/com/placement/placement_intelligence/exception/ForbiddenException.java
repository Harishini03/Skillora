package com.placement.placement_intelligence.exception;

/**
 * Exception thrown when a user is authenticated but not authorized to access a resource.
 * Results in HTTP 403 Forbidden response.
 * 
 * Use this when the user identity is known but they lack the necessary permissions.
 */
public class ForbiddenException extends RuntimeException {
    
    private final String resource;
    private final String action;
    
    public ForbiddenException(String message) {
        super(message);
        this.resource = null;
        this.action = null;
    }
    
    public ForbiddenException(String message, String resource, String action) {
        super(message);
        this.resource = resource;
        this.action = action;
    }
    
    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
        this.resource = null;
        this.action = null;
    }
    
    public String getResource() {
        return resource;
    }
    
    public String getAction() {
        return action;
    }
}
