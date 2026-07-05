package com.placement.placement_intelligence.exception;

import com.placement.placement_intelligence.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for all REST API controllers.
 * Provides centralized exception handling with consistent error responses,
 * proper HTTP status codes, and structured logging.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle validation errors (400 Bad Request)
     * Triggered by @Valid annotation on request bodies
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "Request validation failed. Please check the field errors.",
                request.getRequestURI(),
                details
        );
        
        logger.warn("Validation error on {}: {}", request.getRequestURI(), details);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle invalid request exceptions (400 Bad Request)
     */
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequestException(
            InvalidRequestException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid Request",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        if (ex.getErrorCode() != null) {
            errorResponse.addDetail("Error Code: " + ex.getErrorCode());
        }
        
        logger.warn("Invalid request on {}: {}", request.getRequestURI(), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle illegal argument exceptions (400 Bad Request)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        logger.warn("Illegal argument on {}: {}", request.getRequestURI(), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle invalid operation exceptions (400 Bad Request)
     */
    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOperationException(
            InvalidOperationException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid Operation",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        logger.warn("Invalid operation on {}: {}", request.getRequestURI(), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle business rule violation exceptions (400 Bad Request)
     */
    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRuleViolationException(
            BusinessRuleViolationException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Business Rule Violation",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        if (ex.getRuleCode() != null) {
            errorResponse.addDetail("Rule Code: " + ex.getRuleCode());
        }
        
        logger.warn("Business rule violation on {}: {}", request.getRequestURI(), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle missing request parameters (400 Bad Request)
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Missing Parameter",
                "Required parameter '" + ex.getParameterName() + "' is missing",
                request.getRequestURI()
        );
        
        errorResponse.addDetail("Parameter: " + ex.getParameterName());
        errorResponse.addDetail("Type: " + ex.getParameterType());
        
        logger.warn("Missing parameter on {}: {}", request.getRequestURI(), ex.getParameterName());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle type mismatch exceptions (400 Bad Request)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        
        String expectedType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Type Mismatch",
                String.format("Parameter '%s' should be of type %s", ex.getName(), expectedType),
                request.getRequestURI()
        );
        
        errorResponse.addDetail("Parameter: " + ex.getName());
        errorResponse.addDetail("Provided value: " + ex.getValue());
        errorResponse.addDetail("Expected type: " + expectedType);
        
        logger.warn("Type mismatch on {}: {} should be {}", request.getRequestURI(), ex.getName(), expectedType);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle malformed JSON (400 Bad Request)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Malformed Request",
                "Request body is malformed or unreadable. Please check JSON syntax.",
                request.getRequestURI()
        );
        
        logger.warn("Malformed request body on {}: {}", request.getRequestURI(), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle authentication exceptions (401 Unauthorized)
     */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Authentication failed. Please provide valid credentials.",
                request.getRequestURI()
        );
        
        logger.warn("Authentication failed on {}: {}", request.getRequestURI(), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handle custom unauthorized exceptions (401 Unauthorized)
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(
            UnauthorizedException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        logger.warn("Unauthorized access on {}: {}", request.getRequestURI(), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handle access denied exceptions (403 Forbidden)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                "You do not have permission to access this resource.",
                request.getRequestURI()
        );
        
        logger.warn("Access denied on {}: {}", request.getRequestURI(), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Handle custom forbidden exceptions (403 Forbidden)
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(
            ForbiddenException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        if (ex.getResource() != null) {
            errorResponse.addDetail("Resource: " + ex.getResource());
        }
        if (ex.getAction() != null) {
            errorResponse.addDetail("Action: " + ex.getAction());
        }
        
        logger.warn("Forbidden access on {}: {}", request.getRequestURI(), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Handle resource not found exceptions (404 Not Found)
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        if (ex.getResourceName() != null) {
            errorResponse.addDetail("Resource: " + ex.getResourceName());
        }
        if (ex.getFieldName() != null) {
            errorResponse.addDetail("Field: " + ex.getFieldName());
        }
        if (ex.getFieldValue() != null) {
            errorResponse.addDetail("Value: " + ex.getFieldValue());
        }
        
        logger.warn("Resource not found on {}: {}", request.getRequestURI(), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handle no handler found (404 Not Found)
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(
            NoHandlerFoundException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                "The requested endpoint does not exist.",
                request.getRequestURI()
        );
        
        errorResponse.addDetail("HTTP Method: " + ex.getHttpMethod());
        
        logger.warn("No handler found for {} {}", ex.getHttpMethod(), request.getRequestURI());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handle business logic exceptions (422 Unprocessable Entity)
     */
    @ExceptionHandler(BusinessLogicException.class)
    public ResponseEntity<ErrorResponse> handleBusinessLogicException(
            BusinessLogicException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Business Logic Error",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        if (ex.getErrorCode() != null) {
            errorResponse.addDetail("Error Code: " + ex.getErrorCode());
        }
        
        logger.warn("Business logic error on {}: {}", request.getRequestURI(), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
    }

    /**
     * Handle all other unhandled exceptions (500 Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI()
        );
        
        // Log full stack trace for debugging but don't expose it to client
        logger.error("Unexpected error on {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
