package com.placement.placement_intelligence.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Specialized error response for validation failures.
 * Provides field-level validation error details.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationErrorResponse {
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime timestamp;
    
    private int status;
    
    private String error;
    
    private String message;
    
    private String path;
    
    private Map<String, String> fieldErrors;

    public ValidationErrorResponse() {
        this.timestamp = LocalDateTime.now();
        this.fieldErrors = new HashMap<>();
    }

    public ValidationErrorResponse(int status, String error, String message, String path) {
        this();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    // Getters and Setters
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

    public void addFieldError(String field, String message) {
        if (this.fieldErrors == null) {
            this.fieldErrors = new HashMap<>();
        }
        this.fieldErrors.put(field, message);
    }
}
