package com.placement.placement_intelligence.controller;

import com.placement.placement_intelligence.dto.AuthRequest;
import com.placement.placement_intelligence.dto.AuthResponse;
import com.placement.placement_intelligence.dto.FirebaseLoginRequest;
import com.placement.placement_intelligence.dto.GoogleLoginRequest;
import com.placement.placement_intelligence.dto.SignupRequest;
import com.placement.placement_intelligence.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final String googleClientId;

    public AuthController(AuthService authService, @Value("${app.google.client-id:}") String googleClientId) {
        this.authService = authService;
        this.googleClientId = googleClientId;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest request) {
        return ResponseEntity.ok(authService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/google-login")
    public ResponseEntity<AuthResponse> googleLogin(@RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(authService.googleLogin(request));
    }

    @PostMapping("/firebase-login")
    public ResponseEntity<AuthResponse> firebaseLogin(@RequestBody FirebaseLoginRequest request) {
        return ResponseEntity.ok(authService.firebaseLogin(request));
    }

    @GetMapping("/google-client-id")
    public ResponseEntity<Map<String, String>> googleClientId() {
        return ResponseEntity.ok(Map.of("clientId", googleClientId == null ? "" : googleClientId));
    }
}
