package com.placement.placement_intelligence.exception;

import com.placement.placement_intelligence.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for GlobalExceptionHandler.
 * Verifies proper exception handling, status codes, and response format.
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private HttpServletRequest request;

    @Mock
    private BindingResult bindingResult;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        exceptionHandler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    void testHandleValidationException() {
        // Arrange
        FieldError fieldError1 = new FieldError("user", "email", "must not be blank");
        FieldError fieldError2 = new FieldError("user", "age", "must be greater than 0");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));
        
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(exception, request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Validation Failed", response.getBody().getError());
        assertEquals(2, response.getBody().getDetails().size());
    }

    @Test
    void testHandleInvalidRequestException() {
        // Arrange
        InvalidRequestException exception = new InvalidRequestException("Invalid input format", "ERR_001");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidRequestException(exception, request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Invalid Request", response.getBody().getError());
        assertEquals("Invalid input format", response.getBody().getMessage());
        assertTrue(response.getBody().getDetails().stream()
                .anyMatch(d -> d.contains("ERR_001")));
    }

    @Test
    void testHandleIllegalArgumentException() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument provided");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(exception, request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
    }

    @Test
    void testHandleUnauthorizedException() {
        // Arrange
        UnauthorizedException exception = new UnauthorizedException("Invalid credentials");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleUnauthorizedException(exception, request);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Unauthorized", response.getBody().getError());
        assertEquals("Invalid credentials", response.getBody().getMessage());
    }

    @Test
    void testHandleAuthenticationException() {
        // Arrange
        BadCredentialsException exception = new BadCredentialsException("Bad credentials");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuthenticationException(exception, request);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Unauthorized", response.getBody().getError());
    }

    @Test
    void testHandleAccessDeniedException() {
        // Arrange
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDeniedException(exception, request);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().getStatus());
        assertEquals("Forbidden", response.getBody().getError());
    }

    @Test
    void testHandleForbiddenException() {
        // Arrange
        ForbiddenException exception = new ForbiddenException("Not authorized to delete", "User", "DELETE");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleForbiddenException(exception, request);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().getStatus());
        assertEquals("Forbidden", response.getBody().getError());
        assertTrue(response.getBody().getDetails().stream()
                .anyMatch(d -> d.contains("User")));
        assertTrue(response.getBody().getDetails().stream()
                .anyMatch(d -> d.contains("DELETE")));
    }

    @Test
    void testHandleResourceNotFoundException() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("User", "id", 123);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFoundException(exception, request);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Not Found", response.getBody().getError());
        assertTrue(response.getBody().getDetails().stream()
                .anyMatch(d -> d.contains("User")));
    }

    @Test
    void testHandleBusinessLogicException() {
        // Arrange
        BusinessLogicException exception = new BusinessLogicException("Insufficient balance", "BIZ_001");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessLogicException(exception, request);

        // Assert
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(422, response.getBody().getStatus());
        assertEquals("Business Logic Error", response.getBody().getError());
        assertEquals("Insufficient balance", response.getBody().getMessage());
        assertTrue(response.getBody().getDetails().stream()
                .anyMatch(d -> d.contains("BIZ_001")));
    }

    @Test
    void testHandleBusinessRuleViolationException() {
        // Arrange
        BusinessRuleViolationException exception = new BusinessRuleViolationException("Age must be 18 or older", "RULE_001");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessRuleViolationException(exception, request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Business Rule Violation", response.getBody().getError());
    }

    @Test
    void testHandleInvalidOperationException() {
        // Arrange
        InvalidOperationException exception = new InvalidOperationException("Cannot delete active user");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidOperationException(exception, request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Invalid Operation", response.getBody().getError());
    }

    @Test
    void testHandleGlobalException() {
        // Arrange
        Exception exception = new RuntimeException("Unexpected error");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGlobalException(exception, request);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals("An unexpected error occurred. Please try again later.", response.getBody().getMessage());
    }

    @Test
    void testErrorResponseContainsTimestamp() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("User not found");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFoundException(exception, request);

        // Assert
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testErrorResponseContainsPath() {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/users/123");
        ResourceNotFoundException exception = new ResourceNotFoundException("User not found");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFoundException(exception, request);

        // Assert
        assertNotNull(response.getBody());
        assertEquals("/api/users/123", response.getBody().getPath());
    }
}
