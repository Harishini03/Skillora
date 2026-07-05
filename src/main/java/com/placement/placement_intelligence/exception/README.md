# Exception Handling Guide

## Overview
This package contains the centralized exception handling mechanism for the Placement Intelligence REST API. All exceptions are handled by the `GlobalExceptionHandler` and return consistent error responses.

## Exception Hierarchy

### Custom Business Exceptions

#### 1. **ResourceNotFoundException** (HTTP 404)
- **Use Case**: When a requested resource cannot be found
- **Example**: User, Course, Student not found
```java
throw new ResourceNotFoundException("Student", "id", studentId);
throw new ResourceNotFoundException("Course not found");
```

#### 2. **UnauthorizedException** (HTTP 401)
- **Use Case**: Authentication required but not provided or invalid
- **Example**: Invalid JWT token, missing credentials
```java
throw new UnauthorizedException("Invalid authentication token");
```

#### 3. **ForbiddenException** (HTTP 403)
- **Use Case**: User is authenticated but lacks permission
- **Example**: Student trying to access staff-only resource
```java
throw new ForbiddenException("Not authorized to delete users", "User", "DELETE");
```

#### 4. **InvalidRequestException** (HTTP 400)
- **Use Case**: Request is malformed or missing required data
- **Example**: Invalid JSON, missing required fields
```java
throw new InvalidRequestException("Email format is invalid", "INVALID_EMAIL");
```

#### 5. **BusinessRuleViolationException** (HTTP 400)
- **Use Case**: Business rule constraints violated
- **Example**: Age restrictions, duplicate entries
```java
throw new BusinessRuleViolationException("Student age must be 18 or older", "AGE_RULE");
```

#### 6. **InvalidOperationException** (HTTP 400)
- **Use Case**: Operation cannot be performed due to current state
- **Example**: Cannot delete active user, cannot enroll in closed course
```java
throw new InvalidOperationException("Cannot delete user with active enrollments");
```

#### 7. **BusinessLogicException** (HTTP 422)
- **Use Case**: Semantic errors in well-formed requests
- **Example**: Insufficient balance, quota exceeded
```java
throw new BusinessLogicException("Course enrollment limit reached", "ENROLLMENT_LIMIT");
```

## Error Response Format

All exceptions return a consistent JSON structure:

```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Student not found with id: '123'",
  "path": "/api/students/123",
  "details": [
    "Resource: Student",
    "Field: id",
    "Value: 123"
  ]
}
```

### Validation Error Response

Validation errors include field-level details:

```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 400,
  "error": "Validation Failed",
  "message": "Request validation failed. Please check the field errors.",
  "path": "/api/students",
  "details": [
    "email: must not be blank",
    "age: must be greater than 0"
  ]
}
```

## HTTP Status Code Mapping

| Status Code | Exception Types | Use Case |
|-------------|----------------|----------|
| **400 Bad Request** | InvalidRequestException, BusinessRuleViolationException, InvalidOperationException, IllegalArgumentException, Validation errors | Malformed requests, validation failures, business rule violations |
| **401 Unauthorized** | UnauthorizedException, AuthenticationException, BadCredentialsException | Authentication failures |
| **403 Forbidden** | ForbiddenException, AccessDeniedException | Authorization failures |
| **404 Not Found** | ResourceNotFoundException, NoHandlerFoundException | Resource not found, endpoint not found |
| **422 Unprocessable Entity** | BusinessLogicException | Semantic errors in well-formed requests |
| **500 Internal Server Error** | All unhandled exceptions | Unexpected server errors |

## Logging

All exceptions are logged using SLF4J:

- **WARN level**: Client errors (4xx) - business exceptions, validation errors
- **ERROR level**: Server errors (5xx) - unexpected exceptions with full stack trace

Example log output:
```
2024-01-15 10:30:00 WARN  GlobalExceptionHandler - Resource not found on /api/students/123: Student not found with id: '123'
2024-01-15 10:30:05 ERROR GlobalExceptionHandler - Unexpected error on /api/courses: NullPointerException
java.lang.NullPointerException: ...
    at com.placement.service.CourseService.getCourse(CourseService.java:42)
    ...
```

## Usage Examples

### In Service Layer
```java
@Service
public class StudentService {
    
    public StudentDto getStudent(Long id) {
        return studentRepository.findById(id)
            .map(studentMapper::toDto)
            .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
    }
    
    public void enrollStudent(Long studentId, Long courseId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
            
        if (course.isFull()) {
            throw new BusinessLogicException("Course enrollment limit reached", "ENROLLMENT_LIMIT");
        }
        
        if (!course.isOpen()) {
            throw new InvalidOperationException("Cannot enroll in closed course");
        }
        
        // Process enrollment...
    }
    
    public void validateAge(Integer age) {
        if (age < 18) {
            throw new BusinessRuleViolationException("Student age must be 18 or older", "AGE_RULE");
        }
    }
}
```

### In Controller Layer
```java
@RestController
@RequestMapping("/api/students")
public class StudentController {
    
    @PostMapping
    public ResponseEntity<StudentDto> createStudent(@Valid @RequestBody StudentRequest request) {
        // Validation errors automatically handled by GlobalExceptionHandler
        StudentDto student = studentService.createStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(student);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<StudentDto> getStudent(@PathVariable Long id) {
        // ResourceNotFoundException automatically handled
        return ResponseEntity.ok(studentService.getStudent(id));
    }
}
```

## Best Practices

1. **Use Specific Exceptions**: Choose the most appropriate exception type for your use case
2. **Provide Clear Messages**: Exception messages should be user-friendly and actionable
3. **Include Error Codes**: Use error codes for business exceptions to help client apps handle errors
4. **Don't Expose Internals**: Never expose stack traces or internal details to clients (handled automatically)
5. **Log Appropriately**: Let GlobalExceptionHandler handle logging - don't log exceptions before throwing
6. **Use @Valid Annotation**: For request validation, use Bean Validation annotations and @Valid
7. **Chain Exceptions**: When wrapping exceptions, preserve the original cause for debugging

## Testing

See `GlobalExceptionHandlerTest.java` for comprehensive unit tests covering all exception scenarios.

To test exception handling in integration tests:
```java
@Test
void testResourceNotFound() throws Exception {
    mockMvc.perform(get("/api/students/999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").exists())
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.path").value("/api/students/999"));
}
```

## Configuration

No additional configuration required. `@RestControllerAdvice` on `GlobalExceptionHandler` automatically applies to all REST controllers.

For customization:
- Extend exception classes for domain-specific exceptions
- Add new @ExceptionHandler methods in GlobalExceptionHandler
- Modify ErrorResponse DTO for different response formats
