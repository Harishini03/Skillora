package com.placement.placement_intelligence.service;

import com.placement.placement_intelligence.dto.AuthRequest;
import com.placement.placement_intelligence.dto.AuthResponse;
import com.placement.placement_intelligence.dto.SignupRequest;
import com.placement.placement_intelligence.exception.InvalidRequestException;
import com.placement.placement_intelligence.model.AuthProvider;
import com.placement.placement_intelligence.model.Department;
import com.placement.placement_intelligence.model.Role;
import com.placement.placement_intelligence.model.StaffProfile;
import com.placement.placement_intelligence.model.Student;
import com.placement.placement_intelligence.model.User;
import com.placement.placement_intelligence.repository.DepartmentRepository;
import com.placement.placement_intelligence.repository.SkillRepository;
import com.placement.placement_intelligence.repository.StaffProfileRepository;
import com.placement.placement_intelligence.repository.StudentRepository;
import com.placement.placement_intelligence.repository.StudentSkillRepository;
import com.placement.placement_intelligence.repository.UserRepository;
import com.placement.placement_intelligence.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private SkillRepository skillRepository;
    @Mock
    private StaffProfileRepository staffProfileRepository;
    @Mock
    private StudentSkillRepository studentSkillRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private com.placement.placement_intelligence.service.FirebaseTokenVerifier firebaseTokenVerifier;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        PasswordValidationService passwordValidationService = new PasswordValidationService();
        authService = new AuthService(
                userRepository,
                studentRepository,
                departmentRepository,
                skillRepository,
                staffProfileRepository,
                studentSkillRepository,
                passwordEncoder,
                jwtService,
                "",
                passwordValidationService,
                firebaseTokenVerifier
        );
    }

    @Test
    void testLoginWithValidCredentials() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        String encodedPassword = "encodedPassword";
        
        User user = createUser(1L, username, "test@example.com", "Test User", encodedPassword, Role.STUDENT);
        Student student = createStudent(1L, user);
        
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(studentRepository.findByUser_Id(user.getId())).thenReturn(student);
        when(jwtService.generateToken(any())).thenReturn("jwt-token");
        when(userRepository.save(any(User.class))).thenReturn(user);

        AuthRequest request = new AuthRequest();
        request.setUsernameOrEmail(username);
        request.setPassword(password);

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals(user.getId(), response.getUserId());
        assertEquals(student.getId(), response.getStudentId());
        assertEquals(Role.STUDENT, response.getRole());
        assertEquals("jwt-token", response.getToken());
        assertEquals("Login successful", response.getMessage());
        verify(userRepository).save(user);
    }

    @Test
    void testLoginWithInvalidPassword() {
        // Arrange
        String username = "testuser";
        String password = "wrongpassword";
        String encodedPassword = "encodedPassword";
        
        User user = createUser(1L, username, "test@example.com", "Test User", encodedPassword, Role.STUDENT);
        
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

        AuthRequest request = new AuthRequest();
        request.setUsernameOrEmail(username);
        request.setPassword(password);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.login(request);
        });
        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void testLoginWithNonExistentUser() {
        // Arrange
        String username = "nonexistent";
        String password = "password123";
        
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(username)).thenReturn(Optional.empty());

        AuthRequest request = new AuthRequest();
        request.setUsernameOrEmail(username);
        request.setPassword(password);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.login(request);
        });
        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void testSignupWithValidData() {
        // Arrange
        Department department = new Department("Computer Science");
        department.setId(1L);
        
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> {
            Student student = invocation.getArgument(0);
            student.setId(1L);
            return student;
        });
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        SignupRequest request = new SignupRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("StrongPass123!");
        request.setName("New User");
        request.setRole(Role.STUDENT);
        request.setDepartmentId(1L);
        request.setCgpa(3.5);
        request.setLevel("Intermediate");

        // Act
        AuthResponse response = authService.signup(request);

        // Assert
        assertNotNull(response);
        assertEquals(Role.STUDENT, response.getRole());
        assertEquals("jwt-token", response.getToken());
        assertEquals("Signup successful", response.getMessage());
        verify(userRepository).save(any(User.class));
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void testSignupWithDuplicateUsername() {
        // Arrange
        User existingUser = createUser(1L, "existinguser", "existing@example.com", "Existing User", "password", Role.STUDENT);
        
        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(existingUser));

        SignupRequest request = new SignupRequest();
        request.setUsername("existinguser");
        request.setEmail("newemail@example.com");
        request.setPassword("StrongPass123!");
        request.setRole(Role.STUDENT);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.signup(request);
        });
        assertEquals("Username already exists", exception.getMessage());
    }

    @Test
    void testSignupWithDuplicateEmail() {
        // Arrange
        User existingUser = createUser(1L, "existinguser", "existing@example.com", "Existing User", "password", Role.STUDENT);
        
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUser));

        SignupRequest request = new SignupRequest();
        request.setUsername("newuser");
        request.setEmail("existing@example.com");
        request.setPassword("StrongPass123!");
        request.setRole(Role.STUDENT);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.signup(request);
        });
        assertEquals("Email already exists", exception.getMessage());
    }

    @Test
    void testPasswordValidation_TooShort() {
        // Arrange
        SignupRequest request = new SignupRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("Short1!");
        request.setRole(Role.STUDENT);

        // Act & Assert
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            authService.signup(request);
        });
        assertTrue(exception.getMessage().contains("8 characters") || exception.getMessage().contains("least 8"));
    }

    @Test
    void testPasswordValidation_NoUppercase() {
        // Arrange
        SignupRequest request = new SignupRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("lowercase123!");
        request.setRole(Role.STUDENT);

        // Act & Assert
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            authService.signup(request);
        });
        assertTrue(exception.getMessage().contains("uppercase"));
    }

    @Test
    void testPasswordValidation_NoLowercase() {
        // Arrange
        SignupRequest request = new SignupRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("UPPERCASE123!");
        request.setRole(Role.STUDENT);

        // Act & Assert
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            authService.signup(request);
        });
        assertTrue(exception.getMessage().contains("lowercase"));
    }

    @Test
    void testPasswordValidation_NoNumber() {
        // Arrange
        SignupRequest request = new SignupRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("NoNumbers!");
        request.setRole(Role.STUDENT);

        // Act & Assert
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            authService.signup(request);
        });
        assertTrue(exception.getMessage().contains("digit") || exception.getMessage().contains("number"));
    }

    @Test
    void testPasswordValidation_NoSpecialCharacter() {
        // Arrange
        SignupRequest request = new SignupRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("NoSpecial123");
        request.setRole(Role.STUDENT);

        // Act & Assert
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            authService.signup(request);
        });
        assertTrue(exception.getMessage().contains("special character"));
    }

    @Test
    void testSignupCreatesStudentProfileForStudentRole() {
        // Arrange
        Department department = new Department("Computer Science");
        department.setId(1L);
        
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> {
            Student student = invocation.getArgument(0);
            student.setId(1L);
            return student;
        });
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        SignupRequest request = new SignupRequest();
        request.setUsername("student");
        request.setEmail("student@example.com");
        request.setPassword("StrongPass123!");
        request.setRole(Role.STUDENT);
        request.setDepartmentId(1L);

        // Act
        AuthResponse response = authService.signup(request);

        // Assert
        assertNotNull(response.getStudentId());
        verify(studentRepository).save(any(Student.class));
        verify(staffProfileRepository, never()).save(any(StaffProfile.class));
    }

    @Test
    void testSignupCreatesStaffProfileForStaffRole() {
        // Arrange
        Department department = new Department("Computer Science");
        department.setId(1L);
        
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(staffProfileRepository.save(any(StaffProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        SignupRequest request = new SignupRequest();
        request.setUsername("staff");
        request.setEmail("staff@example.com");
        request.setPassword("StrongPass123!");
        request.setRole(Role.STAFF);
        request.setDepartmentId(1L);

        // Act
        AuthResponse response = authService.signup(request);

        // Assert
        assertNull(response.getStudentId());
        verify(staffProfileRepository).save(any(StaffProfile.class));
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void testSignupCreatesStaffProfileForRecruiterRole() {
        // Arrange
        Department department = new Department("Computer Science");
        department.setId(1L);
        
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(staffProfileRepository.save(any(StaffProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        SignupRequest request = new SignupRequest();
        request.setUsername("recruiter");
        request.setEmail("recruiter@example.com");
        request.setPassword("StrongPass123!");
        request.setRole(Role.RECRUITER);
        request.setDepartmentId(1L);

        // Act
        AuthResponse response = authService.signup(request);

        // Assert
        assertNull(response.getStudentId());
        verify(staffProfileRepository).save(any(StaffProfile.class));
        verify(studentRepository, never()).save(any(Student.class));
    }

    // Helper methods
    private User createUser(Long id, String username, String email, String name, String passwordHash, Role role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setName(name);
        user.setPasswordHash(passwordHash);
        user.setRole(role);
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setActive(true);
        return user;
    }

    private Student createStudent(Long id, User user) {
        Student student = new Student();
        student.setId(id);
        student.setUser(user);
        student.setName(user.getName());
        Department dept = new Department("Computer Science");
        dept.setId(1L);
        student.setDepartment(dept);
        student.setCgpa(3.5);
        student.setLevel("Intermediate");
        return student;
    }
}
