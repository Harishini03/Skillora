package com.placement.placement_intelligence.service;

import com.placement.placement_intelligence.dto.AuthRequest;
import com.placement.placement_intelligence.dto.AuthResponse;
import com.placement.placement_intelligence.dto.FirebaseLoginRequest;
import com.placement.placement_intelligence.dto.GoogleLoginRequest;
import com.placement.placement_intelligence.dto.SignupRequest;
import com.placement.placement_intelligence.exception.InvalidRequestException;
import com.placement.placement_intelligence.model.AuthProvider;
import com.placement.placement_intelligence.model.Department;
import com.placement.placement_intelligence.model.Role;
import com.placement.placement_intelligence.model.Skill;
import com.placement.placement_intelligence.model.StaffProfile;
import com.placement.placement_intelligence.model.Student;
import com.placement.placement_intelligence.model.StudentSkill;
import com.placement.placement_intelligence.model.StudentSkillId;
import com.placement.placement_intelligence.model.User;
import com.placement.placement_intelligence.repository.DepartmentRepository;
import com.placement.placement_intelligence.repository.SkillRepository;
import com.placement.placement_intelligence.repository.StaffProfileRepository;
import com.placement.placement_intelligence.repository.StudentRepository;
import com.placement.placement_intelligence.repository.StudentSkillRepository;
import com.placement.placement_intelligence.repository.UserRepository;
import com.placement.placement_intelligence.security.JwtService;
import com.placement.placement_intelligence.security.UserPrincipal;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class AuthService {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Set<Role> SUPPORTED_ROLES = EnumSet.of(Role.STUDENT, Role.STAFF, Role.RECRUITER);

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;
    private final SkillRepository skillRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final StudentSkillRepository studentSkillRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final String googleClientId;
    private final PasswordValidationService passwordValidationService;
    private final FirebaseTokenVerifier firebaseTokenVerifier;

    public AuthService(UserRepository userRepository,
                       StudentRepository studentRepository,
                       DepartmentRepository departmentRepository,
                       SkillRepository skillRepository,
                       StaffProfileRepository staffProfileRepository,
                       StudentSkillRepository studentSkillRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       @Value("${app.google.client-id:}") String googleClientId,
                       PasswordValidationService passwordValidationService,
                       FirebaseTokenVerifier firebaseTokenVerifier) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.departmentRepository = departmentRepository;
        this.skillRepository = skillRepository;
        this.staffProfileRepository = staffProfileRepository;
        this.studentSkillRepository = studentSkillRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.googleClientId = googleClientId;
        this.passwordValidationService = passwordValidationService;
        this.firebaseTokenVerifier = firebaseTokenVerifier;
    }

    @Transactional
    public AuthResponse login(AuthRequest request) {
        User user = findUserByUsernameOrEmail(request.getUsernameOrEmail());
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        if (request.getRole() != null && !request.getRole().isBlank()) {
            Role requestedRole = parseRole(request.getRole());
            validateSupportedRole(requestedRole);
            if (user.getRole() != requestedRole) {
                throw new IllegalArgumentException("Role mismatch for this account");
            }
        }
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        Long studentId = resolveStudentId(user);
        return createAuthResponse(user, studentId, "Login successful");
    }

    @Transactional
    public AuthResponse googleLogin(GoogleLoginRequest request) {
        GoogleIdentity identity = verifyGoogleIdentity(request);
        String email = identity.email() != null ? identity.email() : request.getEmail();
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Google email is required");
        }
        String name = identity.name() != null ? identity.name() : request.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            if (request.getRole() == null) {
                throw new IllegalArgumentException("Role is required for first-time Google sign-in");
            }
            validateSupportedRole(request.getRole());
            if (request.getDepartmentId() == null && isBlank(request.getDepartmentName())) {
                throw new IllegalArgumentException("Department is required for first-time Google sign-in");
            }
            String generatedUsername = email.split("@")[0] + "_" + UUID.randomUUID().toString().substring(0, 8);
            validateUniqueUser(generatedUsername, email);
            user = createBaseUser(generatedUsername, email, safeName(name, email), UUID.randomUUID().toString(),
                    request.getRole(), AuthProvider.GOOGLE);
            createRoleProfile(user, request.getRole(), request.getDepartmentId(), request.getDepartmentName(),
                    0.0, "Beginner", "", Collections.emptyList());
        }
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        return createAuthResponse(user, resolveStudentId(user), "Google login successful");
    }

    @Transactional
    public AuthResponse firebaseLogin(FirebaseLoginRequest request) {
        FirebaseToken decodedToken = firebaseTokenVerifier.verifyIdToken(request.getIdToken());
        String email = (String) decodedToken.getClaims().getOrDefault("email", decodedToken.getEmail());
        String name = (String) decodedToken.getClaims().getOrDefault("name", null);
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Firebase token does not contain an email address");
        }
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            if (request.getRole() == null) {
                throw new InvalidRequestException("Role is required for first-time Firebase sign-in");
            }
            validateSupportedRole(request.getRole());
            String generatedUsername = email.split("@")[0] + "_" + UUID.randomUUID().toString().substring(0, 8);
            validateUniqueUser(generatedUsername, email);
            user = createBaseUser(generatedUsername, email, safeName(name, email), UUID.randomUUID().toString(),
                    request.getRole(), AuthProvider.GOOGLE);
            createRoleProfile(user, request.getRole(), request.getDepartmentId(), request.getDepartmentName(),
                    request.getCgpa() != null ? request.getCgpa() : 0.0,
                    request.getLevel() != null ? request.getLevel() : "Beginner",
                    request.getInterests() != null ? request.getInterests() : "",
                    Collections.emptyList());
        }
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        return createAuthResponse(user, resolveStudentId(user), "Firebase login successful");
    }

    private User createBaseUser(String username, String email, String name, String password, Role role, AuthProvider provider) {        if (role == null) {
            throw new IllegalArgumentException("Role is required");
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setName(safeName(name, email));
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        user.setAuthProvider(provider);
        user.setActive(true);
        user.setLastLoginAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private Long createRoleProfile(User user, Role role, Long departmentId, String departmentName, Double cgpa, String level, String interests,
                                   List<Long> skillIds) {
        Department department = resolveDepartment(departmentId, departmentName);
        if (role == Role.STUDENT) {
            Student student = new Student();
            student.setName(user.getName());
            student.setDepartment(department);
            student.setCgpa(cgpa == null ? 0.0 : cgpa);
            student.setLevel(level == null || level.isBlank() ? "Beginner" : level);
            student.setInterests(interests);
            student.setUser(user);
            student.setReadinessScore(0.0);
            student = studentRepository.save(student);
            if (skillIds != null && !skillIds.isEmpty()) {
                List<Skill> skills = skillRepository.findAllById(skillIds);
                for (Skill skill : skills) {
                    StudentSkill studentSkill = new StudentSkill();
                    studentSkill.setId(new StudentSkillId(student.getId(), skill.getId()));
                    studentSkill.setStudent(student);
                    studentSkill.setSkill(skill);
                    studentSkill.setSkillScore(0.0);
                    studentSkillRepository.save(studentSkill);
                }
            }
            return student.getId();
        }
        StaffProfile profile = new StaffProfile();
        profile.setUser(user);
        profile.setDepartment(department);
        staffProfileRepository.save(profile);
        return null;
    }

    private Long resolveStudentId(User user) {
        if (user.getRole() != Role.STUDENT) {
            return null;
        }
        Student student = studentRepository.findByUser_Id(user.getId());
        return student == null ? null : student.getId();
    }

    private AuthResponse createAuthResponse(User user, Long studentId, String message) {
        String token = jwtService.generateToken(new UserPrincipal(user));
        return new AuthResponse(user.getId(), studentId, user.getRole(), user.getName(), user.getEmail(), token, message);
    }

    private GoogleIdentity verifyGoogleIdentity(GoogleLoginRequest request) {
        if (request.getIdToken() == null || request.getIdToken().isBlank()) {
            return new GoogleIdentity(request.getEmail(), request.getName());
        }
        try {
            GoogleIdTokenVerifier.Builder builder = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance());
            if (googleClientId != null && !googleClientId.isBlank()) {
                builder.setAudience(List.of(googleClientId));
            }
            GoogleIdToken idToken = builder.build().verify(request.getIdToken());
            if (idToken == null) {
                throw new IllegalArgumentException("Invalid Google token");
            }
            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = (String) payload.get("email");
            String name = (String) payload.get("name");
            return new GoogleIdentity(email, name);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Google token verification failed");
        }
    }

    private User findUserByUsernameOrEmail(String usernameOrEmail) {
        if (usernameOrEmail == null || usernameOrEmail.isBlank()) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        User user = userRepository.findByUsername(usernameOrEmail).orElse(null);
        if (user != null) {
            return user;
        }
        user = userRepository.findByEmail(usernameOrEmail).orElse(null);
        if (user != null) {
            return user;
        }
        throw new IllegalArgumentException("Invalid credentials");
    }

    private Role parseRole(String role) {
        try {
            return Role.valueOf(role.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }

    private void validateUniqueUser(String username, String email) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Enter a valid email address");
        }
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }
    }

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        List<String> passwordViolations = passwordValidationService.validatePassword(request.getPassword());
        if (!passwordViolations.isEmpty()) {
            throw new InvalidRequestException(String.join("; ", passwordViolations));
        }
        validateUniqueUser(request.getUsername(), request.getEmail());
        validateSupportedRole(request.getRole());
        User user = createBaseUser(
                request.getUsername(),
                request.getEmail(),
                request.getName(),
                request.getPassword(),
                request.getRole(),
                AuthProvider.LOCAL
        );
        Long studentId = createRoleProfile(user, request.getRole(), request.getDepartmentId(), request.getDepartmentName(), request.getCgpa(),
                request.getLevel(), request.getInterests(), request.getSkillIds());
        return createAuthResponse(user, studentId, "Signup successful");
    }

    private Department resolveDepartment(Long departmentId, String departmentName) {
        if (departmentId != null) {
            return departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new IllegalArgumentException("Department not found"));
        }

        if (!isBlank(departmentName)) {
            String cleanName = departmentName.trim();
            return departmentRepository.findByNameIgnoreCase(cleanName)
                    .orElseGet(() -> departmentRepository.save(new Department(cleanName)));
        }

        return departmentRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String safeName(String name, String email) {
        if (name != null && !name.isBlank()) {
            return name;
        }
        return email == null ? "User" : email.split("@")[0];
    }

    private void validateSupportedRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role is required");
        }
        if (!SUPPORTED_ROLES.contains(role)) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }

    private static class GoogleIdentity {
        private final String email;
        private final String name;

        private GoogleIdentity(String email, String name) {
            this.email = email;
            this.name = name;
        }

        private String email() {
            return email;
        }

        private String name() {
            return name;
        }
    }
}
